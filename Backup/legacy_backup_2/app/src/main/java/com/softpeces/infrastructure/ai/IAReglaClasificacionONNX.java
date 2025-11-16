package com.softpeces.infrastructure.ai;

import java.util.List;

import com.softpeces.domain.enums.ResultadoAptitud;
import com.softpeces.domain.model.Fotografia;
import com.softpeces.domain.model.Muestreo;
import com.softpeces.domain.service.ReglaClasificacion;

public class IAReglaClasificacionONNX implements ReglaClasificacion {
    private final ModeloProvider provider;
    private final PreprocesadorImagen pre;
    private final PostprocesadorSalida post;

    public IAReglaClasificacionONNX(ModeloProvider p, PreprocesadorImagen pre, PostprocesadorSalida post) {
        this.provider = p; this.pre = pre; this.post = post;
    }

    @Override
    public ResultadoAptitud evaluar(List<Fotografia> fotos, List<Muestreo> datos) {
        // stub: siempre APTA
        return ResultadoAptitud.APTA;
    }
}
