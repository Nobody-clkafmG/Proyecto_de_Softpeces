package com.softpeces.model;

import java.time.LocalDate;

public class Limpieza {
    private Integer id;
    private int tanqueId;
    private LocalDate fecha;
    private String responsable;
    private String descripcion;

    public Limpieza(Integer id, int tanqueId, LocalDate fecha, String responsable, String descripcion) {
        this.id = id;
        this.tanqueId = tanqueId;
        this.fecha = fecha;
        this.responsable = responsable;
        this.descripcion = descripcion;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getTanqueId() { return tanqueId; }
    public void setTanqueId(int tanqueId) { this.tanqueId = tanqueId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
