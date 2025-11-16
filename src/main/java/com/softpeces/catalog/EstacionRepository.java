package com.softpeces.catalog;

import com.softpeces.infra.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EstacionRepository {

    private static final String SQL_FIND_ALL =
            "SELECT ID,NOMBRE,ENCARGADO,GEOUBICACION,TIPO_PEZ," +
                    " LITROS_APROX,PECES_APROX,FECHA_INICIO,CANTIDAD_TANQUES," +
                    " ALTITUD,AREA_M2,FUENTE_AGUA " +
                    "FROM ESTACION ORDER BY NOMBRE";

    public List<Estacion> findAll() {
        List<Estacion> out = new ArrayList<>();
        try (Connection cn = Database.get();
             PreparedStatement ps = cn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public Estacion insert(String nombre) {
        String sql = "INSERT INTO ESTACION(NOMBRE) VALUES (?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int id = rs.next() ? rs.getInt(1) : -1;
                return new Estacion(id, nombre.trim(), null, null, null, null, null, null, null, null, null, null);
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
        String nombre     = rs.getString("NOMBRE");
        String encargado  = rs.getString("ENCARGADO");
        String geo        = rs.getString("GEOUBICACION");
        String tipo       = rs.getString("TIPO_PEZ");

        Double  litros    = (Double)  rs.getObject("LITROS_APROX");
        Integer peces     = (Integer) rs.getObject("PECES_APROX");
        Integer cantTq    = (Integer) rs.getObject("CANTIDAD_TANQUES");

        LocalDate fecha   = null;
        String f = rs.getString("FECHA_INICIO");
        if (f != null && !f.isBlank()) fecha = LocalDate.parse(f);

        Integer altitud   = (Integer) rs.getObject("ALTITUD");
        Double  areaM2    = (Double)  rs.getObject("AREA_M2");
        String  fuente    = rs.getString("FUENTE_AGUA");

        return new Estacion(
                rs.getInt("ID"),
                nombre,
                encargado,
                geo,
                tipo,
                litros,
                peces,
                fecha,
                cantTq,
                altitud,
                areaM2,
                fuente
        );
    }

}
