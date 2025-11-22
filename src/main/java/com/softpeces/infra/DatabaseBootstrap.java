package com.softpeces.infra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.stream.Collectors;

public final class DatabaseBootstrap {

    private DatabaseBootstrap() {}

    public static void init() {
        try (Connection c = Database.get()) {
            System.out.println("Inicializando base de datos...");
            System.out.println("Ruta de la base de datos: " + Database.getJdbcUrl());

            // 1) Aplica SIEMPRE el schema (idempotente: solo crea lo que falte).
            System.out.println("Aplicando schema.sql (idempotente)...");
            runSchema(c);

            // 2) Verifica las tablas mínimas que la app usa hoy.
            System.out.println("Verificando tablas requeridas...");
            if (!tablesExist(c)) {
                throw new IllegalStateException("Faltan tablas tras aplicar schema.sql");
            }

            // 3) Semillas (roles/usuarios) también idempotentes.
            System.out.println("Insertando datos base (roles/usuarios)...");
            seed(c);

            System.out.println("DB OK: esquema verificado correctamente");
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos:");
            e.printStackTrace();
            throw new RuntimeException("Error inicializando DB", e);
        }
    }

    /**
     * Verifica solo las tablas que realmente usa la app hoy.
     * Añade aquí cualquier tabla nueva que sea obligatoria.
     */
    private static boolean tablesExist(Connection c) throws SQLException {
        String[] requiredTables = {
                "ESTACION", "TANQUE",
                "USERS", "ROLES", "USER_ROLES",
                "TANQUE_LIMPIEZA"   // <-- NUEVA
        };
        for (String table : requiredTables) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?")) {
                ps.setString(1, table);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Ejecuta todas las sentencias del schema.sql separadas por ';'
     * (omitiendo líneas vacías).
     */
    private static void runSchema(Connection c) throws Exception {
        String sql = readResource("schema.sql");
        for (String stmt : sql.split(";")) {
            String s = stmt.trim();
            if (s.isEmpty()) continue;
            try (PreparedStatement ps = c.prepareStatement(s)) {
                ps.execute();
            }
        }
    }

    /**
     * Semillas idempotentes: inserta roles y usuarios si no existen.
     * Usuarios por defecto: admin / oper (hash SHA-256).
     */
    private static void seed(Connection c) throws Exception {
        // Roles
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT OR IGNORE INTO ROLES(NAME) VALUES (?)")) {
            ps.setString(1, "ADMIN"); ps.execute();
            ps.setString(1, "OPERADOR"); ps.execute();
            ps.setString(1, "INSPECTOR"); ps.execute();
        }

        int adminRoleId     = idForRole(c, "ADMIN");
        int operadorRoleId  = idForRole(c, "OPERADOR");
        int inspectorRoleId = idForRole(c, "INSPECTOR");

        // Usuarios (password temporal)
        insertUserIfMissing(c, "admin",     sha256("admin"),     adminRoleId);
        insertUserIfMissing(c, "oper",      sha256("oper"),      operadorRoleId);
        insertUserIfMissing(c, "inspector", sha256("inspector"), inspectorRoleId);
    }

    private static int idForRole(Connection c, String name) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT ID FROM ROLES WHERE NAME=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private static void insertUserIfMissing(Connection c, String user, String hash, int roleId) throws Exception {
        // Crea usuario si no existe
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT OR IGNORE INTO USERS(USERNAME, PASS_HASH, ACTIVE) VALUES (?,?,1)")) {
            ps.setString(1, user);
            ps.setString(2, hash);
            ps.execute();
        }
        // Relación rol (si no existe)
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT OR IGNORE INTO USER_ROLES(USER_ID, ROLE_ID) " +
                        "SELECT U.ID, ? FROM USERS U WHERE U.USERNAME=?")) {
            ps.setInt(1, roleId);
            ps.setString(2, user);
            ps.execute();
        }
    }

    private static String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : out) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    private static String readResource(String name) throws IOException {
        try (InputStream in = DatabaseBootstrap.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) throw new FileNotFoundException("No se encontró " + name);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
