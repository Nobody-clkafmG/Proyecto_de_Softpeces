package com.softpeces.arch.infra.storage;

import java.io.IOException;
import java.nio.file.Path;

public interface FotoStorage {
    Path guardar(byte[] imagenBytes, String nombreArchivo) throws IOException;
    boolean eliminar(Path path) throws IOException;
}
