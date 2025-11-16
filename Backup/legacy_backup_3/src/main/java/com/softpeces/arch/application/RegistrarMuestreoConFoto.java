package com.softpeces.arch.application;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.softpeces.domain.Parte;
import com.softpeces.infra.Database;
import com.softpeces.repo.FotoRepository;

/** Caso de uso: registrar muestreo y su fotografía (no invasivo). */
public class RegistrarMuestreoConFoto {
    private final FotoRepository fotos = new FotoRepository();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Usa IDs existentes (alineado con la BD actual). */
    public void registrar(int loteId, int estacionId, int tanqueId, Path rutaFoto, Parte parte) {
        LocalDateTime fecha = LocalDateTime.now();
        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            int muestreoId;

            // Insertar muestreo
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO MUESTREO(LOTE_ID,ESTACION_ID,TANQUE_ID,FECHA_HORA) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, loteId);
                ps.setInt(2, estacionId);
                ps.setInt(3, tanqueId);
                ps.setString(4, fecha.format(FMT));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No se obtuvo ID de muestreo.");
                    muestreoId = rs.getInt(1);
                }
            }

            // Insertar foto asociada (usa tu repo actual)
            fotos.insert(muestreoId, parte, rutaFoto.toString(), true);

            c.commit();
        } catch (Exception e) {
            throw new RuntimeException("Error registrando muestreo/foto", e);
        }
    }
}
