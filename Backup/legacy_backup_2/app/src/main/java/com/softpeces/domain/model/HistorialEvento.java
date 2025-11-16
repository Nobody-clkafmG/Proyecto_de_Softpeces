package com.softpeces.domain.model;

import java.util.Date;

public class HistorialEvento {
    private Date fecha;
    private Usuario usuario;
    private String tipo;
    private String detalle;

    public HistorialEvento(Date fecha, Usuario usuario, String tipo, String detalle) {
        this.fecha = fecha; this.usuario = usuario; this.tipo = tipo; this.detalle = detalle;
    }
}
