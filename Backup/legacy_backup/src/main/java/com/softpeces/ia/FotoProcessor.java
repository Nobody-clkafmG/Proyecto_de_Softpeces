package com.softpeces.ia;

import com.softpeces.audit.Audit;
import com.softpeces.domain.*;
import com.softpeces.repo.FotoRepository;

import java.util.concurrent.*;

public class FotoProcessor {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final IAService ia = new IAServiceStub();
    private final FotoRepository repo = new FotoRepository();

    public void processAsync(Foto foto, Runnable onDone) {
        exec.submit(() -> {
            try {
                repo.updateEstado(foto.id(), EstadoFoto.PROCESANDO, null, null, null);
                var pred = ia.predict(foto.ruta(), foto.parte().name());
                repo.updateEstado(foto.id(), EstadoFoto.CLASIFICADO, pred.label(), pred.prob(), null);
                Audit.log("Clasificar", "FOTO", foto.id(), "label="+pred.label()+" prob="+pred.prob());
            } catch (Exception e) {
                repo.updateEstado(foto.id(), EstadoFoto.ERROR, null, null, e.getMessage());
                Audit.log("ClasificarError", "FOTO", foto.id(), e.getMessage());
            } finally {
                if (onDone != null) onDone.run();
            }
        });
    }

    public void shutdown() { exec.shutdownNow(); }
}
