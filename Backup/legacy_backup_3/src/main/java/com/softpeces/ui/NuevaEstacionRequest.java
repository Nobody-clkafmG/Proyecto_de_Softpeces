package com.softpeces.ui;

public record NuevaEstacionRequest(
        String sitio,
        String encargado,
        String geo,
        int cantidadTanques
) {}
