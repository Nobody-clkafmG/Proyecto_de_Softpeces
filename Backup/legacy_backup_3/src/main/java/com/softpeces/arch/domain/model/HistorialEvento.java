package com.softpeces.arch.domain.model;

import java.time.LocalDateTime;

public record HistorialEvento(LocalDateTime fechaHora, String usuario, String accion, String detalle) {}
