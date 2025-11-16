package com.softpeces.arch.infra.persistence;

import java.util.List;
import java.util.Optional;

import com.softpeces.domain.Lote;
import com.softpeces.repo.LoteRepository;

/** Adapter a nombres del diagrama, delegando al repo actual. */
public class JdbcLoteRepository {
    private final LoteRepository delegate = new LoteRepository();

    public Optional<Lote> byId(int id) {
        return delegate.findAll().stream().filter(l -> l.id() == id).findFirst();
    }

    public List<Lote> listar() {
        return delegate.findAll();
    }
}

