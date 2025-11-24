package com.softpeces.arch.application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.softpeces.infra.Database;
import com.softpeces.ui.NuevaEstacionRequest;

public class RegistrarEstacionYTanquesService {

    /** Crea la ESTACION y N TANQUES con datos base. Devuelve el id de la estación. */
    public int crear(NuevaEstacionRequest r) {
        try (Connection c = Database.get()) {
            c.setAutoCommit(false);

            int estacionId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ESTACION(" +
                            "NOMBRE, ENCARGADO, GEOUBICACION, CANTIDAD_TANQUES, ALTITUD, AREA_M2, FUENTE_AGUA" +
                            ") VALUES(?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, r.sitio());
                ps.setString(2, r.encargado());
                ps.setString(3, r.geo());
                ps.setInt(4, r.cantidadTanques());
                if (r.altitud() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, r.altitud());
                if (r.areaM2() == null) ps.setNull(6, Types.DOUBLE); else ps.setDouble(6, r.areaM2());
                if (r.fuenteAgua() == null || r.fuenteAgua().isBlank()) ps.setNull(7, Types.VARCHAR);
                else ps.setString(7, r.fuenteAgua().trim());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No se obtuvo ID de estación.");
                    estacionId = rs.getInt(1);
                }
            }

            for (int i = 1; i <= r.cantidadTanques(); i++) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO TANQUE(ESTACION_ID, CODIGO, CAPACIDAD_L) VALUES(?,?,?)")) {
                    ps.setInt(1, estacionId);
                    ps.setString(2, "T" + i);
                    ps.setDouble(3, 5000); // Capacidad por defecto de 5000 litros
                    ps.executeUpdate();
                }
            }

            c.commit();
            return estacionId;
        } catch (Exception e) {
            throw new RuntimeException("Error registrando estación y tanques", e);
        }
    }
}
