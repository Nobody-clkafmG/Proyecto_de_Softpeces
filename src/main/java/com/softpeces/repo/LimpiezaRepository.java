package com.softpeces.repo;

import com.softpeces.infra.Database;
import com.softpeces.model.Limpieza;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LimpiezaRepository {

    public List<Limpieza> findByTanque(int tanqueId) {
        String sql = """
            SELECT ID, TANQUE_ID, FECHA, RESPONSABLE, DESCRIPCION
            FROM TANQUE_LIMPIEZA
            WHERE TANQUE_ID = ?
            ORDER BY DATE(FECHA) DESC, ID DESC
            """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tanqueId);
            List<Limpieza> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("ID");
                    int tId = rs.getInt("TANQUE_ID");
                    LocalDate fecha = LocalDate.parse(rs.getString("FECHA"));
                    String resp = rs.getString("RESPONSABLE");
                    String desc = rs.getString("DESCRIPCION");
                    list.add(new Limpieza(id, tId, fecha, resp, desc));
                }
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM TANQUE_LIMPIEZA WHERE ID = ?";
        try (var c = com.softpeces.infra.Database.get();
             var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void insert(Limpieza l) {
        String sql = """
            INSERT INTO TANQUE_LIMPIEZA (TANQUE_ID, FECHA, RESPONSABLE, DESCRIPCION)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, l.getTanqueId());
            ps.setString(2, l.getFecha().toString()); // ISO-8601 yyyy-MM-dd
            ps.setString(3, l.getResponsable());
            ps.setString(4, l.getDescripcion());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
