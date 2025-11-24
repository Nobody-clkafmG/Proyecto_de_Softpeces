package com.softpeces.auth;

import com.softpeces.infra.Database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

public class UserRepository {

    public static record User(int id, String username, String email, String passHash, boolean active) {}
    public static record UserRow(int id, String username, String email, String passHash, boolean active, List<String> roles) {}

    public User findByUsername(String username) {
        String sql = "SELECT ID, USERNAME, EMAIL, PASS_HASH, ACTIVE FROM USERS WHERE USERNAME=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("ID"),
                            rs.getString("USERNAME"),
                            rs.getString("EMAIL"),
                            rs.getString("PASS_HASH"),
                            rs.getInt("ACTIVE") == 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando usuario", e);
        }
        return null;
    }

    public List<UserRow> findAll() {
        String sql = """
      SELECT U.ID, U.USERNAME, U.EMAIL, U.PASS_HASH, U.ACTIVE,
             GROUP_CONCAT(DISTINCT R.NAME) AS ROLES
      FROM USERS U
      LEFT JOIN USER_ROLES UR ON UR.USER_ID = U.ID
      LEFT JOIN ROLES R ON R.ID = UR.ROLE_ID
      GROUP BY U.ID, U.USERNAME, U.EMAIL, U.PASS_HASH, U.ACTIVE
      ORDER BY U.USERNAME
      """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<UserRow> out = new ArrayList<>();
            while (rs.next()) {
                String csv = rs.getString("ROLES");
                List<String> roles = csv == null || csv.isBlank()
                        ? Collections.emptyList()
                        : Arrays.stream(csv.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();
                out.add(new UserRow(
                        rs.getInt("ID"),
                        rs.getString("USERNAME"),
                        rs.getString("EMAIL"),
                        rs.getString("PASS_HASH"),
                        rs.getInt("ACTIVE") == 1,
                        roles
                ));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Error listando usuarios", e);
        }
    }

    public void createUser(String username, String email, String rawPassword,
                           boolean admin, boolean operador, boolean inspector, boolean active) {
        String user = username == null ? "" : username.trim();
        if (user.isEmpty()) throw new IllegalArgumentException("Usuario requerido");
        String hash = sha256(rawPassword == null ? "" : rawPassword);
        String correo = email == null || email.isBlank() ? null : email.trim();

        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            int userId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO USERS(USERNAME,EMAIL,PASS_HASH,ACTIVE) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user);
                ps.setString(2, correo);
                ps.setString(3, hash);
                ps.setInt(4, active ? 1 : 0);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    userId = keys.next() ? keys.getInt(1) : fetchUserId(c, user);
                }
            }

            updateRolesInternal(c, userId, admin, operador, inspector);
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error creando usuario", e);
        }
    }

    public void resetPassword(int userId, String newPassword) {
        String hash = sha256(newPassword == null ? "" : newPassword);
        String sql = "UPDATE USERS SET PASS_HASH=? WHERE ID=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error reseteando password", e);
        }
    }

    public void setActive(int userId, boolean active) {
        String sql = "UPDATE USERS SET ACTIVE=? WHERE ID=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando estado", e);
        }
    }

    public void updateRoles(int userId, boolean admin, boolean operador, boolean inspector) {
        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            updateRolesInternal(c, userId, admin, operador, inspector);
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando roles", e);
        }
    }

    public void updateUser(int userId, String username, String email, boolean active) {
        String user = username == null ? "" : username.trim();
        if (user.isEmpty()) throw new IllegalArgumentException("Usuario requerido");
        String correo = email == null || email.isBlank() ? null : email.trim();
        String sql = "UPDATE USERS SET USERNAME=?, EMAIL=?, ACTIVE=? WHERE ID=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user);
            if (correo == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, correo);
            }
            ps.setInt(3, active ? 1 : 0);
            ps.setInt(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando usuario", e);
        }
    }

    private void updateRolesInternal(Connection c, int userId, boolean admin, boolean operador, boolean inspector) throws SQLException {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM USER_ROLES WHERE USER_ID=?")) {
            del.setInt(1, userId);
            del.executeUpdate();
        }

        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO USER_ROLES(USER_ID,ROLE_ID) VALUES (?, (SELECT ID FROM ROLES WHERE NAME=?))")) {
            if (admin) {
                ins.setInt(1, userId);
                ins.setString(2, "ADMIN");
                ins.executeUpdate();
            }
            if (operador) {
                ins.setInt(1, userId);
                ins.setString(2, "OPERADOR");
                ins.executeUpdate();
            }
            if (inspector) {
                ins.setInt(1, userId);
                ins.setString(2, "INSPECTOR");
                ins.executeUpdate();
            }
        }
    }

    private int fetchUserId(Connection c, String username) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT ID FROM USERS WHERE USERNAME=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Usuario no encontrado tras insertar");
            }
        }
    }

    public boolean verifyPassword(String plain, String hashStored) {
        return sha256(plain).equalsIgnoreCase(hashStored);
    }

    public boolean hasRole(int userId, String role) {
        String sql = """
      SELECT 1
      FROM USER_ROLES ur
      JOIN ROLES r ON r.ID = ur.ROLE_ID
      WHERE ur.USER_ID=? AND r.NAME=?
      """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, role);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b: out) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
