package com.softpeces.domain.model;

import java.util.Date;
import java.util.UUID;

public class Fotografia {
    private UUID id = UUID.randomUUID();
    private String ruta;
    private Date tomadaEn = new Date();

    public Fotografia(String ruta) { this.ruta = ruta; }
}
