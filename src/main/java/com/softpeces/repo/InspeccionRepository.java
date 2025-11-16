package com.softpeces.repo;

import com.softpeces.domain.Inspeccion;
import com.softpeces.infra.Database;

import java.sql.*;
import java.util.Optional;

public class InspeccionRepository {

    public long insert(Inspeccion i) throws SQLException {
        String sql = """
            INSERT INTO INSPECCION(MUESTREO_ID, OLOR, TEXTURA, COLOR, COMENTARIOS)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.muestreoId());
            ps.setInt(2, i.olor());
            ps.setInt(3, i.textura());
            ps.setInt(4, i.color());
            ps.setString(5, i.comentarios());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    public Optional<Inspeccion> findUltimaPorMuestreo(int muestreoId) throws SQLException {
        String sql = "SELECT * FROM INSPECCION WHERE MUESTREO_ID=? ORDER BY ID DESC LIMIT 1";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, muestreoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    private static Inspeccion map(ResultSet rs) throws SQLException {
        return new Inspeccion(
                rs.getInt("ID"),
                rs.getInt("MUESTREO_ID"),
                rs.getInt("OLOR"),
                rs.getInt("TEXTURA"),
                rs.getInt("COLOR"),
                rs.getString("COMENTARIOS"),
                rs.getString("FECHA")
        );
    }
}
