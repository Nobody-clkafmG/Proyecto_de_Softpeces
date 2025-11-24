package com.softpeces.ui;

public record NuevaEstacionRequest(
        String sitio,
        String encargado,
        String geo,
        int cantidadTanques,
        Integer altitud,
        Double areaM2,
        String fuenteAgua
) {}
