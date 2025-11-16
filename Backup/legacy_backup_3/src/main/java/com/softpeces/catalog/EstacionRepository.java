package com.softpeces.catalog;

import com.softpeces.infra.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EstacionRepository {

    public List<Estacion> findAll() {
        String sql = "SELECT ID, NOMBRE, ENCARGADO, GEOUBICACION, TIPO_PEZ, " +
                "LITROS_APROX, PECES_APROX, FECHA_INICIO, CANTIDAD_TANQUES " +
                "FROM ESTACION ORDER BY NOMBRE";
        List<Estacion> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapRow(rs));
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
                return new Estacion(id, nombre.trim(), null, null, null, null, null, null, null);
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))
                throw new RuntimeException("Ya existe una estación con ese nombre.");
            throw new RuntimeException("Error creando estación", e);
        }
    }

    public void update(int id, String nombre, String encargado, String geo, int cantidadTanques) {
        String sql = "UPDATE ESTACION SET NOMBRE=?, ENCARGADO=?, GEOUBICACION=? WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, encargado);
            ps.setString(3, geo);
            ps.setInt(4, id);
            ps.executeUpdate();
            actualizarCantidadTanques(id);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))
                throw new RuntimeException("Ya existe una estación con ese nombre.");
            throw new RuntimeException("Error actualizando estación", e);
        }
    }

    public void actualizarCantidadTanques(int estacionId) {
        String sql = "UPDATE ESTACION SET CANTIDAD_TANQUES = (SELECT COUNT(*) FROM TANQUE WHERE ESTACION_ID = ?) WHERE ID = ?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, estacionId);
            ps.setInt(2, estacionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando cantidad de tanques", e);
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

    private Estacion mapRow(ResultSet rs) throws SQLException {
        String nombre = rs.getString("NOMBRE");
        String encargado = rs.getString("ENCARGADO");
        String geo = rs.getString("GEOUBICACION");
        String tipo = rs.getString("TIPO_PEZ");

        Double litros = null;
        Object litrosObj = rs.getObject("LITROS_APROX");
        if (litrosObj != null) litros = rs.getDouble("LITROS_APROX");

        Integer peces = null;
        Object pecesObj = rs.getObject("PECES_APROX");
        if (pecesObj != null) peces = rs.getInt("PECES_APROX");

        Integer cantTanques = null;
        Object cantObj = rs.getObject("CANTIDAD_TANQUES");
        if (cantObj != null) cantTanques = rs.getInt("CANTIDAD_TANQUES");

        LocalDate fecha = null;
        String fechaStr = rs.getString("FECHA_INICIO");
        if (fechaStr != null && !fechaStr.isBlank()) {
            fecha = LocalDate.parse(fechaStr);
        }

        return new Estacion(
                rs.getInt("ID"),
                nombre,
                encargado,
                geo,
                tipo,
                litros,
                peces,
                fecha,
                cantTanques
        );
    }
}
