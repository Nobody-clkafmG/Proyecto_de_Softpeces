package com.softpeces.catalog;

import com.softpeces.infra.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstacionRepository {

    public List<Estacion> findAll() {
        String sql = "SELECT ID, NOMBRE FROM ESTACION ORDER BY NOMBRE";
        List<Estacion> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new Estacion(rs.getInt("ID"), rs.getString("NOMBRE")));
            return out;
        } catch (Exception e) { throw new RuntimeException("Error listando estaciones", e); }
    }

    public Estacion insert(String nombre) {
        String sql = "INSERT INTO ESTACION(NOMBRE) VALUES (?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int id = rs.next() ? rs.getInt(1) : -1;
                return new Estacion(id, nombre.trim());
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))
                throw new RuntimeException("Ya existe una estación con ese nombre.");
            throw new RuntimeException("Error creando estación", e);
        }
    }

    public void update(int id, String nombre) {
        String sql = "UPDATE ESTACION SET NOMBRE=? WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre.trim()); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))
                throw new RuntimeException("Ya existe una estación con ese nombre.");
            throw new RuntimeException("Error actualizando estación", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM ESTACION WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("No se puede eliminar (¿usada por tanques?)", e);
        }
    }
}
