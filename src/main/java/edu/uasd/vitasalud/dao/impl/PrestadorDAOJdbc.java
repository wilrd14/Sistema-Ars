
package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.PrestadorDAO;
import edu.uasd.vitasalud.model.Prestador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementa las operaciones de PrestadorDAO mediante JDBC.
 */
public class PrestadorDAOJdbc implements PrestadorDAO {

    @Override
    public Optional<Prestador> buscarPorRncCedula(String rncCedula)
            throws SQLException {

        String sql = "SELECT * FROM prestador WHERE rnc_cedula = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rncCedula);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public long registrarPrestador(Prestador prestador)
            throws SQLException {

        String sql = "INSERT INTO prestador "
                + "(rnc_cedula, nombre, tipo, direccion, provincia_id, "
                + "telefono, correo, estado_contrato) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, prestador.getRncCedula());
            ps.setString(2, prestador.getNombre());
            ps.setString(3, prestador.getTipo());
            ps.setString(4, prestador.getDireccion());

            if (prestador.getProvinciaId() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, prestador.getProvinciaId());
            }

            ps.setString(6, prestador.getTelefono());
            ps.setString(7, prestador.getCorreo());

            String estado = prestador.getEstadoContrato() == null
                    ? "ACTIVO"
                    : prestador.getEstadoContrato();

            ps.setString(8, estado);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    prestador.setId(id);
                    prestador.setEstadoContrato(estado);
                    return id;
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del prestador.");
    }

    @Override
    public void actualizarPrestador(Prestador prestador)
            throws SQLException {

        String sql = "UPDATE prestador SET "
                + "rnc_cedula = ?, nombre = ?, tipo = ?, direccion = ?, "
                + "provincia_id = ?, telefono = ?, correo = ? "
                + "WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, prestador.getRncCedula());
            ps.setString(2, prestador.getNombre());
            ps.setString(3, prestador.getTipo());
            ps.setString(4, prestador.getDireccion());

            if (prestador.getProvinciaId() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, prestador.getProvinciaId());
            }

            ps.setString(6, prestador.getTelefono());
            ps.setString(7, prestador.getCorreo());
            ps.setLong(8, prestador.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void cambiarEstadoContrato(Long id, String estadoContrato)
            throws SQLException {

        String sql = "UPDATE prestador "
                + "SET estado_contrato = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estadoContrato);
            ps.setLong(2, id);

            ps.executeUpdate();
        }
    }

    @Override
    public List<Prestador> listarPrestadores() throws SQLException {

        List<Prestador> prestadores = new ArrayList<>();
        String sql = "SELECT * FROM prestador ORDER BY nombre";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestadores.add(mapear(rs));
            }
        }

        return prestadores;
    }

    @Override
    public List<Prestador> buscarPorNombre(String nombre)
            throws SQLException {

        List<Prestador> prestadores = new ArrayList<>();
        String sql = "SELECT * FROM prestador WHERE nombre LIKE ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestadores.add(mapear(rs));
                }
            }
        }

        return prestadores;
    }

    /**
     * Convierte una fila de la tabla prestador en un objeto Prestador.
     */
    private Prestador mapear(ResultSet rs) throws SQLException {

        Prestador p = new Prestador();

        p.setId(rs.getLong("id"));
        p.setRncCedula(rs.getString("rnc_cedula"));
        p.setNombre(rs.getString("nombre"));
        p.setTipo(rs.getString("tipo"));
        p.setDireccion(rs.getString("direccion"));

        int provinciaId = rs.getInt("provincia_id");
        p.setProvinciaId(rs.wasNull() ? null : provinciaId);

        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setEstadoContrato(rs.getString("estado_contrato"));

        return p;
    }
}
