
package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.TarifaDAO;
import edu.uasd.vitasalud.model.Tarifa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TarifaDAOImpI implements TarifaDAO {

    @Override
    public Optional<Tarifa> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM tarifa WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tarifa tarifa = new Tarifa(
                            rs.getLong("id"),
                            rs.getLong("prestador_id"),
                            rs.getLong("procedimiento_id"),
                            rs.getBigDecimal("monto")
                    );

                    return Optional.of(tarifa);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Tarifa> listarTodo() throws SQLException {
        List<Tarifa> tarifas = new ArrayList<>();
        String sql = "SELECT * FROM tarifa";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tarifa tarifa = new Tarifa(
                        rs.getLong("id"),
                        rs.getLong("prestador_id"),
                        rs.getLong("procedimiento_id"),
                        rs.getBigDecimal("monto")
                );

                tarifas.add(tarifa);
            }
        }

        return tarifas;
    }

    @Override
    public void registrar(Tarifa tarifa) throws SQLException {
        String sql = "INSERT INTO tarifa "
                + "(prestador_id, procedimiento_id, monto) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, tarifa.getPrestador_id());
            ps.setLong(2, tarifa.getProcedimiento_id());
            ps.setBigDecimal(3, tarifa.getMonto());

            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Tarifa tarifa) throws SQLException {
        String sql = "UPDATE tarifa SET prestador_id = ?, "
                + "procedimiento_id = ?, monto = ? WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, tarifa.getPrestador_id());
            ps.setLong(2, tarifa.getProcedimiento_id());
            ps.setBigDecimal(3, tarifa.getMonto());
            ps.setLong(4, tarifa.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM tarifa WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
