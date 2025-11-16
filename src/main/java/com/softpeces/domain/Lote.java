// src/main/java/com/softpeces/domain/Lote.java
package com.softpeces.domain;

public record Lote(int id, String nombre, LoteEstado estado,
                   String departamento, String municipio) {
    // constructor de compatibilidad (3 parámetros)
    public Lote(int id, String nombre, LoteEstado estado) {
        this(id, nombre, estado, null, null);
    }
}
