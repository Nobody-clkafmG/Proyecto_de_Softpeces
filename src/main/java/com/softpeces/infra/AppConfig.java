package com.softpeces.infra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class AppConfig {
    private static final Properties P = new Properties();

    static {
    Path p = Path.of("data", "app.properties");
    System.out.println("Loading properties from: " + p.toAbsolutePath());
    if (Files.exists(p)) {
        try (InputStream in = Files.newInputStream(p)) {
            P.load(in);
            System.out.println("Successfully loaded properties: " + P);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
            e.printStackTrace();
        }
    } else {
        System.err.println("Properties file not found at: " + p.toAbsolutePath());
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

    public static boolean b(String key, boolean def) {
        String v = P.getProperty(key);
        if (v == null) return def;
        return switch (v.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> def;
        };
    }
}
