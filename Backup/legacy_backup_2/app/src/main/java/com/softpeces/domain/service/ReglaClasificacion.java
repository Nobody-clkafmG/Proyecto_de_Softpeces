package com.softpeces.domain.service;

import java.util.List;

import com.softpeces.domain.enums.ResultadoAptitud;
import com.softpeces.domain.model.Fotografia;
import com.softpeces.domain.model.Muestreo;

public interface ReglaClasificacion {
    ResultadoAptitud evaluar(List<Fotografia> fotos, List<Muestreo> datos);
}
