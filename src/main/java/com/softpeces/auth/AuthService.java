package com.softpeces.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

import com.softpeces.infra.Database;

public class AuthService {

    public static class Session {
        public final int userId;
        public final String username;
        public final boolean admin;
        public final boolean operador;

        public Session(int userId, String username, boolean admin, boolean operador) {
            this.userId = userId;
            this.username = username;
            this.admin = admin;
            this.operador = operador;
        }
    }

    /** Valida contra la tabla USERS (PASS_HASH = SHA-256 en seeds). */
    public Session login(String username, String password) throws Exception {
        System.out.println("Intento de login para usuario: " + username);
        if (username == null || password == null) {
            System.out.println("Error: Credenciales nulas");
            throw new Exception("Credenciales requeridas");
        }

        final String u = username.trim();    // usuario se normaliza
        final String p = password;           // ¡NO trim a la contraseña!

        System.out.println("Buscando usuario en la base de datos...");
        String sql = "SELECT ID, USERNAME, PASS_HASH, ACTIVE FROM USERS WHERE USERNAME=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Usuario no encontrado: " + u);
                    throw new Exception("Usuario o contraseña incorrectos");
                }

                int userId = rs.getInt("ID");
                String storedHash = rs.getString("PASS_HASH");
                int active = rs.getInt("ACTIVE");
                
                System.out.println("Usuario encontrado - ID: " + userId + ", Activo: " + (active == 1));
                System.out.println("Hash almacenado: " + storedHash);
                
                if (active != 1) {
                    System.out.println("Usuario inactivo: " + u);
                    throw new Exception("Usuario inactivo");
                }

                System.out.println("Verificando contraseña...");
                boolean passwordMatches = Passwords.matches(p, storedHash);
                System.out.println("¿Contraseña válida? " + passwordMatches);
                
                if (!passwordMatches) {
                    System.out.println("Contraseña incorrecta para el usuario: " + u);
                    throw new Exception("Usuario o contraseña incorrectos");
                }
                
                System.out.println("Inicio de sesión exitoso para: " + u);
                var roles = loadRoles(userId);
                boolean isAdmin = roles.contains("ADMIN");
                boolean isOperador = roles.contains("OPERADOR");
                return new Session(userId, u, isAdmin, isOperador);
            }
        }
    }

    private java.util.Set<String> loadRoles(int userId) throws Exception {
        String sql = """
      SELECT R.NAME
      FROM USER_ROLES UR
      JOIN ROLES R ON R.ID = UR.ROLE_ID
      WHERE UR.USER_ID=?
      """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                java.util.Set<String> out = new java.util.HashSet<>();
                while (rs.next()) out.add(rs.getString("NAME"));
                return out;
            }
        }
    }

    /** Utilidad de verificación flexible: SHA-256 (seed), BCrypt si está en classpath, o texto plano. */
    static final class Passwords {
        static boolean matches(String raw, String stored) {
            System.out.println("Comparando contraseña...");
            System.out.println("  Contraseña ingresada: " + raw);
            System.out.println("  Hash almacenado: " + stored);
            
            if (stored == null) {
                System.out.println("  Error: Hash almacenado es nulo");
                return false;
            }

            // 1) Si parece BCrypt ($2a/$2b/$2y) e HAY lib, la usamos por reflexión (no rompe si no está)
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                System.out.println("  Detectado formato BCrypt");
                try {
                    Class<?> bc = Class.forName("org.mindrot.jbcrypt.BCrypt");
                    var m = bc.getMethod("checkpw", String.class, String.class);
                    Object ok = m.invoke(null, raw, stored);
                    boolean result = ok instanceof Boolean b && b;
                    System.out.println("  Resultado verificación BCrypt: " + result);
                    return result;
                } catch (Throwable e) {
                    System.out.println("  Error al verificar BCrypt: " + e.getMessage());
                    return false; // es BCrypt pero no tenemos la lib
                }
            }

            // 2) SHA-256 en HEX (lo que pone DatabaseBootstrap al seedear)
            //    64 chars hexadecimales => comparamos contra SHA-256 de la contraseña digitada
            if (isHex64(stored)) {
                System.out.println("  Detectado formato SHA-256");
                String hashedInput = sha256Hex(raw);
                System.out.println("  Hash de entrada: " + hashedInput);
                System.out.println("  Hash almacenado: " + stored);
                boolean result = hashedInput.equalsIgnoreCase(stored);
                System.out.println("  ¿Coinciden los hashes? " + result);
                return result;
            }
            
            if (stored.startsWith("{SHA-256}")) { // por si algún día se usa con prefijo
                System.out.println("  Detectado formato {SHA-256}");
                String hex = stored.substring("{SHA-256}".length());
                String hashedInput = sha256Hex(raw);
                System.out.println("  Hash de entrada: " + hashedInput);
                System.out.println("  Hash almacenado (sin prefijo): " + hex);
                boolean result = hashedInput.equalsIgnoreCase(hex);
                System.out.println("  ¿Coinciden los hashes? " + result);
                return result;
            }

            // 3) Texto plano (último recurso)
            System.out.println("  Comparando texto plano");
            boolean result = stored.equals(raw);
            System.out.println("  ¿Coinciden las contraseñas en texto plano? " + result);
            return result;
        }

        private static boolean isHex64(String s) {
            if (s == null || s.length() != 64) return false;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                boolean hex = (ch >= '0' && ch <= '9')
                           || (ch >= 'a' && ch <= 'f')
                           || (ch >= 'A' && ch <= 'F');
                if (!hex) return false;
            }
            return true;
        }

        private static String sha256Hex(String s) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
                StringBuilder hex = new StringBuilder(out.length * 2);
                for (byte b : out) hex.append(String.format(Locale.ROOT, "%02x", b));
                return hex.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT ID, USERNAME, PASS_HASH, ACTIVE FROM USERS WHERE USERNAME=?";
        try (java.sql.Connection c = com.softpeces.infra.Database.get();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("ID"),
                            rs.getString("USERNAME"),
                            rs.getString("PASS_HASH"),
                            rs.getInt("ACTIVE")==1
                    );
                }
            }
            return null;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // DTO mínimo para este uso (o usa el que ya tengas)
    public static record User(int id, String username, String passHash, boolean active) {}

    public void updatePassword(int userId, String newPlain) {
        // Hasheo igual que el seed (SHA-256 HEX)
        String newHash;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(newPlain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(out.length * 2);
            for (byte b : out) hex.append(String.format(java.util.Locale.ROOT, "%02x", b));
            newHash = hex.toString();
        } catch (Exception e) { throw new RuntimeException(e); }

        try (java.sql.Connection c = com.softpeces.infra.Database.get();
             java.sql.PreparedStatement ps = c.prepareStatement("UPDATE USERS SET PASS_HASH=? WHERE ID=?")) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

}
