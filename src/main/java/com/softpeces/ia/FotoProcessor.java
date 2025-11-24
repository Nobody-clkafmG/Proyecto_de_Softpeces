package com.softpeces.ia;

import com.softpeces.domain.Foto;
import com.softpeces.infra.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FotoProcessor {

    private final ExecutorService pool = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2)
    );
    private final IAService svc = IAServiceFactory.create();

    /**
     * Procesa una foto en background usando IA.
     * @param f     Foto (con ruta)
     * @param parte "AUTO", "OJO" o "BRANQUIAS"
     * @param onDone callback (UI) cuando termina
     */
    public void processAsync(Foto f, String parte, Runnable onDone) {
        pool.submit(() -> {
            try {
                processSync(f, parte);
            } finally {
                if (onDone != null) {
                    javafx.application.Platform.runLater(onDone);
                }
            }
        });
    }

    /** Procesamiento síncrono (útil para pruebas). */
    public void processSync(Foto f, String parte) {
        try {
            String parteStr = (parte == null || parte.isBlank()) ? "AUTO" : parte;
            IAService.Prediction p = svc.predict(f.ruta(), parteStr);

            updateOk(f.id(), p.label(), p.prob(), "");
        } catch (Exception ex) {
            updateErr(f.id(), ex.getMessage() == null ? "Error en IA" : ex.getMessage());
        }
    }

    // ======= Persistencia directa (evita depender de métodos desconocidos del repo) =======
    private void updateOk(int idFoto, String label, double prob, String msg) {
        String sql = "UPDATE FOTO SET ESTADO='CLASIFICADO', LABEL=?, PROB=?, MENSAJE_ERROR=? WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, label);
            ps.setDouble(2, prob);
            ps.setString(3, msg == null ? "" : msg);
            ps.setInt(4, idFoto);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateErr(int idFoto, String msg) {
        String sql = "UPDATE FOTO SET ESTADO='ERROR', MENSAJE_ERROR=? WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, msg == null ? "" : msg);
            ps.setInt(2, idFoto);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
