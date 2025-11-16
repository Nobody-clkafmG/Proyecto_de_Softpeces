package com.softpeces.application;

import java.util.UUID;

import com.softpeces.domain.model.Fotografia;
import com.softpeces.domain.model.Muestreo;
import com.softpeces.domain.repository.FotoStorage;
import com.softpeces.domain.repository.LoteRepository;

public class RegistrarMuestreoConFoto {
    private final LoteRepository lotes;
    private final FotoStorage fotos;

    public RegistrarMuestreoConFoto(LoteRepository lotes, FotoStorage fotos) {
        this.lotes = lotes; this.fotos = fotos;
    }

    public void ejecutar(UUID loteId, Muestreo m, byte[] imagen) {
        lotes.addMuestreo(loteId, m);
        String ruta = fotos.guardar(imagen, loteId);
        Fotografia f = new Fotografia(ruta);
        lotes.addFoto(loteId, f);
    }
}
