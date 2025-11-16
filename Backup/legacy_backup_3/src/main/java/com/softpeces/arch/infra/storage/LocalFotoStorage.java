package com.softpeces.arch.infra.storage;

import java.io.IOException;
import java.nio.file.*;

public class LocalFotoStorage implements FotoStorage {
    private final Path basePath;

    public LocalFotoStorage(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public Path guardar(byte[] imagenBytes, String nombreArchivo) throws IOException {
        Files.createDirectories(basePath);
        Path target = basePath.resolve(nombreArchivo);
        Files.write(target, imagenBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return target;
    }

    @Override
    public boolean eliminar(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.delete(path);
            return true;
        }
        return false;
    }
}
