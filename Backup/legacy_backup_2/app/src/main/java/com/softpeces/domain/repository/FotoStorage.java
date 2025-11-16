package com.softpeces.domain.repository;

import java.util.UUID;

public interface FotoStorage {
    String guardar(byte[] img, UUID loteId);
}
