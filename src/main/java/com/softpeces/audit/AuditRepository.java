package com.softpeces.audit;

import com.softpeces.infra.Database;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AuditRepository {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<AuditRow> search(String user, String accion, String entidad,
                                 LocalDate desde, LocalDate hasta) {
        StringBuilder sb = new StringBuilder("""
      SELECT ID, FECHA_HORA, USERNAME, ACCION, ENTIDAD, ENTIDAD_ID, DETALLE
      FROM BITACORA WHERE 1=1
    """);
        List<Object> args = new ArrayList<>();
        if (user != null && !user.isBlank()) { sb.append(" AND USERNAME LIKE ?"); args.add("%"+user.trim()+"%"); }
        if (accion != null && !accion.isBlank()) { sb.append(" AND ACCION LIKE ?"); args.add("%"+accion.trim()+"%"); }
        if (entidad != null && !entidad.isBlank()) { sb.append(" AND ENTIDAD LIKE ?"); args.add("%"+entidad.trim()+"%"); }
        if (desde != null) { sb.append(" AND FECHA_HORA >= ?"); args.add(desde.atStartOfDay().format(FMT)); }
        if (hasta != null) { sb.append(" AND FECHA_HORA <= ?"); args.add(hasta.plusDays(1).atStartOfDay().format(FMT)); }
        sb.append(" ORDER BY ID DESC");

        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i=0;i<args.size();i++) ps.setObject(i+1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<AuditRow> out = new ArrayList<>();
                while (rs.next()) out.add(new AuditRow(
                        rs.getInt("ID"), rs.getString("FECHA_HORA"), rs.getString("USERNAME"),
                        rs.getString("ACCION"), rs.getString("ENTIDAD"),
                        (Integer) rs.getObject("ENTIDAD_ID"), rs.getString("DETALLE")
                ));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException("Error leyendo bitácora", e); }
    }
}
