package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.ReporteDAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Consultas JDBC de solo lectura sobre autorizacion, reclamacion, prestador, bitacora y usuario. */
public class ReporteDAOJdbc implements ReporteDAO {

    /** Convierte una fila del resultado. */
    private interface Mapa<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    /**
     * Agrega a {@code sqlBase} (que termina en una condición WHERE) el filtro de fechas sobre {@code columna}
     * y la {@code cola} (GROUP BY / ORDER BY). Los textos de SQL son fijos; las fechas van como parámetros.
     */
    private <T> List<T> consultar(String sqlBase, String columna, String cola, LocalDate desde, LocalDate hasta,
                                  Mapa<T> mapa) throws SQLException {
        StringBuilder sql = new StringBuilder(sqlBase);
        if (desde != null) {
            sql.append(" AND ").append(columna).append(" >= ?");
        }
        if (hasta != null) {
            sql.append(" AND ").append(columna).append(" < ?");
        }
        sql.append(' ').append(cola);
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (desde != null) {
                ps.setTimestamp(i++, Timestamp.valueOf(desde.atStartOfDay()));
            }
            if (hasta != null) {
                ps.setTimestamp(i, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            }
            List<T> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapa.mapear(rs));
                }
            }
            return lista;
        }
    }

    private long contar(String sql) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    @Override
    public List<ConteoEstado> autorizacionesPorEstado(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultar("""
                SELECT estado, COUNT(*) AS total,
                       SUM(CASE WHEN fecha_decision IS NOT NULL THEN 1 ELSE 0 END) AS decididas,
                       COALESCE(SUM(CASE WHEN fecha_decision IS NOT NULL
                                         THEN TIMESTAMPDIFF(SECOND, fecha_solicitud, fecha_decision) END), 0) AS segundos
                  FROM autorizacion WHERE 1=1""", "fecha_solicitud", "GROUP BY estado ORDER BY estado", desde, hasta,
                rs -> new ConteoEstado(rs.getString("estado"), rs.getLong("total"),
                        rs.getLong("decididas"), rs.getLong("segundos")));
    }

    @Override
    public List<ReclamacionPrestador> reclamacionesPorPrestador(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultar("""
                SELECT p.nombre, COUNT(*) AS total, SUM(r.monto_reclamado) AS reclamado,
                       SUM(r.monto_glosado) AS glosado, COALESCE(SUM(r.monto_pagar), 0) AS a_pagar
                  FROM reclamacion r JOIN prestador p ON p.id = r.prestador_id
                 WHERE 1=1""", "r.fecha_servicio", "GROUP BY p.id, p.nombre ORDER BY total DESC, p.nombre", desde, hasta,
                rs -> new ReclamacionPrestador(rs.getString("nombre"), rs.getLong("total"),
                        rs.getBigDecimal("reclamado"), rs.getBigDecimal("glosado"), rs.getBigDecimal("a_pagar")));
    }

    @Override
    public List<DevolucionPrestador> devoluciones(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultar("""
                SELECT p.nombre, COALESCE(r.motivo_devolucion, '(sin motivo)') AS motivo, COUNT(*) AS cantidad
                  FROM reclamacion r JOIN prestador p ON p.id = r.prestador_id
                 WHERE r.estado = 'DEVUELTA'""", "r.fecha_servicio",
                "GROUP BY p.id, p.nombre, motivo ORDER BY p.nombre, cantidad DESC", desde, hasta,
                rs -> new DevolucionPrestador(rs.getString("nombre"), rs.getString("motivo"), rs.getLong("cantidad")));
    }

    @Override
    public List<Pago> pagos(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultar("""
                SELECT r.referencia_pago, r.numero, p.nombre, r.fecha_pago, r.monto_pagar
                  FROM reclamacion r JOIN prestador p ON p.id = r.prestador_id
                 WHERE r.estado = 'PAGADA'""", "r.fecha_pago", "ORDER BY r.fecha_pago DESC, r.numero", desde, hasta,
                rs -> {
                    Date fecha = rs.getDate("fecha_pago");
                    return new Pago(rs.getString("referencia_pago"), rs.getString("numero"), rs.getString("nombre"),
                            fecha == null ? null : fecha.toLocalDate(), rs.getBigDecimal("monto_pagar"));
                });
    }

    @Override
    public List<ConteoPrecertificacion> precertificaciones(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultar("""
                SELECT COALESCE(estado_precertificacion, 'SIN_ESTADO') AS est, COUNT(*) AS cantidad
                  FROM autorizacion WHERE numero_precertificacion IS NOT NULL""", "fecha_solicitud",
                "GROUP BY est ORDER BY est", desde, hasta,
                rs -> new ConteoPrecertificacion(rs.getString("est"), rs.getLong("cantidad")));
    }

    @Override
    public List<ActividadUsuario> actividadUsuarios(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultar("""
                SELECT COALESCE(u.nombre, '(sistema)') AS usuario, b.modulo, COUNT(*) AS acciones,
                       MAX(b.fecha_hora) AS ultima
                  FROM bitacora b LEFT JOIN usuario u ON u.id = b.usuario_id
                 WHERE 1=1""", "b.fecha_hora", "GROUP BY u.id, usuario, b.modulo ORDER BY acciones DESC, usuario",
                desde, hasta,
                rs -> {
                    Timestamp ultima = rs.getTimestamp("ultima");
                    return new ActividadUsuario(rs.getString("usuario"), rs.getString("modulo"),
                            rs.getLong("acciones"), ultima == null ? null : ultima.toLocalDateTime());
                });
    }

    @Override
    public List<SolicitudAntigua> pendientesMasAntiguas(int limite) throws SQLException {
        String sql = """
                SELECT a.numero, p.nombre, a.fecha_solicitud
                  FROM autorizacion a JOIN prestador p ON p.id = a.prestador_id
                 WHERE a.estado = 'PENDIENTE' ORDER BY a.fecha_solicitud ASC LIMIT ?""";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            List<SolicitudAntigua> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new SolicitudAntigua(rs.getString("numero"), rs.getString("nombre"),
                            rs.getTimestamp("fecha_solicitud").toLocalDateTime()));
                }
            }
            return lista;
        }
    }

    @Override
    public long reclamacionesEnAuditoria() throws SQLException {
        return contar("SELECT COUNT(*) FROM reclamacion WHERE estado = 'EN_AUDITORIA'");
    }

    @Override
    public long cuentasBloqueadas() throws SQLException {
        return contar("SELECT COUNT(*) FROM usuario WHERE estado = 'BLOQUEADO'");
    }
}
