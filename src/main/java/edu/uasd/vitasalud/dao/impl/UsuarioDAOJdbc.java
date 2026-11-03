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
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
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
    public Optional<Usuario> buscarPorId(long id) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE u.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Usuario> listar(String filtro) throws SQLException {
        String sql = """
                SELECT u.id, u.nombre, u.username, u.password_hash, u.perfil, r.codigo AS rol,
                       u.prestador_id, u.cargo, u.estado, u.intentos_fallidos, p.nombre AS prestador_nombre
                  FROM usuario u
                  JOIN rol r ON r.id = u.rol_id
                  LEFT JOIN prestador p ON p.id = u.prestador_id
                 WHERE (? IS NULL OR u.nombre LIKE ? OR u.username LIKE ? OR u.cargo LIKE ?)
                 ORDER BY u.nombre
                """;
        String patron = filtro == null || filtro.isBlank() ? null : "%" + filtro.trim() + "%";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ps.setString(4, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = mapear(rs);
                    u.setPrestadorNombre(rs.getString("prestador_nombre"));
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    @Override
    public List<PrestadorOpcion> listarPrestadores() throws SQLException {
        List<PrestadorOpcion> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, nombre, estado_contrato FROM prestador ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new PrestadorOpcion(rs.getLong("id"), rs.getString("nombre"), rs.getString("estado_contrato")));
            }
        }
        return lista;
    }

    @Override
    public long insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, username, password_hash, perfil, rol_id, prestador_id, cargo, estado) "
                + "VALUES (?, ?, ?, ?, (SELECT id FROM rol WHERE codigo = ?), ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getPerfil().name());
            ps.setString(5, u.getRol().codigo());
            asignarPrestador(ps, 6, u.getPrestadorId());
            ps.setString(7, u.getCargo());
            ps.setString(8, u.getEstado() == null ? Usuario.ESTADO_ACTIVO : u.getEstado());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("La base de datos no devolvió el id del usuario creado");
                }
                return keys.getLong(1);
            }
        }
    }

    @Override
    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, rol_id = (SELECT id FROM rol WHERE codigo = ?), "
                + "prestador_id = ?, cargo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRol().codigo());
            asignarPrestador(ps, 3, u.getPrestadorId());
            ps.setString(4, u.getCargo());
            ps.setLong(5, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizarPassword(long usuarioId, String passwordHash) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE usuario SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setLong(2, usuarioId);
            ps.executeUpdate();
        }
    }

    private static void asignarPrestador(PreparedStatement ps, int indice, Long prestadorId) throws SQLException {
        if (prestadorId == null) {
            ps.setNull(indice, Types.BIGINT);
        } else {
            ps.setLong(indice, prestadorId);
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
