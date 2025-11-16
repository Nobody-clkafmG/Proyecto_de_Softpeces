package com.softpeces.ia;

import ai.onnxruntime.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public final class IAServiceOnnx implements IAService {

    private final OrtEnvironment env;
    private final OrtSession session;
    private final String inputName;

    private final int imgSize;
    private final float confTh;
    private final float iouTh;

    private final String  activation;   // "sigmoid" | "softmax"
    private final float   labelMargin;  // margen de indecisión
    private final boolean swapEye;      // invertir etiqueta en OJO
    private final boolean swapGill;     // invertir etiqueta en BRANQUIAS
    private final boolean debug;        // logs

    // NUEVO
    private final float minBoxArea;     // píxeles (fracción*imgSize^2)
    private final boolean onlyPairTop1; // usar solo cajas cuyo top1 pertenece al par

    private final String[] CLASSES;

    public IAServiceOnnx(Path onnxPath, int imgSize, float confTh, float iouTh) throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        this.session = env.createSession(onnxPath.toString(), opts);
        this.inputName = this.session.getInputInfo().keySet().iterator().next();

        this.imgSize = imgSize;
        this.confTh  = confTh;
        this.iouTh   = iouTh;

        // --- properties (sin AppConfig.b) ---
        this.activation  = com.softpeces.infra.AppConfig
                .s("ai.activation", "sigmoid")
                .toLowerCase(java.util.Locale.ROOT);
        this.labelMargin = com.softpeces.infra.AppConfig.f("ai.label.margin", 0.05f);
        this.swapEye     = propBool("ai.swap.eye",  false);
        this.swapGill    = propBool("ai.swap.gill", false);
        this.debug       = propBool("ai.debug",     false);

        // NUEVO: filtros
        this.onlyPairTop1 = propBool("ai.only_pair_top1", true);
        float minFrac = com.softpeces.infra.AppConfig.f("ai.min_box_area", 0.002f); // 0.2%
        this.minBoxArea = Math.max(0f, minFrac) * (imgSize * imgSize);

        // --- clases desde archivo o default ---
        String classesPath = com.softpeces.infra.AppConfig.s("ai.classes.path", "");
        String[] defaults = new String[]{"fresh_eye","fresh_gill","not_fresh_eye","not_fresh_gill"};
        if (!classesPath.isBlank() && Files.exists(Path.of(classesPath))) {
            List<String> lines = Files.readAllLines(Path.of(classesPath));
            this.CLASSES = lines.stream().map(String::trim).filter(s -> !s.isBlank()).toArray(String[]::new);
        } else {
            this.CLASSES = defaults;
        }

        if (debug) {
            System.out.println("[IA] ONNX=" + onnxPath + " img=" + imgSize + " conf=" + confTh + " iou=" + iouTh);
            System.out.println("[IA] classes=" + Arrays.toString(CLASSES)
                    + " activation=" + activation
                    + " margin=" + labelMargin
                    + " swapEye=" + swapEye + " swapGill=" + swapGill
                    + " onlyPairTop1=" + onlyPairTop1 + " minBoxArea(px)=" + minBoxArea);
        }
    }

    private static boolean propBool(String key, boolean def) {
        String v = com.softpeces.infra.AppConfig.s(key, Boolean.toString(def));
        return "1".equals(v) || "true".equalsIgnoreCase(v);
    }

    // -------- Parte (OJO / BRANQUIAS) ----------
    private enum Parte { OJO, BRANQUIAS, DESCONOCIDA;

        static Parte from(String s) {
            if (s == null) return DESCONOCIDA;
            String k = s.trim().toUpperCase(java.util.Locale.ROOT);
            if (k.startsWith("OJO")) return OJO;
            if (k.startsWith("BRA") || k.contains("GILL")) return BRANQUIAS;
            return DESCONOCIDA;
        }

        int freshIndex(String[] classes) {
            return switch (this) {
                case OJO       -> indexOf(classes, "fresh_eye");
                case BRANQUIAS -> indexOf(classes, "fresh_gill");
                default        -> maxIndex(classes, "fresh_eye", "fresh_gill");
            };
        }

        int notFreshIndex(String[] classes) {
            return switch (this) {
                case OJO       -> indexOf(classes, "not_fresh_eye");
                case BRANQUIAS -> indexOf(classes, "not_fresh_gill");
                default        -> maxIndex(classes, "not_fresh_eye", "not_fresh_gill");
            };
        }

        private static int indexOf(String[] a, String name) {
            for (int i = 0; i < a.length; i++) if (a[i].equalsIgnoreCase(name)) return i;
            throw new IllegalArgumentException("Clase no encontrada: " + name + " en " + java.util.Arrays.toString(a));
        }
        private static int maxIndex(String[] a, String n1, String n2) {
            int i1 = -1, i2 = -1;
            for (int i = 0; i < a.length; i++) {
                if (a[i].equalsIgnoreCase(n1)) i1 = i;
                if (a[i].equalsIgnoreCase(n2)) i2 = i;
            }
            return Math.max(i1, i2);
        }
    }

    // ===========================================================
    // API
    // ===========================================================
    @Override
    public Prediction predict(String imagePath, String parte) throws Exception {
        // 1) Preproceso
        float[] chw = preprocessLetterbox(imagePath, imgSize);
        long[] inShape = new long[]{1, 3, imgSize, imgSize};

        // 2) Inferencia
        float[] outData;
        long[]  outShape;
        try (OnnxTensor input = OnnxTensor.createTensor(env, FloatBuffer.wrap(chw), inShape);
             OrtSession.Result result = session.run(Collections.singletonMap(inputName, input))) {

            OnnxTensor out = (OnnxTensor) result.get(0);
            outShape = out.getInfo().getShape();             // [1, 4+nc, n] o [1, n, 4+nc]
            FloatBuffer fb = out.getFloatBuffer();
            outData = new float[fb.remaining()];
            fb.get(outData);
        }
        if (debug) System.out.println("[IA] outShape=" + Arrays.toString(outShape));

        // 3) Decodificación
        Decoded dec = decodeDynamic(outData, outShape, CLASSES.length);

        // 4) ¿AUTO o manual?
        boolean auto = (parte == null) || parte.isBlank()
                || parte.equalsIgnoreCase("AUTO") || parte.equalsIgnoreCase("MIXTO");

        if (auto) {
            int eyeFresh  = Parte.OJO.freshIndex(CLASSES);
            int eyeNot    = Parte.OJO.notFreshIndex(CLASSES);
            int gillFresh = Parte.BRANQUIAS.freshIndex(CLASSES);
            int gillNot   = Parte.BRANQUIAS.notFreshIndex(CLASSES);

            Box bestEye  = bestForPair(dec, eyeFresh,  eyeNot);
            Box bestGill = bestForPair(dec, gillFresh, gillNot);

            if (bestEye == null && bestGill == null) {
                if (debug) System.out.println("[IA] AUTO: sin detección ≥ conf");
                return new Prediction("SIN DETECCIÓN", 0f, "onnx");
            }

            Box best; Parte used;
            if (bestGill == null || (bestEye != null && bestEye.score >= bestGill.score)) {
                best = bestEye;  used = Parte.OJO;
                if (swapEye) best = best.swap();
            } else {
                best = bestGill; used = Parte.BRANQUIAS;
                if (swapGill) best = best.swap();
            }

            float diff = Math.abs(best.pFresh - best.pNot);
            String finalLabel = (diff < labelMargin) ? "DUDOSO" : best.label;

            if (debug) {
                System.out.printf(java.util.Locale.US,
                        "[IA] AUTO→%s best=%s pFresh=%.4f pNot=%.4f prob=%.4f diff=%.4f%n",
                        used.name(), finalLabel, best.pFresh, best.pNot, best.score, diff);
            }
            return new Prediction(finalLabel, best.score, "onnx");
        }

        // ----- Modo manual (parte fija) -----
        Parte p = Parte.from(parte);
        int idxFresh = p.freshIndex(CLASSES);
        int idxNot   = p.notFreshIndex(CLASSES);

        Box best = bestForPair(dec, idxFresh, idxNot);
        if (best == null) {
            if (debug) System.out.println("[IA] sin detección ≥ conf");
            return new Prediction("SIN DETECCIÓN", 0f, "onnx");
        }

        if ((p == Parte.OJO && swapEye) || (p == Parte.BRANQUIAS && swapGill)) best = best.swap();

        float diff = Math.abs(best.pFresh - best.pNot);
        String finalLabel = (diff < labelMargin) ? "DUDOSO" : best.label;

        if (debug) {
            System.out.printf(java.util.Locale.US,
                    "[IA] parte=%s best=%s pFresh=%.4f pNot=%.4f prob=%.4f diff=%.4f%n",
                    p.name(), finalLabel, best.pFresh, best.pNot, best.score, diff);
        }
        return new Prediction(finalLabel, best.score, "onnx");
    }

    // ===========================================================
    // Helpers de prob/clases y selección por par
    // ===========================================================
    private float[] probsForBox(Decoded dec, int i) {
        int nc = CLASSES.length;
        float[] p = new float[nc];
        if ("softmax".equals(activation)) {
            float m = -1e9f;
            for (int c = 0; c < nc; c++) {
                float v = dec.get(i, 4 + c);
                if (v > m) m = v;
            }
            float sum = 0f;
            for (int c = 0; c < nc; c++) {
                p[c] = (float) Math.exp(dec.get(i, 4 + c) - m);
                sum += p[c];
            }
            float inv = 1f / (sum + 1e-9f);
            for (int c = 0; c < nc; c++) p[c] *= inv;
        } else {
            for (int c = 0; c < nc; c++) p[c] = sigmoid(dec.get(i, 4 + c));
        }
        return p;
    }
    private static int argmax(float[] a) {
        int k = 0; float v = a[0];
        for (int i = 1; i < a.length; i++) if (a[i] > v) { v = a[i]; k = i; }
        return k;
    }

    // Retorna la mejor caja para el par (idxFresh, idxNot) o null si no hay
    private Box bestForPair(Decoded dec, int idxFresh, int idxNot) {
        List<Box> cands = new ArrayList<>();
        for (int i = 0; i < dec.n; i++) {

            float cx = dec.get(i,0), cy = dec.get(i,1), w = dec.get(i,2), h = dec.get(i,3);
            float x1 = cx - w/2f, y1 = cy - h/2f, x2 = cx + w/2f, y2 = cy + h/2f;
            float area = w * h;
            if (area < minBoxArea) continue; // descarta cajas diminutas

            float[] probs = probsForBox(dec, i);
            int kTop = argmax(probs);

            // Usa solo cajas cuyo top-1 pertenezca al par solicitado
            if (onlyPairTop1 && !(kTop == idxFresh || kTop == idxNot)) continue;

            float pFresh = probs[idxFresh];
            float pNot   = probs[idxNot];
            float score  = Math.max(pFresh, pNot);
            if (score < confTh) continue;

            String lbl = (pFresh >= pNot) ? "FRESCO" : "NO FRESCO";
            cands.add(new Box(x1,y1,x2,y2,score,lbl,pFresh,pNot));
        }
        if (cands.isEmpty()) return null;
        List<Box> kept = nms(cands, iouTh);
        return kept.get(0);
    }

    // ===========================================================
    // Preproceso (letterbox 640x640 RGB NCHW [0..1])
    // ===========================================================
    private static float[] preprocessLetterbox(String imagePath, int size) throws Exception {
        BufferedImage src = ImageIO.read(new File(imagePath));
        if (src == null) throw new IllegalArgumentException("No se pudo leer: " + imagePath);
        int w = src.getWidth(), h = src.getHeight();
        float r = Math.min(size / (float) w, size / (float) h);
        int nw = Math.round(w * r), nh = Math.round(h * r);
        int dx = (size - nw) / 2, dy = (size - nh) / 2;

        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = dst.createGraphics();
        g.setColor(new Color(114,114,114)); g.fillRect(0,0,size,size);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, dx,dy, dx+nw,dy+nh, 0,0,w,h, null);
        g.dispose();

        float[] chw = new float[3 * size * size];
        int idxR=0, idxG=size*size, idxB=2*size*size;
        for (int y=0;y<size;y++){
            for (int x=0;x<size;x++){
                int rgb = dst.getRGB(x,y);
                float r8=((rgb>>16)&0xFF)/255f;
                float g8=((rgb>>8)&0xFF)/255f;
                float b8=(rgb&0xFF)/255f;
                int pos = y*size + x;
                chw[idxR+pos]=r8; chw[idxG+pos]=g8; chw[idxB+pos]=b8;
            }
        }
        return chw;
    }

    // ===========================================================
    // Decodificador dinámico
    // ===========================================================
    private static final class Decoded {
        final float[] data; final int n; final int attrs; final boolean transposed;
        Decoded(float[] data, int n, int attrs, boolean t){ this.data=data; this.n=n; this.attrs=attrs; this.transposed=t; }
        float get(int i,int a){ return transposed ? data[i*attrs + a] : data[a*n + i]; }
    }
    private static Decoded decodeDynamic(float[] outData, long[] outShape, int nc) {
        if (outShape.length != 3 || outShape[0] != 1)
            throw new IllegalStateException("Forma inesperada: "+Arrays.toString(outShape));
        int a=(int)outShape[1], b=(int)outShape[2], attrs=4+nc;
        if (a==attrs) return new Decoded(outData, b, attrs, false); // [1, attrs, n]
        if (b==attrs) return new Decoded(outData, a, attrs, true ); // [1, n, attrs]
        throw new IllegalStateException("attrs != 4+nc. shape="+Arrays.toString(outShape)+" esperado attrs="+attrs);
    }

    // ===========================================================
    // NMS / utils
    // ===========================================================
    private static final class Box {
        final float x1,y1,x2,y2,score,pFresh,pNot; final String label;
        Box(float x1,float y1,float x2,float y2,float score,String label,float pFresh,float pNot){
            this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2; this.score=score; this.label=label;
            this.pFresh=pFresh; this.pNot=pNot;
        }
        Box swap(){ return new Box(x1,y1,x2,y2,score, "FRESCO".equals(label)?"NO FRESCO":"FRESCO", pNot, pFresh); }
    }
    private static List<Box> nms(List<Box> boxes, float iouTh){
        boxes.sort((a,b)->Float.compare(b.score,a.score));
        List<Box> keep = new ArrayList<>();
        boolean[] rm = new boolean[boxes.size()];
        for (int i=0;i<boxes.size();i++){
            if (rm[i]) continue; Box bi=boxes.get(i); keep.add(bi);
            for (int j=i+1;j<boxes.size();j++){ if (!rm[j] && iou(bi,boxes.get(j))>iouTh) rm[j]=true; }
        }
        return keep;
    }
    private static float iou(Box a, Box b){
        float xx1 = Math.max(a.x1, b.x1);
        float yy1 = Math.max(a.y1, b.y1);
        float xx2 = Math.min(a.x2, b.x2);
        float yy2 = Math.min(a.y2, b.y2);
        float w = Math.max(0f, xx2 - xx1);
        float h = Math.max(0f, yy2 - yy1);
        float inter = w * h;

        float areaA = Math.max(0f, a.x2 - a.x1) * Math.max(0f, a.y2 - a.y1);
        float areaB = Math.max(0f, b.x2 - b.x1) * Math.max(0f, b.y2 - b.y1);

        float uni = areaA + areaB - inter + 1e-6f;
        return inter / uni;
    }
    private static float sigmoid(float x){ return (float)(1.0/(1.0+Math.exp(-x))); }
}
