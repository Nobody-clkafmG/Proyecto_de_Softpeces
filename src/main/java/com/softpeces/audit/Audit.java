package com.softpeces.audit;

import com.softpeces.auth.AuthContext;
import com.softpeces.infra.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Audit {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String accion, String entidad, Integer entidadId, String detalle) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO BITACORA(USERNAME,ACCION,ENTIDAD,ENTIDAD_ID,FECHA_HORA,DETALLE) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, AuthContext.username());
            ps.setString(2, accion);
            ps.setString(3, entidad);
            if (entidadId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, entidadId);
            ps.setString(5, LocalDateTime.now().format(FMT));
            ps.setString(6, detalle);
            ps.executeUpdate();
        } catch (SQLException e) {
            // No rompas el flujo por fallas de auditoría
            System.err.println("AUDIT ERROR: " + e.getMessage());
        }
    }
}
