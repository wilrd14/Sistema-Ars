
package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.ProcedimientoDAO;
import edu.uasd.vitasalud.model.Procedimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcedimientoDAOImpI implements ProcedimientoDAO {

    @Override
    public Optional<Procedimiento> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM procedimiento WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Procedimiento procedimiento = new Procedimiento(
                            rs.getLong("id"),
                            rs.getString("codigo"),
                            rs.getString("descripcion"),
                            rs.getString("categoria"),
                            rs.getBigDecimal("precio_referencia")
                    );

                    return Optional.of(procedimiento);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Procedimiento> listarTodo() throws SQLException {
        List<Procedimiento> procedimientos = new ArrayList<>();
        String sql = "SELECT * FROM procedimiento";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Procedimiento procedimiento = new Procedimiento(
                        rs.getLong("id"),
                        rs.getString("codigo"),
                        rs.getString("descripcion"),
                        rs.getString("categoria"),
                        rs.getBigDecimal("precio_referencia")
                );

                procedimientos.add(procedimiento);
            }
        }

        return procedimientos;
    }

    @Override
    public void guardar(Procedimiento procedimiento) throws SQLException {
        String sql = "INSERT INTO procedimiento "
                + "(codigo, descripcion, categoria, precio_referencia) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, procedimiento.getCodigo());
            ps.setString(2, procedimiento.getDescripcion());
            ps.setString(3, procedimiento.getCategoria());
            ps.setBigDecimal(4, procedimiento.getPrecio_referencia());

            ps.executeUpdate();
        }
    }

    @Override
    public void modificar(Procedimiento procedimiento) throws SQLException {
        String sql = "UPDATE procedimiento SET codigo = ?, "
                + "descripcion = ?, categoria = ?, precio_referencia = ? "
                + "WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, procedimiento.getCodigo());
            ps.setString(2, procedimiento.getDescripcion());
            ps.setString(3, procedimiento.getCategoria());
            ps.setBigDecimal(4, procedimiento.getPrecio_referencia());
            ps.setLong(5, procedimiento.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM procedimiento WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
