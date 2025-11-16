package com.softpeces.audit;

public record AuditRow(
        int id, String fechaHora, String username, String accion,
        String entidad, Integer entidadId, String detalle
) {}
