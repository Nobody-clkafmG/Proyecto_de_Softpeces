package com.softpeces.catalog;
import java.time.LocalDate;

public record TanqueLimpieza(
        int id, int tanqueId, LocalDate fecha,
        String responsable, String descripcion, String observaciones
) {}
