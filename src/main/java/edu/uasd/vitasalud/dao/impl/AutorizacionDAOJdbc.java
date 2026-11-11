

package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.AutorizacionDAO;
import edu.uasd.vitasalud.model.*;
import edu.uasd.vitasalud.service.ServicioException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación JDBC de AUT. PreparedStatement para cada entrada; cierra
 * recursos.
 */
public class AutorizacionDAOJdbc implements AutorizacionDAO {
    private static final String BASE = "SELECT a.*, CONCAT(f.nombres,' ',f.apellidos) afiliado_nombre, " +
            "p.nombre prestador_nombre FROM autorizacion a " +
            "JOIN afiliado f ON f.id=a.afiliado_id JOIN prestador p ON p.id=a.prestador_id ";

    @Override
    public String crear(Autorizacion a, List<DetalleAutorizacion> detalles) throws SQLException {
        // RN-11: sin autocommit para que cabecera y todas las líneas sean indivisibles.
        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                long id;
                String sql = "INSERT INTO autorizacion " +
                        "(afiliado_id,prestador_id,usuario_solicita_id,tipo_servicio,medico_prescriptor," +
                        "fecha_servicio,tipo_telefono,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,origen_laboral,"
                        +
                        "monto_solicitado,estado) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,'PENDIENTE')";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, a.afiliadoId());
                    ps.setLong(2, a.prestadorId());
                    ps.setLong(3, a.usuarioSolicitaId());
                    ps.setString(4, a.tipoServicio());
                    ps.setString(5, a.medicoPrescriptor());
                    ps.setDate(6, java.sql.Date.valueOf(a.fechaServicio()));
                    ps.setString(7, a.tipoTelefono());
                    ps.setString(8, a.telefonoContacto());
                    ps.setString(9, a.correoContacto());
                    ps.setString(10, a.diagnosticoCie10());
                    ps.setString(11, a.diagnosticoDescripcion());
                    ps.setBoolean(12, a.origenLaboral());
                    ps.setBigDecimal(13, a.montoSolicitado());
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        if (!claves.next())
                            throw new SQLException("No se generó id de autorización");
                        id = claves.getLong(1);
                    }
                }
                String numero = "AUT-" + String.format("%06d", id);
                try (PreparedStatement ps = c.prepareStatement("UPDATE autorizacion SET numero=? WHERE id=?")) {
                    ps.setString(1, numero);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado) VALUES(?,?,?,?)")) {
                    for (DetalleAutorizacion d : detalles) {
                        ps.setLong(1, id);
                        ps.setLong(2, d.procedimientoId());
                        ps.setInt(3, d.cantidad());
                        ps.setBigDecimal(4, d.montoSolicitado());
                        ps.addBatch();
                    }
                    ps.executeBatch();
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
    public List<Autorizacion> listar(Long prestadorId) throws SQLException {
        // Aplicación diferida de vencimientos: al abrir la bandeja se aplica RF-AUT-10.
        vencerSinReclamacion();
        // RN-07 se impone en el propio SELECT: no devolver todas para filtrar después.
        String sql = BASE + (prestadorId == null ? "" : " WHERE a.prestador_id=? ")
                + " ORDER BY a.fecha_solicitud DESC";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (prestadorId != null)
                ps.setLong(1, prestadorId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Autorizacion> lista = new ArrayList<>();
                while (rs.next())
                    lista.add(leer(rs));
                return lista;
            }
        }
    }

    @Override
    public Optional<Autorizacion> buscar(long id, Long prestadorId) throws SQLException {
        String sql = BASE + " WHERE a.id=?" + (prestadorId == null ? "" : " AND a.prestador_id=?");
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
    public List<DetalleAutorizacion> detalles(long id, Long prestadorId) throws SQLException {
        String sql = "SELECT d.*,p.codigo,p.descripcion,p.categoria FROM detalle_autorizacion d " +
                "JOIN procedimiento p ON p.id=d.procedimiento_id JOIN autorizacion a ON a.id=d.autorizacion_id " +
                "WHERE d.autorizacion_id=?" + (prestadorId == null ? "" : " AND a.prestador_id=?") + " ORDER BY d.id";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (prestadorId != null)
                ps.setLong(2, prestadorId);
            try (ResultSet rs = ps.executeQuery()) {
                List<DetalleAutorizacion> lista = new ArrayList<>();
                while (rs.next())
                    lista.add(new DetalleAutorizacion(rs.getLong("id"), rs.getLong("autorizacion_id"),
                            rs.getLong("procedimiento_id"), rs.getString("codigo"), rs.getString("descripcion"),
                            rs.getString("categoria"), rs.getInt("cantidad"), rs.getBigDecimal("monto_solicitado"),
                            rs.getBigDecimal("monto_cubierto")));
                return lista;
            }
        }
    }

    @Override
    public List<AfiliadoBusqueda> buscarAfiliados(String texto) throws SQLException {
        String sql = "SELECT id,numero_afiliado,numero_documento,nss,nombres,apellidos,estado,fecha_nacimiento " +
                "FROM afiliado WHERE numero_documento LIKE ? OR nss LIKE ? OR nombres LIKE ? OR apellidos LIKE ? " +
                "ORDER BY apellidos,nombres LIMIT 30";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            String q = "%" + texto + "%";
            for (int i = 1; i <= 4; i++)
                ps.setString(i, q);
            try (ResultSet rs = ps.executeQuery()) {
                List<AfiliadoBusqueda> lista = new ArrayList<>();
                while (rs.next())
                    lista.add(new AfiliadoBusqueda(rs.getLong("id"), rs.getString("numero_afiliado"),
                            rs.getString("numero_documento"), rs.getString("nss"), rs.getString("nombres"),
                            rs.getString("apellidos"), rs.getString("estado"),
                            rs.getDate("fecha_nacimiento").toLocalDate()));
                return lista;
            }
        }
    }

    @Override
    public List<ProcedimientoBusqueda> procedimientos() throws SQLException {
        try (Connection c = ConexionBD.getConexion();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT id,codigo,descripcion,categoria,precio_referencia FROM procedimiento ORDER BY codigo");
                ResultSet rs = ps.executeQuery()) {
            List<ProcedimientoBusqueda> lista = new ArrayList<>();
            while (rs.next())
                lista.add(new ProcedimientoBusqueda(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getBigDecimal(5)));
            return lista;
        }
    }

    @Override
    public void aprobar(long id, long usuario, Evaluador evaluador) throws SQLException {
        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                long afiliadoId;
                // Bloquear la autorización y luego el afiliado para serializar aprobaciones /
                // topes de ese afiliado.
                try (PreparedStatement ps = c
                        .prepareStatement("SELECT afiliado_id,estado FROM autorizacion WHERE id=? FOR UPDATE")) {
                    ps.setLong(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new ServicioException("Autorización no encontrada");
                        String estado = rs.getString("estado");
                        if (!estado.equals("PENDIENTE") && !estado.equals("EN_REVISION"))
                            throw new ServicioException("La autorización ya fue decidida");
                        afiliadoId = rs.getLong("afiliado_id");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM afiliado WHERE id=? FOR UPDATE")) {
                    ps.setLong(1, afiliadoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new ServicioException("Afiliado inexistente");
                    }
                }
                Contexto contexto = contexto(c, id, afiliadoId);
                Resultado resultado = evaluador.calcular(contexto); // La regla sigue en el servicio.
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE detalle_autorizacion SET monto_cubierto=? WHERE id=? AND autorizacion_id=?")) {
                    for (LineaCalculada linea : resultado.lineas()) {
                        ps.setBigDecimal(1, linea.cubierto());
                        ps.setLong(2, linea.detalleId());
                        ps.setLong(3, id);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE autorizacion SET estado='APROBADA'," +
                        "usuario_revisa_id=?,fecha_decision=NOW(),fecha_vencimiento=DATE_ADD(CURDATE(), INTERVAL 30 DAY),"
                        +
                        "monto_cubierto=?,copago=?,nota_revision=NULL WHERE id=?")) {
                    ps.setLong(1, usuario);
                    ps.setBigDecimal(2, resultado.cubiertoTotal());
                    ps.setBigDecimal(3, resultado.copagoTotal());
                    ps.setLong(4, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Contexto contexto(Connection c, long autId, long afiliadoId) throws SQLException {
        String ea, ep;
        LocalDate inicio;
        try (PreparedStatement ps = c
                .prepareStatement("SELECT f.estado,f.inicio_cobertura,p.estado_contrato FROM autorizacion a " +
                        "JOIN afiliado f ON a.afiliado_id=f.id JOIN prestador p ON a.prestador_id=p.id WHERE a.id=?")) {
            ps.setLong(1, autId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new ServicioException("Datos de autorización incompletos");
                ea = rs.getString(1);
                inicio = rs.getDate(2).toLocalDate();
                ep = rs.getString(3);
            }
        }
        List<FilaCobertura> filas = new ArrayList<>();
        String sql = "SELECT d.id,d.cantidad,d.monto_solicitado,p.categoria,COALESCE(t.monto,p.precio_referencia) tarifa,"
                +
                "cv.porcentaje,cv.copago_fijo,cv.tope_anual,cv.dias_carencia, " +
                "(SELECT COALESCE(SUM(d2.monto_cubierto),0) FROM detalle_autorizacion d2 " +
                "JOIN autorizacion a2 ON a2.id=d2.autorizacion_id JOIN procedimiento p2 ON p2.id=d2.procedimiento_id " +
                "WHERE a2.afiliado_id=? AND a2.id<>? AND a2.estado='APROBADA' " +
                "AND YEAR(a2.fecha_decision)=YEAR(CURRENT_DATE()) AND p2.categoria=p.categoria) consumido " +
                "FROM detalle_autorizacion d JOIN autorizacion a ON a.id=d.autorizacion_id " +
                "JOIN afiliado f ON f.id=a.afiliado_id JOIN procedimiento p ON p.id=d.procedimiento_id " +
                "LEFT JOIN cobertura cv ON cv.plan_id=f.plan_id AND cv.categoria=p.categoria " +
                "LEFT JOIN tarifa t ON t.prestador_id=a.prestador_id AND t.procedimiento_id=d.procedimiento_id " +
                "WHERE a.id=? ORDER BY d.id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, afiliadoId);
            ps.setLong(2, autId);
            ps.setLong(3, autId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    filas.add(new FilaCobertura(rs.getLong("id"), rs.getString("categoria"), rs.getInt("cantidad"),
                            rs.getBigDecimal("monto_solicitado"), rs.getBigDecimal("tarifa"),
                            rs.getBigDecimal("porcentaje"), rs.getBigDecimal("copago_fijo"),
                            rs.getBigDecimal("tope_anual"), rs.getInt("dias_carencia"), rs.getBigDecimal("consumido")));
            }
        }
        return new Contexto(ea, ep, inicio, filas);
    }

    @Override
    public boolean cambiarEstado(long id, long usuario, EstadoAutorizacion destino, String nota) throws SQLException {
        String condicion = switch (destino) {
            case EN_REVISION -> " AND estado='PENDIENTE'";
            case NEGADA -> " AND estado IN ('PENDIENTE','EN_REVISION')";
            case ANULADA -> " AND estado IN ('PENDIENTE','EN_REVISION','APROBADA') " +
                    " AND NOT EXISTS(SELECT 1 FROM reclamacion r WHERE r.autorizacion_id=autorizacion.id)";
            default -> throw new ServicioException("Cambio de estado no permitido");
        };
        String sql = "UPDATE autorizacion SET estado=?,usuario_revisa_id=?,fecha_decision=NOW(),nota_revision=? " +
                "WHERE id=?" + condicion;
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, destino.name());
            ps.setLong(2, usuario);
            ps.setString(3, nota);
            ps.setLong(4, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public int vencerSinReclamacion() throws SQLException {
        String sql = "UPDATE autorizacion a SET estado='VENCIDA' WHERE estado='APROBADA' " +
                "AND fecha_decision < DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                "AND NOT EXISTS (SELECT 1 FROM reclamacion r WHERE r.autorizacion_id=a.id)";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    @Override
    public void registrarIngreso(long aut, long prestador, LocalDateTime fecha, String nota) throws SQLException {
        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                // RN-07 y RF-PRE-06: la autorización aprobada debe pertenecer al prestador
                // actual.
                String sql = "INSERT INTO hospitalizacion(autorizacion_id,fecha_ingreso,observacion) " +
                        "SELECT id,?,? FROM autorizacion WHERE id=? AND prestador_id=? AND estado='APROBADA'";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(fecha));
                    ps.setString(2, nota);
                    ps.setLong(3, aut);
                    ps.setLong(4, prestador);
                    if (ps.executeUpdate() != 1)
                        throw new ServicioException("Autorización no aprobada o ajena al prestador");
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    @Override
    public void registrarEgreso(long aut, long prestador, LocalDateTime fecha) throws SQLException {
        String sql = "UPDATE hospitalizacion h JOIN autorizacion a ON a.id=h.autorizacion_id " +
                "SET h.fecha_egreso=? WHERE h.autorizacion_id=? AND a.prestador_id=? " +
                "AND h.fecha_egreso IS NULL AND h.fecha_ingreso<=?";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fecha));
            ps.setLong(2, aut);
            ps.setLong(3, prestador);
            ps.setTimestamp(4, Timestamp.valueOf(fecha));
            if (ps.executeUpdate() < 1)
                throw new ServicioException("No hay ingreso abierto para esa autorización");
        }
    }

    @Override
    public List<Hospitalizacion> hospitalizaciones(long prestador) throws SQLException {
        String sql = "SELECT h.* FROM hospitalizacion h JOIN autorizacion a ON a.id=h.autorizacion_id " +
                "WHERE a.prestador_id=? ORDER BY h.fecha_ingreso DESC";
        try (Connection c = ConexionBD.getConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, prestador);
            try (ResultSet rs = ps.executeQuery()) {
                List<Hospitalizacion> lista = new ArrayList<>();
                while (rs.next()) {
                    Timestamp e = rs.getTimestamp("fecha_egreso");
                    lista.add(new Hospitalizacion(rs.getLong("id"), rs.getLong("autorizacion_id"),
                            rs.getTimestamp("fecha_ingreso").toLocalDateTime(), e == null ? null : e.toLocalDateTime(),
                            rs.getString("observacion")));
                }
                return lista;
            }
        }
    }

    private Autorizacion leer(ResultSet rs) throws SQLException {
        Timestamp f = rs.getTimestamp("fecha_solicitud");
        java.sql.Date v = rs.getDate("fecha_vencimiento");
        return new Autorizacion(rs.getLong("id"), rs.getString("numero"), rs.getLong("afiliado_id"),
                rs.getLong("prestador_id"), rs.getLong("usuario_solicita_id"), rs.getString("afiliado_nombre"),
                rs.getString("prestador_nombre"), f == null ? null : f.toLocalDateTime(), rs.getString("tipo_servicio"),
                rs.getDate("fecha_servicio") == null ? null : rs.getDate("fecha_servicio").toLocalDate(),
                rs.getString("tipo_telefono"),
                rs.getString("medico_prescriptor"), rs.getString("telefono_contacto"), rs.getString("correo_contacto"),
                rs.getString("diagnostico_cie10"), rs.getString("diagnostico_desc"), rs.getBoolean("origen_laboral"),
                rs.getBigDecimal("monto_solicitado"), rs.getBigDecimal("monto_cubierto"), rs.getBigDecimal("copago"),
                EstadoAutorizacion.valueOf(rs.getString("estado")), rs.getString("nota_revision"),
                v == null ? null : v.toLocalDate());
    }
}
