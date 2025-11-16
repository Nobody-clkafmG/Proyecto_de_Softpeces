package com.softpeces.catalog;

import com.softpeces.infra.Database;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TanqueRepository {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Tanque> findByEstacion(int estacionId) {
        String sql = "SELECT ID, ESTACION_ID, CODIGO, CAPACIDAD_L, TIPO_PEZ, PECES_APROX, FECHA_INICIO FROM TANQUE WHERE ESTACION_ID=? ORDER BY CODIGO";
        List<Tanque> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, estacionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String fechaStr = rs.getString("FECHA_INICIO");
                    LocalDate fecha = LocalDate.now(); // Fecha por defecto segura
                    if (fechaStr != null && !fechaStr.trim().isEmpty()) {
                        try {
                            fecha = LocalDate.parse(fechaStr, DATE_FMT);
                        } catch (Exception e) {
                            // Si la fecha está mal formateada, mantener la fecha por defecto
                            fecha = LocalDate.now();
                        }
                    }
                    Integer peces = null;
                    Object pecesObj = rs.getObject("PECES_APROX");
                    if (pecesObj != null) peces = rs.getInt("PECES_APROX");

                    out.add(new Tanque(
                        rs.getInt("ID"), 
                        rs.getInt("ESTACION_ID"),
                        rs.getString("CODIGO"), 
                        rs.getDouble("CAPACIDAD_L"),
                        rs.getString("TIPO_PEZ"),
                        peces,
                        fecha
                    ));
                }
            }
            return out;
        } catch (Exception e) { throw new RuntimeException("Error listando tanques", e); }
    }

    public Tanque insert(int estacionId, String codigo, double capacidadL, String tipoPez, Integer pecesAprox, LocalDate fechaInicio) {
        String sql = "INSERT INTO TANQUE(ESTACION_ID,CODIGO,CAPACIDAD_L,TIPO_PEZ,PECES_APROX,FECHA_INICIO) VALUES (?,?,?,?,?,?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, estacionId);
            ps.setString(2, codigo.trim());
            ps.setDouble(3, capacidadL);
            ps.setString(4, tipoPez);
            ps.setObject(5, pecesAprox);
            // Asegurar que siempre se pase una fecha válida
            LocalDate fechaFinal = fechaInicio != null ? fechaInicio : LocalDate.now();
            ps.setString(6, fechaFinal.format(DATE_FMT));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int id = rs.next() ? rs.getInt(1) : -1;
                return new Tanque(id, estacionId, codigo.trim(), capacidadL, tipoPez, pecesAprox, fechaFinal);
            }
        } catch (SQLException e) {
            if ((e.getMessage()+"").toLowerCase().contains("unique"))
                throw new RuntimeException("Ya existe un tanque con ese código en esta estación.");
            throw new RuntimeException("Error creando tanque", e);
        }
    }

    public void update(int id, String codigo, double capacidadL, String tipoPez, Integer pecesAprox, LocalDate fechaInicio) {
        String sql = "UPDATE TANQUE SET CODIGO=?, CAPACIDAD_L=?, TIPO_PEZ=?, PECES_APROX=?, FECHA_INICIO=? WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            ps.setDouble(2, capacidadL);
            ps.setString(3, tipoPez);
            ps.setObject(4, pecesAprox);
            // Asegurar que siempre se pase una fecha válida
            LocalDate fechaFinal = fechaInicio != null ? fechaInicio : LocalDate.now();
            ps.setString(5, fechaFinal.format(DATE_FMT));
            ps.setInt(6, id);
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
