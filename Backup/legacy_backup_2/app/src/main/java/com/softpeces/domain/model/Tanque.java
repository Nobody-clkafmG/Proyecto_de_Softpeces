package com.softpeces.domain.model;

import java.util.UUID;

public class Tanque {
    private UUID id;
    private String codigo;
    private double capacidadLitros;

    public Tanque(UUID id, String codigo, double capacidadLitros) {
        this.id = id; this.codigo = codigo; this.capacidadLitros = capacidadLitros;
    }
}
