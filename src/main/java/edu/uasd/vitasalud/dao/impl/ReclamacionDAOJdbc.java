package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.ReclamacionDAO;
import edu.uasd.vitasalud.model.*;
import edu.uasd.vitasalud.service.ServicioException;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO JDBC de reclamaciones. RN-07, RN-08 y RN-11 se respaldan también en SQL.
 */
public class ReclamacionDAOJdbc implements ReclamacionDAO {
    private static final String BASE = "SELECT r.*,a.numero numero_autorizacion,p.nombre prestador_nombre " +
            "FROM reclamacion r JOIN autorizacion a ON a.id=r.autorizacion_id " +
            "JOIN prestador p ON p.id=r.prestador_id ";

    @Override
    public String crear(Reclamacion r) throws SQLException {
        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                // Bloqueo de autorización: dos solicitudes simultáneas no deben duplicar la
                // reclamación.
                String sql = "SELECT a.prestador_id,a.estado,a.monto_cubierto," +
                        "(SELECT COUNT(*) FROM reclamacion x WHERE x.autorizacion_id=a.id) numero_reclamaciones " +
                        "FROM autorizacion a WHERE a.id=? AND a.prestador_id=? " +
                        "AND a.fecha_decision >= DATE_SUB(NOW(), INTERVAL 30 DAY) FOR UPDATE";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setLong(1, r.autorizacionId());
                    ps.setLong(2, r.prestadorId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new ServicioException("Autorización no encontrada para el prestador");
                        if (!"APROBADA".equals(rs.getString("estado")))
                            throw new ServicioException("Solo se reclama una autorización APROBADA y vigente");
                        if (rs.getInt("numero_reclamaciones") > 0)
                            throw new ServicioException("Esta autorización ya tiene reclamación");
                        BigDecimal cubierto = rs.getBigDecimal("monto_cubierto");
                        if (cubierto == null || r.montoReclamado().compareTo(cubierto) > 0)
                            throw new ServicioException("El monto excede la cobertura autorizada");
                    }
                }
                long id;
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO reclamacion(" +
                        "autorizacion_id,prestador_id,factura_prestador,fecha_servicio," +
                        "monto_reclamado,monto_glosado,monto_pagar,estado) VALUES(?,?,?,?,?,0,?,'RECIBIDA')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, r.autorizacionId());
                    ps.setLong(2, r.prestadorId());
                    ps.setString(3, r.facturaPrestador());
                    ps.setDate(4, java.sql.Date.valueOf(r.fechaServicio()));
                    ps.setBigDecimal(5, r.montoReclamado());
                    ps.setBigDecimal(6, r.montoReclamado());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next())
                            throw new SQLException("No se generó id REC");
                        id = rs.getLong(1);
                    }
                }
                String numero = "REC-" + String.format("%06d", id);
                try (PreparedStatement ps = c.prepareStatement("UPDATE reclamacion SET numero=? WHERE id=?")) {
                    ps.setString(1, numero);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
                c.commit();
                return numero;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Reclamacion> listar(Long prestadorId) throws SQLException {
        String sql = BASE + (prestadorId == null ? "" : " WHERE r.prestador_id=?") + " ORDER BY r.id DESC";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (prestadorId != null)
                ps.setLong(1, prestadorId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Reclamacion> lista = new ArrayList<>();
                while (rs.next())
                    lista.add(leer(rs));
                return lista;
            }
        }
    }

    @Override
    public Optional<Reclamacion> buscar(long id, Long prestadorId) throws SQLException {
        String sql = BASE + " WHERE r.id=?" + (prestadorId == null ? "" : " AND r.prestador_id=?");
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (prestadorId != null)
                ps.setLong(2, prestadorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean cambiarEstado(long id, EstadoReclamacion actual, EstadoReclamacion destino, String nota)
            throws SQLException {
        // Optimistic locking por estado; PAGADA jamás se cambia ni hay rutas de
        // regreso.
        String sql = "UPDATE reclamacion SET estado=?,motivo_devolucion=? WHERE id=? AND estado=? AND estado<>'PAGADA'";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, destino.name());
            ps.setString(2, nota);
            ps.setLong(3, id);
            ps.setString(4, actual.name());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public void agregarGlosa(long id, Glosa glosa) throws SQLException {
        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                BigDecimal reclamada, actual;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT monto_reclamado,monto_glosado,estado FROM reclamacion WHERE id=? FOR UPDATE")) {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new ServicioException("Reclamación inexistente");
                        if (!"EN_AUDITORIA".equals(rs.getString("estado")) && !"GLOSADA".equals(rs.getString("estado")))
                            throw new ServicioException("Se glosa solo durante auditoría");
                        reclamada = rs.getBigDecimal("monto_reclamado");
                        actual = rs.getBigDecimal("monto_glosado");
                    }
                }
                BigDecimal acumulado = actual.add(glosa.monto());
                if (acumulado.compareTo(reclamada) > 0)
                    throw new ServicioException("Las glosas exceden el monto reclamado");
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO glosa(reclamacion_id,motivo,monto,observacion,usuario_id) VALUES(?,?,?,?,?)")) {
                    ps.setLong(1, id);
                    ps.setString(2, glosa.motivo());
                    ps.setBigDecimal(3, glosa.monto());
                    ps.setString(4, glosa.observacion());
                    ps.setLong(5, glosa.usuarioId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE reclamacion SET estado='GLOSADA',monto_glosado=?,monto_pagar=? WHERE id=?")) {
                    ps.setBigDecimal(1, acumulado);
                    ps.setBigDecimal(2, reclamada.subtract(acumulado));
                    ps.setLong(3, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<Glosa> glosas(long id) throws SQLException {
        try (Connection c = ConexionBD.getConexion();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM glosa WHERE reclamacion_id=? ORDER BY id")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Glosa> datos = new ArrayList<>();
                while (rs.next())
                    datos.add(new Glosa(rs.getLong("id"), id, rs.getString("motivo"), rs.getBigDecimal("monto"),
                            rs.getString("observacion"), rs.getLong("usuario_id")));
                return datos;
            }
        }
    }

    @Override
    public boolean pagar(long id, LocalDate fecha, String referencia) throws SQLException {
        String sql = "UPDATE reclamacion SET estado='PAGADA',fecha_pago=?,referencia_pago=? " +
                "WHERE id=? AND estado IN('APROBADA','GLOSADA')";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            ps.setString(2, referencia);
            ps.setLong(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean corregirDevuelta(long id, long prestador, String factura, LocalDate fecha, BigDecimal monto)
            throws SQLException {
        String sql = "UPDATE reclamacion r JOIN autorizacion a ON a.id=r.autorizacion_id " +
                "SET r.factura_prestador=?,r.fecha_servicio=?,r.monto_reclamado=?,r.monto_pagar=?, " +
                "r.motivo_devolucion=NULL,r.estado='RECIBIDA' " +
                "WHERE r.id=? AND r.prestador_id=? AND r.estado='DEVUELTA' AND r.monto_glosado=0 " +
                "AND a.estado='APROBADA' AND a.monto_cubierto>=?";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, factura);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            ps.setBigDecimal(3, monto);
            ps.setBigDecimal(4, monto);
            ps.setLong(5, id);
            ps.setLong(6, prestador);
            ps.setBigDecimal(7, monto);
            return ps.executeUpdate() == 1;
        }
    }

    private Reclamacion leer(ResultSet rs) throws SQLException {
        java.sql.Date s = rs.getDate("fecha_servicio"), p = rs.getDate("fecha_pago");
        return new Reclamacion(rs.getLong("id"), rs.getString("numero"), rs.getLong("autorizacion_id"),
                rs.getLong("prestador_id"), rs.getString("numero_autorizacion"), rs.getString("prestador_nombre"),
                rs.getString("factura_prestador"), s == null ? null : s.toLocalDate(),
                rs.getBigDecimal("monto_reclamado"),
                rs.getBigDecimal("monto_glosado"), rs.getBigDecimal("monto_pagar"),
                EstadoReclamacion.valueOf(rs.getString("estado")), rs.getString("motivo_devolucion"),
                p == null ? null : p.toLocalDate(), rs.getString("referencia_pago"));
    }
}
