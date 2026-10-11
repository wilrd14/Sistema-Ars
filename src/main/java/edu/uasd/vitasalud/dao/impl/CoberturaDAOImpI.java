
package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.CoberturaDAO;
import edu.uasd.vitasalud.model.Cobertura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CoberturaDAOImpI implements CoberturaDAO {

    @Override
    public Optional<Cobertura> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM cobertura WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cobertura cobertura = new Cobertura(
                            rs.getLong("id"),
                            rs.getInt("plan_id"),
                            rs.getString("categoria"),
                            rs.getBigDecimal("porcentaje"),
                            rs.getBigDecimal("copago_fijo"),
                            rs.getBigDecimal("tope_anual"),
                            rs.getInt("dias_carencia"),
                            rs.getBoolean("requiere_precertificacion")
                    );

                    return Optional.of(cobertura);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Cobertura> listarTodo() throws SQLException {
        List<Cobertura> coberturas = new ArrayList<>();
        String sql = "SELECT * FROM cobertura";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cobertura cobertura = new Cobertura(
                        rs.getLong("id"),
                        rs.getInt("plan_id"),
                        rs.getString("categoria"),
                        rs.getBigDecimal("porcentaje"),
                        rs.getBigDecimal("copago_fijo"),
                        rs.getBigDecimal("tope_anual"),
                        rs.getInt("dias_carencia"),
                        rs.getBoolean("requiere_precertificacion")
                );

                coberturas.add(cobertura);
            }
        }

        return coberturas;
    }

    @Override
    public void registrar(Cobertura cobertura) throws SQLException {
        String sql = "INSERT INTO cobertura "
                + "(plan_id, categoria, porcentaje, copago_fijo, "
                + "tope_anual, dias_carencia, requiere_precertificacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cobertura.getPlan_id());
            ps.setString(2, cobertura.getCategoria());
            ps.setBigDecimal(3, cobertura.getPorcentaje());
            ps.setBigDecimal(4, cobertura.getCopago_fijo());
            ps.setBigDecimal(5, cobertura.getTope_anual());
            ps.setInt(6, cobertura.getDias_carencia());
            ps.setBoolean(7, cobertura.getRequiere_precertificacion());

            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Cobertura cobertura) throws SQLException {
        String sql = "UPDATE cobertura SET plan_id = ?, categoria = ?, "
                + "porcentaje = ?, copago_fijo = ?, tope_anual = ?, "
                + "dias_carencia = ?, requiere_precertificacion = ? "
                + "WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cobertura.getPlan_id());
            ps.setString(2, cobertura.getCategoria());
            ps.setBigDecimal(3, cobertura.getPorcentaje());
            ps.setBigDecimal(4, cobertura.getCopago_fijo());
            ps.setBigDecimal(5, cobertura.getTope_anual());
            ps.setInt(6, cobertura.getDias_carencia());
            ps.setBoolean(7, cobertura.getRequiere_precertificacion());
            ps.setLong(8, cobertura.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM cobertura WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
