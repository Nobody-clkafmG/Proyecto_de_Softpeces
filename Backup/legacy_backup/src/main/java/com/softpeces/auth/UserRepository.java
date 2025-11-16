package com.softpeces.auth;

import com.softpeces.infra.Database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

public class UserRepository {

    public static record User(int id, String username, String passHash, boolean active) {}

    public User findByUsername(String username) {
        String sql = "SELECT ID, USERNAME, PASS_HASH, ACTIVE FROM USERS WHERE USERNAME=?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("ID"),
                            rs.getString("USERNAME"),
                            rs.getString("PASS_HASH"),
                            rs.getInt("ACTIVE") == 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando usuario", e);
        }
        return null;
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
