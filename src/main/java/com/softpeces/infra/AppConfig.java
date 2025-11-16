package com.softpeces.infra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private static final Properties P = new Properties();

    static {
        Path p = Path.of("data", "app.properties");
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) { // ← bloque requerido
                P.load(in);
            } catch (IOException e) {
                // si quieres, loguéalo a bitácora
                e.printStackTrace();
            }
        }
    }

    private AppConfig() {}

    public static String s(String key, String def) {
        return P.getProperty(key, def);
    }

    public static int i(String key, int def) {
        String v = P.getProperty(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException ignore) { return def; }
    }

    public static float f(String key, float def) {
        String v = P.getProperty(key);
        if (v == null) return def;
        try { return Float.parseFloat(v.trim()); }
        catch (NumberFormatException ignore) { return def; }
    }
}
