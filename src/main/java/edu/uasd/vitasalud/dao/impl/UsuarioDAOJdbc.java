package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.UsuarioDAO;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** DAO JDBC de {@code usuario}. Todo el SQL usa PreparedStatement. */
public class UsuarioDAOJdbc implements UsuarioDAO {

    private static final String SELECT_BASE = """
            SELECT u.id, u.nombre, u.username, u.password_hash, u.perfil, r.codigo AS rol,
                   u.prestador_id, u.cargo, u.estado, u.intentos_fallidos
              FROM usuario u
              JOIN rol r ON r.id = u.rol_id
            """;

    @Override
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE u.username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public int registrarIntentoFallido(long usuarioId) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE usuario SET intentos_fallidos = intentos_fallidos + 1 WHERE id = ?")) {
                ps.setLong(1, usuarioId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT intentos_fallidos FROM usuario WHERE id = ?")) {
                ps.setLong(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        }
    }

    @Override
    public void reiniciarIntentos(long usuarioId) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE usuario SET intentos_fallidos = 0 WHERE id = ?")) {
            ps.setLong(1, usuarioId);
            ps.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(long usuarioId, String estado) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE usuario SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setLong(2, usuarioId);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(rs.getString("nombre"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        u.setRol(Rol.deCodigo(rs.getString("rol")));
        long prestadorId = rs.getLong("prestador_id");
        u.setPrestadorId(rs.wasNull() ? null : prestadorId);
        u.setCargo(rs.getString("cargo"));
        u.setEstado(rs.getString("estado"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        return u;
    }
}
