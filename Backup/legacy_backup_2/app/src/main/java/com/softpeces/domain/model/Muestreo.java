package com.softpeces.domain.model;

import java.util.Date;

public class Muestreo {
    private Date fechaHora;
    private Double temperaturaAguaC;
    private Double ph;
    private String comentarios;

    public Muestreo(Date fechaHora, Double temperaturaAguaC, Double ph, String comentarios) {
        this.fechaHora = fechaHora; this.temperaturaAguaC = temperaturaAguaC; this.ph = ph; this.comentarios = comentarios;
    }
}
