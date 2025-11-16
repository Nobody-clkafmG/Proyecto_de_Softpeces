package com.softpeces.domain;

public record Foto(
        int id,
        int muestreoId,
        Parte parte,
        String ruta,
        boolean qcOk,
        EstadoFoto estado,
        String label,
        Double prob,
        String mensajeError
) {}
