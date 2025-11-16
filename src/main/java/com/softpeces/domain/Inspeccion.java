package com.softpeces.domain;

public record Inspeccion(
        Integer id,
        int muestreoId,
        int olor,        // 1..5
        int textura,     // 1..5
        int color,       // 1..5
        String comentarios,
        String fecha     // generado por la BD
) {}
