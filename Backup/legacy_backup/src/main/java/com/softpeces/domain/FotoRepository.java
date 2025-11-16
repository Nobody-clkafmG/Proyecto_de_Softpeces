package com.softpeces.repo;

import com.softpeces.domain.*;
import com.softpeces.infra.Database;
import java.sql.*;
import java.util.*;

public class FotoRepository {

    public List<Foto> findByMuestreo(int muestreoId) {
        String sql = "SELECT * FROM FOTO WHERE MUESTREO_ID=? ORDER BY ID DESC";
        List<Foto> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, muestreoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (Exception e) { throw new RuntimeException("Error listando fotos", e); }
    }

    public Foto insert(int muestreoId, Parte parte, String ruta, boolean qcOk) {
        String sql = "INSERT INTO FOTO(MUESTREO_ID,PARTE,RUTA,QC_OK,ESTADO) VALUES (?,?,?,?,?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, muestreoId);
            ps.setString(2, parte.name());
            ps.setString(3, ruta);
            ps.setInt(4, qcOk ? 1 : 0);
            ps.setString(5, EstadoFoto.PENDIENTE.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int id = rs.next() ? rs.getInt(1) : -1;
                return new Foto(id, muestreoId, parte, ruta, qcOk, EstadoFoto.PENDIENTE, null, null, null);
            }
        } catch (SQLException e) { throw new RuntimeException("Error insertando foto", e); }
    }

    public void updateEstado(int id, EstadoFoto estado, String label, Double prob, String msg) {
        String sql = "UPDATE FOTO SET ESTADO=?, LABEL=?, PROB=?, MENSAJE_ERROR=? WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setString(2, label);
            if (prob == null) ps.setNull(3, Types.REAL); else ps.setDouble(3, prob);
            ps.setString(4, msg);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error actualizando estado de foto", e); }
    }

    private Foto map(ResultSet rs) throws Exception {
        return new Foto(
                rs.getInt("ID"),
                rs.getInt("MUESTREO_ID"),
                Parte.valueOf(rs.getString("PARTE")),
                rs.getString("RUTA"),
                rs.getInt("QC_OK")==1,
                EstadoFoto.valueOf(rs.getString("ESTADO")),
                rs.getString("LABEL"),
                rs.getObject("PROB")==null? null : rs.getDouble("PROB"),
                rs.getString("MENSAJE_ERROR")
        );
    }
}
