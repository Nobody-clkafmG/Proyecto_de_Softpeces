package com.softpeces.reports;

public record ReportRow(
        int muestreoId, String fechaHora,
        String loteNombre,
        String estacionNombre, String tanqueCodigo,
        String parte, String ruta,
        String qc, String estado,
        String label, Double prob
) {}
