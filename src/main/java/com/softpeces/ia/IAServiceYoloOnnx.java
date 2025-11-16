package com.softpeces.ia;

import ai.onnxruntime.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.Collectors;

import com.softpeces.domain.Parte;

/**
 * IAService con ONNX (Ultralytics YOLOv8/11 .onnx) para detección de:
 *  fresh_eye, not_fresh_eye, fresh_gill, not_fresh_gill
 */
public class IAServiceYoloOnnx implements IAService, AutoCloseable {

    private final OrtEnvironment env;
    private final OrtSession session;
    private final int imgSize = 640; // exportaste a 640x640
    private final List<String> classes = List.of(
            "fresh_eye","fresh_gill","not_fresh_eye","not_fresh_gill"
    );

    public IAServiceYoloOnnx(String onnxPath) {
        try {
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(onnxPath, new OrtSession.SessionOptions());
        } catch (Exception e) {
            throw new RuntimeException("Error cargando modelo ONNX: " + onnxPath, e);
        }
    }

    @Override
    public Prediction predict(String imagePath, String parte) throws Exception {
        BufferedImage img = ImageIO.read(new File(imagePath));
        if (img == null) throw new IllegalArgumentException("No se pudo leer imagen: " + imagePath);

        // 1) Pre-proceso: letterbox a 640x640, RGB, float32 [1,3,640,640], normalizado [0..1]
        float[] chw = preprocess(img, imgSize, imgSize);

        Map<String, OnnxTensor> inputs = new HashMap<>();
        String inputName = session.getInputInfo().keySet().iterator().next();
        long[] shape = new long[]{1, 3, imgSize, imgSize};
        inputs.put(inputName, OnnxTensor.createTensor(env, FloatBuffer.wrap(chw), shape));

        // 2) Inferencia
        OrtSession.Result out = session.run(inputs);
        // Salida típica Ultralytics: [1, N, 84] ó [1, 84, N]. Cubrimos ambas.
        float[][][] preds = null;
        float[][] preds2 = null;

        for (Map.Entry<String, OnnxValue> e : out) {
            OnnxValue v = e.getValue();
            if (v instanceof OnnxTensor t) {
                long[] s = t.getInfo().getShape();
                if (s.length == 3 && s[0] == 1) {
                    if (s[1] > s[2]) { // [1,84,N] -> transponer a [1,N,84]
                        float[][][] raw = (float[][][]) t.getValue();
                        preds = transpose84N_to_N84(raw);
                    } else {          // [1,N,84]
                        preds = (float[][][]) t.getValue();
                    }
                } else if (s.length == 2) { // [N,84]
                    preds2 = (float[][]) null; // not used
                }
            }
        }
        if (preds == null && preds2 == null) {
            throw new IllegalStateException("No pude interpretar la salida del ONNX (shape inesperado).");
        }

        // 3) Post-proceso simple: tomar la mejor detección por clase
        // Formato esperado por Ultralytics: [x,y,w,h, conf, cls0, cls1, ...]
        List<Det> dets = new ArrayList<>();
        if (preds != null) {
            float[][] arr = preds[0]; // [N,84]
            for (float[] row : arr) {
                float conf = row[4];
                if (conf < 0.25f) continue; // umbral base
                int best = argmax(row, 5);  // mejor clase desde 5 en adelante
                float p = row[5 + best] * conf;
                dets.add(new Det(best, p));
            }
        }
        // Si la exportación te devolviera otro layout, aquí adaptaríamos.

        // 4) Filtrar por PARTE (OJO/BRANQUIAS)
        Parte parteEnum = null;
        try { parteEnum = Parte.valueOf(parte); } catch (Exception ignored) {}
        String label = "N/A";
        double prob = 0;

        // mapeo por parte
        Set<Integer> clsOk, clsBad;
        if (parteEnum == Parte.OJO) {
            clsOk  = Set.of(idx("fresh_eye"));
            clsBad = Set.of(idx("not_fresh_eye"));
        } else if (parteEnum == Parte.BRANQUIAS) {
            clsOk  = Set.of(idx("fresh_gill"));
            clsBad = Set.of(idx("not_fresh_gill"));
        } else {
            // PIEL (sin modelo) -> devolvemos error coherente
            return new Prediction("No soportado (PIEL)", 0.0, "yolo-onnx");
        }

        double bestOk  = dets.stream().filter(d->clsOk.contains(d.cls)).mapToDouble(d->d.score).max().orElse(0);
        double bestBad = dets.stream().filter(d->clsBad.contains(d.cls)).mapToDouble(d->d.score).max().orElse(0);

        if (bestOk==0 && bestBad==0) {
            // Sin detecciones relevantes: déjalo como no aceptable con baja confianza
            label = "No aceptable";
            prob  = 0.51;
        } else if (bestOk >= bestBad) {
            label = "Aceptable";
            prob  = clamp(bestOk, 0.5, 0.999);
        } else {
            label = "No aceptable";
            prob  = clamp(bestBad, 0.5, 0.999);
        }

        return new Prediction(label, prob, "yolo-onnx");
    }

    private static class Det {
        int cls; double score;
        Det(int c, double s){ cls=c; score=s; }
    }

    private int idx(String name){ return classes.indexOf(name); }

    private static int argmax(float[] a, int start){
        int m = start, i = start+1;
        for (; i<a.length; i++) if (a[i]>a[m]) m=i;
        return m - start;
    }

    private static double clamp(double v, double lo, double hi){
        return Math.max(lo, Math.min(hi, v));
    }

    private static float[] preprocess(BufferedImage src, int dstW, int dstH) {
        // Letterbox: escala manteniendo aspecto y rellena con 114
        int w = src.getWidth(), h = src.getHeight();
        double r = Math.min(dstW/(double)w, dstH/(double)h);
        int nw = (int)Math.round(w*r), nh = (int)Math.round(h*r);
        BufferedImage resized = new BufferedImage(dstW, dstH, BufferedImage.TYPE_3BYTE_BGR);
        var g = resized.createGraphics();
        g.setColor(new java.awt.Color(114,114,114));
        g.fillRect(0,0,dstW,dstH);
        int x = (dstW-nw)/2, y = (dstH-nh)/2;
        g.drawImage(src, x,y,nw,nh, null);
        g.dispose();

        // BGR->RGB y a CHW float [0..1]
        byte[] bgr = ((java.awt.image.DataBufferByte) resized.getRaster().getDataBuffer()).getData();
        float[] chw = new float[3*dstW*dstH];
        int pixels = dstW*dstH;
        for (int i=0, p=0; i<pixels; i++, p+=3) {
            float b = (bgr[p] & 0xff) / 255f;
            float g2 = (bgr[p+1] & 0xff) / 255f;
            float r2 = (bgr[p+2] & 0xff) / 255f;
            chw[i] = r2;
            chw[i + pixels] = g2;
            chw[i + 2*pixels] = b;
        }
        return chw;
    }

    private static float[][][] transpose84N_to_N84(float[][][] x) {
        int N = x[0][0].length;       // [1,84,N]
        int C = x[0].length;          // 84
        float[][][] out = new float[1][N][C];
        for (int n=0; n<N; n++) {
            for (int c=0; c<C; c++) out[0][n][c] = x[0][c][n];
        }
        return out;
    }

    @Override
    public void close() {
        try { session.close(); } catch (Exception ignored) {}
        try { env.close(); } catch (Exception ignored) {}
    }
}
