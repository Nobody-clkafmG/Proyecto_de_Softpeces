package com.softpeces.repo;

import com.softpeces.domain.MuestreoRow;
import com.softpeces.infra.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MuestreoRepository {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<MuestreoRow> findByLote(int loteId) {
        String sql = """
      SELECT m.ID, m.LOTE_ID, m.ESTACION_ID, e.NOMBRE AS ESTACION_NOMBRE,
             m.TANQUE_ID, t.CODIGO AS TANQUE_CODIGO, m.FECHA_HORA
      FROM MUESTREO m
      JOIN ESTACION e ON e.ID = m.ESTACION_ID
      JOIN TANQUE t   ON t.ID = m.TANQUE_ID
      WHERE m.LOTE_ID=?
      ORDER BY m.ID DESC
      """;
        List<MuestreoRow> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, loteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new MuestreoRow(
                            rs.getInt("ID"), rs.getInt("LOTE_ID"),
                            rs.getInt("ESTACION_ID"), rs.getString("ESTACION_NOMBRE"),
                            rs.getInt("TANQUE_ID"), rs.getString("TANQUE_CODIGO"),
                            rs.getString("FECHA_HORA")
                    ));
                }
            }
            return out;
        } catch (Exception e) { throw new RuntimeException("Error listando muestreos", e); }
    }

    public void insert(int loteId, int estacionId, int tanqueId, LocalDateTime fechaHora) {
        String sql = "INSERT INTO MUESTREO(LOTE_ID,ESTACION_ID,TANQUE_ID,FECHA_HORA) VALUES (?,?,?,?)";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, loteId); ps.setInt(2, estacionId); ps.setInt(3, tanqueId);
            ps.setString(4, fechaHora.format(FMT));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error creando muestreo", e); }
    }

    public void delete(int id) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM MUESTREO WHERE ID=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error eliminando muestreo", e); }
    }
}
