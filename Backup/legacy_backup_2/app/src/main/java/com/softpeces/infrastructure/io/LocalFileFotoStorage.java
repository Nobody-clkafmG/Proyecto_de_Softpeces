package com.softpeces.infrastructure.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.softpeces.domain.repository.FotoStorage;

public class LocalFileFotoStorage implements FotoStorage {
    private final Path basePath;
    public LocalFileFotoStorage(Path basePath) { this.basePath = basePath; }

    @Override public String guardar(byte[] img, UUID loteId) {
        try {
            Path dir = basePath.resolve(loteId.toString());
            Files.createDirectories(dir);
            Path file = dir.resolve(System.currentTimeMillis()+".jpg");
            Files.write(file, img == null ? new byte[0] : img);
            return file.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
