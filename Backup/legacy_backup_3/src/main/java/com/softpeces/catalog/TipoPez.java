package com.softpeces.catalog;

import java.util.Arrays;
import java.util.List;

/**
 * Tipos de pez disponibles en el sistema
 */
public class TipoPez {
    public static final String TILAPIA = "Tilapia";
    public static final String CACHAMA = "Cachama";
    public static final String TRUCHA = "Trucha";

    /**
     * Lista de todos los tipos de pez disponibles
     */
    public static final List<String> TODOS = Arrays.asList(TILAPIA, CACHAMA, TRUCHA);

    private TipoPez() {
        // Clase utilitaria
    }
}
