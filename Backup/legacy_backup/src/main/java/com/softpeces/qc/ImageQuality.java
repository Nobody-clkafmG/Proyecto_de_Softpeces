package com.softpeces.qc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public final class ImageQuality {

    public static record QCResult(boolean ok, String reason, double focus, double brightness) {}

    public static QCResult check(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) return new QCResult(false, "No es una imagen válida", 0, 0);

            int w = img.getWidth(), h = img.getHeight();
            if (w < 640 || h < 480) return new QCResult(false, "Resolución < 640x480", 0, 0);

            // Brillo: promedio de luma (0..255)
            double bri = avgBrightness(img);

            // Enfoque: varianza del Laplaciano aproximado (no requiere OpenCV)
            double foc = varianceOfLaplacian(img);

            boolean ok = (bri >= 60 && bri <= 200) && (foc >= 100); // umbrales básicos
            String reason = ok ? "OK" :
                    (bri < 60 ? "Muy oscura" : (bri > 200 ? "Muy brillante" : "Desenfoque"));
            return new QCResult(ok, reason, foc, bri);
        } catch (Exception e) {
            return new QCResult(false, "Error QC: " + e.getMessage(), 0, 0);
        }
    }

    private static double avgBrightness(BufferedImage img) {
        long sum = 0; int n = 0;
        for (int y=0; y<img.getHeight(); y+=2) {
            for (int x=0; x<img.getWidth(); x+=2) {
                int rgb = img.getRGB(x, y);
                int r = (rgb>>16)&0xff, g = (rgb>>8)&0xff, b = rgb&0xff;
                int luma = (int)(0.299*r + 0.587*g + 0.114*b);
                sum += luma; n++;
            }
        }
        return sum / (double) n;
    }

    private static double varianceOfLaplacian(BufferedImage img) {
        // Conv. 3x3 Laplaciano | aprox en escala de grises
        int w = img.getWidth(), h = img.getHeight();
        int[] gray = new int[w*h];
        for (int y=0; y<h; y++) for (int x=0; x<w; x++) {
            int rgb = img.getRGB(x,y);
            int r=(rgb>>16)&0xff, g=(rgb>>8)&0xff, b=rgb&0xff;
            gray[y*w+x] = (r*299 + g*587 + b*114)/1000;
        }
        double[] lap = new double[(w-2)*(h-2)];
        int idx=0;
        for (int y=1; y<h-1; y++) for (int x=1; x<w-1; x++) {
            int c = gray[y*w+x]*4
                    - gray[y*w + (x-1)] - gray[y*w + (x+1)]
                    - gray[(y-1)*w + x]  - gray[(y+1)*w + x];
            lap[idx++] = c;
        }
        double mean = 0; for (double v: lap) mean += v; mean /= lap.length;
        double var = 0; for (double v: lap) { double d=v-mean; var += d*d; }
        return var / lap.length;
    }
}
