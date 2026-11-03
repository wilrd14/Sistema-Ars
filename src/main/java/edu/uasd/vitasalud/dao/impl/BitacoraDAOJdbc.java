package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.BitacoraDAO;
import edu.uasd.vitasalud.model.Bitacora;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DAO JDBC de {@code bitacora}. Todo el SQL usa PreparedStatement. */
public class BitacoraDAOJdbc implements BitacoraDAO {

    @Override
    public void insertar(Bitacora b) throws SQLException {
        String sql = """
                INSERT INTO bitacora (usuario_id, perfil, modulo, accion, tabla, registro_id, detalle)
                VALUES (?, (SELECT perfil FROM usuario WHERE id = ?), ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            asignarLong(ps, 1, b.getUsuarioId());
            asignarLong(ps, 2, b.getUsuarioId());
            ps.setString(3, b.getModulo());
            ps.setString(4, b.getAccion());
            ps.setString(5, b.getTabla());
            asignarLong(ps, 6, b.getRegistroId());
            ps.setString(7, b.getDetalle());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Bitacora> buscar(String modulo, LocalDate desde, LocalDate hasta, int limite) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT b.id, b.usuario_id, u.nombre AS usuario_nombre, b.perfil, b.fecha_hora,
                       b.modulo, b.accion, b.tabla, b.registro_id, b.detalle
                  FROM bitacora b
                  LEFT JOIN usuario u ON u.id = b.usuario_id
                 WHERE 1 = 1
                """);
        List<Object> parametros = new ArrayList<>();
        if (modulo != null && !modulo.isBlank()) {
            sql.append(" AND b.modulo = ?");
            parametros.add(modulo);
        }
        if (desde != null) {
            sql.append(" AND b.fecha_hora >= ?");
            parametros.add(Timestamp.valueOf(desde.atStartOfDay()));
        }
        if (hasta != null) {
            sql.append(" AND b.fecha_hora < ?");
            parametros.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY b.fecha_hora DESC, b.id DESC LIMIT ?");
        parametros.add(limite);

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Bitacora> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    private static void asignarLong(PreparedStatement ps, int indice, Long valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.BIGINT);
        } else {
            ps.setLong(indice, valor);
        }
    }

    private Bitacora mapear(ResultSet rs) throws SQLException {
        Bitacora b = new Bitacora();
        b.setId(rs.getLong("id"));
        long usuarioId = rs.getLong("usuario_id");
        b.setUsuarioId(rs.wasNull() ? null : usuarioId);
        b.setUsuarioNombre(rs.getString("usuario_nombre"));
        b.setPerfil(rs.getString("perfil"));
        b.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        b.setModulo(rs.getString("modulo"));
        b.setAccion(rs.getString("accion"));
        b.setTabla(rs.getString("tabla"));
        long registroId = rs.getLong("registro_id");
        b.setRegistroId(rs.wasNull() ? null : registroId);
        b.setDetalle(rs.getString("detalle"));
        return b;
    }
}
