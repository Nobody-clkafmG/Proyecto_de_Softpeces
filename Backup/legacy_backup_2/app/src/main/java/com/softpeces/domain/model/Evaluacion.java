package com.softpeces.domain.model;

import java.util.Date;
import java.util.UUID;

import com.softpeces.domain.enums.ResultadoAptitud;

public class Evaluacion {
    private UUID id = UUID.randomUUID();
    private Date fecha = new Date();
    private ResultadoAptitud resultado;
    private String observaciones;

    public Evaluacion(ResultadoAptitud resultado) { this.resultado = resultado; }
}
