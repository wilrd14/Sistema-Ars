
package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.ComunicadoDAO;
import edu.uasd.vitasalud.model.Comunicado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComunicadoDAOJdbc implements ComunicadoDAO {

    @Override
    public Optional<Comunicado> buscarPorId(Integer id) throws SQLException {
        String sql = "SELECT * FROM comunicado WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Comunicado> listarTodo() throws SQLException {
        List<Comunicado> comunicados = new ArrayList<>();
        String sql = "SELECT * FROM comunicado ORDER BY fecha_publicacion DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comunicados.add(mapear(rs));
            }
        }
        return comunicados;
    }

    @Override
    public void registrar(Comunicado comunicado) throws SQLException {
        String sql = "INSERT INTO comunicado (titulo, mensaje, vigente) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, comunicado.getTitulo());
            ps.setString(2, comunicado.getMensaje());
            ps.setBoolean(3, comunicado.getVigente());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Comunicado comunicado) throws SQLException {
        String sql = "UPDATE comunicado SET titulo = ?, mensaje = ?, vigente = ? "
                + "WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, comunicado.getTitulo());
            ps.setString(2, comunicado.getMensaje());
            ps.setBoolean(3, comunicado.getVigente());
            ps.setInt(4, comunicado.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM comunicado WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Comunicado mapear(ResultSet rs) throws SQLException {
        Timestamp fecha = rs.getTimestamp("fecha_publicacion");

        return new Comunicado(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("mensaje"),
                fecha.toLocalDateTime(),
                rs.getBoolean("vigente")
        );
    }
}
