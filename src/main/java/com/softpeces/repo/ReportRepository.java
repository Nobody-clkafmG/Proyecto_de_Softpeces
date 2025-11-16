package com.softpeces.reports;

import com.softpeces.infra.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportRepository {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ReportRow> search(Integer loteId, Integer estacionId,
                                  LocalDate desde, LocalDate hasta) {
        StringBuilder sb = new StringBuilder("""
      SELECT m.ID as MID, m.FECHA_HORA, l.NOMBRE as LOTE,
             e.NOMBRE as ESTACION, t.CODIGO as TANQUE,
             f.PARTE, f.RUTA, f.QC_OK, f.ESTADO, f.LABEL, f.PROB
      FROM FOTO f
      JOIN MUESTREO m ON m.ID = f.MUESTREO_ID
      JOIN LOTE l      ON l.ID = m.LOTE_ID
      JOIN ESTACION e  ON e.ID = m.ESTACION_ID
      JOIN TANQUE t    ON t.ID = m.TANQUE_ID
      WHERE 1=1
      """);
        List<Object> args = new ArrayList<>();

        if (loteId != null) { sb.append(" AND l.ID=?"); args.add(loteId); }
        if (estacionId != null) { sb.append(" AND e.ID=?"); args.add(estacionId); }
        if (desde != null) { sb.append(" AND m.FECHA_HORA >= ?"); args.add(desde.atStartOfDay().format(FMT)); }
        if (hasta != null) { sb.append(" AND m.FECHA_HORA <= ?"); args.add(hasta.plusDays(1).atStartOfDay().format(FMT)); }

        sb.append(" ORDER BY m.FECHA_HORA DESC, m.ID DESC");

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i=0;i<args.size();i++) ps.setObject(i+1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<ReportRow> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new ReportRow(
                            rs.getInt("MID"),
                            rs.getString("FECHA_HORA"),
                            rs.getString("LOTE"),
                            rs.getString("ESTACION"),
                            rs.getString("TANQUE"),
                            rs.getString("PARTE"),
                            rs.getString("RUTA"),
                            rs.getInt("QC_OK")==1? "OK":"FALLA",
                            rs.getString("ESTADO"),
                            rs.getString("LABEL"),
                            rs.getObject("PROB")==null? null: rs.getDouble("PROB")
                    ));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error ejecutando reporte", e);
        }
    }
}
