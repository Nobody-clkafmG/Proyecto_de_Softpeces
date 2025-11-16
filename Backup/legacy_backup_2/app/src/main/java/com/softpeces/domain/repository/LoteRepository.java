package com.softpeces.domain.repository;

import java.util.List;
import java.util.UUID;

import com.softpeces.domain.model.Evaluacion;
import com.softpeces.domain.model.Fotografia;
import com.softpeces.domain.model.Lote;
import com.softpeces.domain.model.Muestreo;

public interface LoteRepository {
    Lote byId(UUID id);
    List<Lote> byTanque(UUID tanqueId);
    UUID save(Lote lote);
    void addMuestreo(UUID loteId, Muestreo m);
    void addFoto(UUID loteId, Fotografia f);
    void addEvaluacion(UUID loteId, Evaluacion e);
}
