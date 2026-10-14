
package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.PlanDAO;
import edu.uasd.vitasalud.model.Plan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlanDAOlmpl implements PlanDAO {

    @Override
    public Optional<Plan> buscarPorId(String id) throws SQLException {
        String sql = "SELECT * FROM plan WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(id));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Plan plan = new Plan(
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getBigDecimal("prima_mensual"),
                            rs.getString("estado")
                    );

                    return Optional.of(plan);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Plan> listarTodo() throws SQLException {
        List<Plan> planes = new ArrayList<>();
        String sql = "SELECT * FROM plan";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Plan plan = new Plan(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("prima_mensual"),
                        rs.getString("estado")
                );

                planes.add(plan);
            }
        }

        return planes;
    }

    @Override
    public void registrar(Plan plan) throws SQLException {
        String sql = "INSERT INTO plan "
                + "(codigo, nombre, descripcion, prima_mensual, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plan.getCodigo());
            ps.setString(2, plan.getNombre());
            ps.setString(3, plan.getDescripcion());
            ps.setBigDecimal(4, plan.getPrima_mensual());
            ps.setString(5, plan.getEstado());

            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Plan plan) throws SQLException {
        String sql = "UPDATE plan SET codigo = ?, nombre = ?, "
                + "descripcion = ?, prima_mensual = ?, estado = ? "
                + "WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plan.getCodigo());
            ps.setString(2, plan.getNombre());
            ps.setString(3, plan.getDescripcion());
            ps.setBigDecimal(4, plan.getPrima_mensual());
            ps.setString(5, plan.getEstado());
            ps.setInt(6, plan.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM plan WHERE id = ?";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
