package com.softpeces.auth;


import com.softpeces.infra.Database;


import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HexFormat;


public class PasswordResetService {


    private static final SecureRandom RNG = new SecureRandom();


    public String generarToken(int userId, int minutosValidez) {
        byte[] b = new byte[16];
        RNG.nextBytes(b);
        String token = HexFormat.of().formatHex(b);
        String expira = LocalDateTime.now().plusMinutes(minutosValidez).toString();
        String sql = "INSERT INTO PASSWORD_RESET(USER_ID,TOKEN,EXPIRA) VALUES(?,?,?)";
        try (Connection cn = Database.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setString(3, expira);
            ps.executeUpdate();
            return token;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Integer validarToken(String token) {
        String sql = "SELECT USER_ID,EXPIRA FROM PASSWORD_RESET WHERE TOKEN=?";
        try (Connection cn = Database.get(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var exp = LocalDateTime.parse(rs.getString("EXPIRA"));
                    if (LocalDateTime.now().isBefore(exp)) {
                        return rs.getInt("USER_ID");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public void consumirToken(String token) {
        try (Connection cn = Database.get(); PreparedStatement ps = cn.prepareStatement("DELETE FROM PASSWORD_RESET WHERE TOKEN=?")) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}