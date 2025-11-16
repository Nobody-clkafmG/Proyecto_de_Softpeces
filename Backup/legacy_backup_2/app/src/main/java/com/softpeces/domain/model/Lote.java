package com.softpeces.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.softpeces.domain.enums.EstadoLote;

public class Lote {
    private UUID id;
    private String codigo;
    private EstadoLote estado;
    private Date fechaCreacion;
    private Tanque tanque;

    private final List<Muestreo> muestreos = new ArrayList<>();
    private final List<Fotografia> fotografias = new ArrayList<>();
    private final List<Evaluacion> evaluaciones = new ArrayList<>();
    private final List<HistorialEvento> eventos = new ArrayList<>();

    public Lote(UUID id, String codigo, Tanque tanque) {
        this.id = id;
        this.codigo = codigo;
        this.tanque = tanque;
        this.estado = EstadoLote.EN_ENGORDE;
        this.fechaCreacion = new Date();
    }

    public UUID getId() { return id; }
    public List<Muestreo> getMuestreos() { return Collections.unmodifiableList(muestreos); }
    public List<Fotografia> getFotografias() { return Collections.unmodifiableList(fotografias); }
    public List<Evaluacion> getEvaluaciones() { return Collections.unmodifiableList(evaluaciones); }

    public void registrarMuestreo(Muestreo m) { muestreos.add(m); }
    public void agregarFotografia(Fotografia f) { fotografias.add(f); }
    public void agregarEvaluacion(Evaluacion e) { evaluaciones.add(e); }
    public void cerrar(Usuario por, String motivo) {
        this.estado = EstadoLote.CERRADO;
        eventos.add(new HistorialEvento(new Date(), por, "CIERRE", motivo));
    }
}
