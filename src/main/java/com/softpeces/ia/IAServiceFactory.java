package com.softpeces.ia;
import com.softpeces.infra.AppConfig;
import java.nio.file.*;

public final class IAServiceFactory {
    public static IAService create() {
        try {
            var path = java.nio.file.Path.of(com.softpeces.infra.AppConfig.s("ai.onnx.path","data/models/tilapia_y11n.onnx"));
            int size = com.softpeces.infra.AppConfig.i("ai.img_size",640);
            float conf = com.softpeces.infra.AppConfig.f("ai.confidence",0.25f);
            float iou  = com.softpeces.infra.AppConfig.f("ai.iou",0.50f);
            if (java.nio.file.Files.exists(path)) {
                System.out.println("[IA] ONNX " + path + " img=" + size + " conf=" + conf + " iou=" + iou);
                return new IAServiceOnnx(path, size, conf, iou);
            }
            System.out.println("[IA] STUB (no existe: " + path + ")");
        } catch (Exception e) {
            System.err.println("[IA] STUB por error ONNX: " + e.getMessage());
        }
        return new IAServiceStub();
    }
}

