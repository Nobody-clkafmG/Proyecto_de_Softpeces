package com.softpeces.repo;

import com.softpeces.domain.Lote;
import com.softpeces.domain.LoteEstado;
import com.softpeces.infra.Database;

import java.sql.*;
import java.util.*;

public class LoteRepository {

    public List<Lote> findAll() {
        String sql = "SELECT ID,NOMBRE,ESTADO FROM LOTE ORDER BY ID DESC";
        List<Lote> out = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Lote(
                        rs.getInt("ID"),
                        rs.getString("NOMBRE"),
                        LoteEstado.valueOf(rs.getString("ESTADO"))
                ));
            }
            return out;
        } catch (Exception e) { throw new RuntimeException("Error listando lotes", e); }
    }

    public Lote insert(String nombre) {
        String sql = "INSERT INTO LOTE(NOMBRE,ESTADO) VALUES (?,?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, LoteEstado.BORRADOR.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int id = rs.next()? rs.getInt(1): -1;
                return new Lote(id, nombre.trim(), LoteEstado.BORRADOR);
            }
        } catch (SQLException e) { throw new RuntimeException("Error creando lote", e); }
    }

    public void rename(int id, String nuevoNombre) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE LOTE SET NOMBRE=? WHERE ID=?")) {
            ps.setString(1, nuevoNombre.trim()); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error renombrando lote", e); }
    }

    public void changeState(int id, LoteEstado nuevo) {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE LOTE SET ESTADO=? WHERE ID=?")) {
            ps.setString(1, nuevo.name()); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error cambiando estado", e); }
    }

    public void delete(int id) {
        // Permite borrar solo si no hay muestreos
        try (Connection c = Database.get();
             PreparedStatement chk = c.prepareStatement("SELECT 1 FROM MUESTREO WHERE LOTE_ID=? LIMIT 1")) {
            chk.setInt(1, id);
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) throw new RuntimeException("No se puede eliminar: tiene muestreos.");
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM LOTE WHERE ID=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("Error eliminando lote", e); }
    }

    private static final String SQL_FIND_ALL = """
        SELECT ID,NOMBRE,ESTADO,DEPARTAMENTO,MUNICIPIO
        FROM LOTE
        ORDER BY ID DESC
        """;


    private Lote mapRow(ResultSet rs) throws SQLException {
        return new Lote(
                rs.getInt("ID"),
                rs.getString("NOMBRE"),
                LoteEstado.valueOf(rs.getString("ESTADO")),
                rs.getString("DEPARTAMENTO"),
                rs.getString("MUNICIPIO")
        );
    }


// Ajusta INSERT/UPDATE para persistir DEPARTAMENTO/MUNICIPIO
}
