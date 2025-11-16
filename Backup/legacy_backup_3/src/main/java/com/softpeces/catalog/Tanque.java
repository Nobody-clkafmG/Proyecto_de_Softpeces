package com.softpeces.catalog;

import java.time.LocalDate;

public record Tanque(int id, int estacionId, String codigo, double capacidadL, String tipoPez, Integer pecesAprox, LocalDate fechaInicio) {}
