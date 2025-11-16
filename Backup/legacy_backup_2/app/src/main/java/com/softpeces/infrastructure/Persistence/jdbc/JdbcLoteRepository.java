package com.softpeces.infrastructure.persistence.jdbc;

import java.util.List;
import java.util.UUID;

import com.softpeces.domain.model.Evaluacion;
import com.softpeces.domain.model.Fotografia;
import com.softpeces.domain.model.Lote;
import com.softpeces.domain.model.Muestreo;
import com.softpeces.domain.model.Tanque;
import com.softpeces.domain.repository.LoteRepository;

public class JdbcLoteRepository implements LoteRepository {
    public JdbcLoteRepository(Object jdbcTemplate) { /* placeholder */ }

    @Override public Lote byId(UUID id) { return new Lote(id, "L-001", new Tanque(UUID.randomUUID(), "T-1", 1000)); }
    @Override public List<Lote> byTanque(UUID tanqueId) { return List.of(); }
    @Override public UUID save(Lote lote) { return lote.getId(); }
    @Override public void addMuestreo(UUID loteId, Muestreo m) { }
    @Override public void addFoto(UUID loteId, Fotografia f) { }
    @Override public void addEvaluacion(UUID loteId, Evaluacion e) { }
}
