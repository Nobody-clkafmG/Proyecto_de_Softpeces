package com.softpeces.arch.api;

import java.nio.file.Path;

import com.softpeces.arch.application.EvaluarLoteService;
import com.softpeces.arch.application.RegistrarMuestreoConFoto;
import com.softpeces.domain.Parte;

public class UIController {
    private final EvaluarLoteService evaluar = new EvaluarLoteService();
    private final RegistrarMuestreoConFoto registrar = new RegistrarMuestreoConFoto();

    public String evaluarLote(int loteId) {
        return evaluar.evaluarAptitud(loteId);
    }

    // Firma por IDs (alineada a la BD actual)
    public void registrarMuestreo(int loteId, int estacionId, int tanqueId, Path rutaFoto, Parte parte) {
        registrar.registrar(loteId, estacionId, tanqueId, rutaFoto, parte);
    }
}
