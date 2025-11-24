package com.softpeces.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SoftwarePiscicultura {
    private final StringProperty nombre;
    private final StringProperty licencia;
    private final StringProperty usaIA;
    private final StringProperty enfoque;
    private final StringProperty descripcion;
    private final StringProperty enlace;

    public SoftwarePiscicultura(String nombre, String licencia, String usaIA, String enfoque, String descripcion, String enlace) {
        this.nombre = new SimpleStringProperty(nombre);
        this.licencia = new SimpleStringProperty(licencia);
        this.usaIA = new SimpleStringProperty(usaIA);
        this.enfoque = new SimpleStringProperty(enfoque);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.enlace = new SimpleStringProperty(enlace);
    }

    // Getters para las propiedades
    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }

    public String getLicencia() { return licencia.get(); }
    public StringProperty licenciaProperty() { return licencia; }

    public String getUsaIA() { return usaIA.get(); }
    public StringProperty usaIAProperty() { return usaIA; }

    public String getEnfoque() { return enfoque.get(); }
    public StringProperty enfoqueProperty() { return enfoque; }

    public String getDescripcion() { return descripcion.get(); }
    public StringProperty descripcionProperty() { return descripcion; }

    public String getEnlace() { return enlace.get(); }
    public StringProperty enlaceProperty() { return enlace; }
}
