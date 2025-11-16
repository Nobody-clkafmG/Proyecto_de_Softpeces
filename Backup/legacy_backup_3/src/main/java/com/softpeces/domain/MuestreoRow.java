package com.softpeces.domain;

// Vista (JOIN) para mostrar nombres/códigos en la tabla
public record MuestreoRow(
        int id, int loteId,
        int estacionId, String estacionNombre,
        int tanqueId, String tanqueCodigo,
        String fechaHora
) {}
