package com.softpeces.catalog;

import com.softpeces.infra.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TanqueRepository {

    public List<Tanque> findByEstacion(int estacionId) {
        String sql = "SELECT ID, ESTACION_ID, CODIGO, CAPACIDAD_L FROM TANQUE WHERE ESTACION_ID=? ORDER BY CODIGO";
        List<Tanque> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, estacionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(new Tanque(rs.getInt("ID"), rs.getInt("ESTACION_ID"),
                            rs.getString("CODIGO"), rs.getDouble("CAPACIDAD_L")));
            }
            return out;
        } catch (Exception e) { throw new RuntimeException("Error listando tanques", e); }
    }

    public Tanque insert(int estacionId, String codigo, double capacidadL) {
        String sql = "INSERT INTO TANQUE(ESTACION_ID,CODIGO,CAPACIDAD_L) VALUES (?,?,?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, estacionId);
            ps.setString(2, codigo.trim());
            ps.setDouble(3, capacidadL);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int id = rs.next() ? rs.getInt(1) : -1;
                return new Tanque(id, estacionId, codigo.trim(), capacidadL);
            }
        } catch (SQLException e) {
            if ((e.getMessage()+"").toLowerCase().contains("unique"))
                throw new RuntimeException("Ya existe un tanque con ese código en esta estación.");
            throw new RuntimeException("Error creando tanque", e);
        }
    }

    public void update(int id, String codigo, double capacidadL) {
        String sql = "UPDATE TANQUE SET CODIGO=?, CAPACIDAD_L=? WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            ps.setDouble(2, capacidadL);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            if ((e.getMessage()+"").toLowerCase().contains("unique"))
                throw new RuntimeException("Ya existe un tanque con ese código en esta estación.");
            throw new RuntimeException("Error actualizando tanque", e);
        }
    }

    public void delete(int id) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM TANQUE WHERE ID=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se puede eliminar el tanque (¿en uso?)", e);
        }
    }
}
