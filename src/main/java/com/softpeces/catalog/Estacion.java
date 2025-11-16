package com.softpeces.catalog;


import java.time.LocalDate;


public record Estacion(
        int id,
        String nombre,
        String encargado,
        String geoUbicacion,
        String tipoPez,
        Double litrosAprox,
        Integer pecesAprox,
        LocalDate fechaInicio,
        Integer cantidadTanques,
// nuevos
        Integer altitud,
        Double areaM2,
        String fuenteAgua
) {}