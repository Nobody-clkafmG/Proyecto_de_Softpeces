package com.softpeces.domain.model;

import java.util.UUID;

public class Usuario {
    private UUID id;
    private String nombre;
    public Usuario(UUID id, String nombre) { this.id = id; this.nombre = nombre; }
}
