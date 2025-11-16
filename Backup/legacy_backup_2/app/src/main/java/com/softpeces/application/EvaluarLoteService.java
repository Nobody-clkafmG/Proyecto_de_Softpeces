package com.softpeces.application;

import java.util.UUID;

import com.softpeces.domain.enums.ResultadoAptitud;
import com.softpeces.domain.model.Evaluacion;
import com.softpeces.domain.model.Lote;
import com.softpeces.domain.repository.LoteRepository;
import com.softpeces.domain.service.ReglaClasificacion;

public class EvaluarLoteService {
    private final LoteRepository lotes;
    private final ReglaClasificacion regla;

    public EvaluarLoteService(LoteRepository lotes, ReglaClasificacion regla) {
        this.lotes = lotes; this.regla = regla;
    }

    public ResultadoAptitud ejecutar(UUID loteId) {
        Lote lote = lotes.byId(loteId);
        ResultadoAptitud res = regla.evaluar(lote.getFotografias(), lote.getMuestreos());
        lotes.addEvaluacion(loteId, new Evaluacion(res));
        return res;
    }
}
