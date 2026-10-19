package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.ProcedimientoDAO;
import edu.uasd.vitasalud.dao.impl.ProcedimientoDAOJdbc;
import edu.uasd.vitasalud.model.Procedimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConsultaPrestadorService {

    private final ProcedimientoDAO procedimientoDAO;

    public ConsultaPrestadorService() {
        this(new ProcedimientoDAOJdbc());
    }

    public ConsultaPrestadorService(ProcedimientoDAO procedimientoDAO) {
        this.procedimientoDAO = procedimientoDAO;
    }

    public List<Procedimiento> buscarProcedimientos(String criterio)
            throws SQLException {

        if (criterio == null || criterio.isBlank()) {
            throw new IllegalArgumentException(
                    "Escribe un código, descripción o categoría.");
        }

        String buscado = criterio.trim().toLowerCase(Locale.ROOT);

        return procedimientoDAO.listarTodo().stream()
                .filter(p -> contiene(p.getCodigo(), buscado)
                        || contiene(p.getDescripcion(), buscado)
                        || contiene(p.getCategoria(), buscado))
                .toList();
    }

    public List<SolicitudPrestador> listarSolicitudes()
            throws SQLException {

        long prestadorId = obtenerPrestadorId();

        String sql = "SELECT id, "
                + "COALESCE(numero, CONCAT('AUT-', LPAD(id, 6, '0'))) "
                + "AS numero_mostrar, fecha_solicitud, "
                + "COALESCE(tipo_servicio, 'No especificado') "
                + "AS tipo_servicio, estado "
                + "FROM autorizacion "
                + "WHERE prestador_id = ? "
                + "ORDER BY fecha_solicitud DESC, id DESC";

        List<SolicitudPrestador> resultado = new ArrayList<>();

        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, prestadorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    LocalDateTime fecha = rs.getTimestamp(
                            "fecha_solicitud").toLocalDateTime();

                    resultado.add(new SolicitudPrestador(
                            rs.getString("numero_mostrar"),
                            fecha,
                            rs.getString("tipo_servicio"),
                            rs.getString("estado")
                    ));
                }
            }
        }

        return resultado;
    }

    public List<PrecertificacionPrestador> listarPrecertificaciones()
            throws SQLException {

        long prestadorId = obtenerPrestadorId();

        String sql = "SELECT a.id, "
                + "COALESCE(a.numero_precertificacion, "
                + "CONCAT('PRE-', LPAD(a.id, 6, '0'))) AS numero_mostrar, "
                + "a.fecha_solicitud, "
                + "COALESCE(GROUP_CONCAT(DISTINCT p.descripcion "
                + "ORDER BY p.descripcion SEPARATOR ', '), "
                + "a.tipo_servicio, 'Sin procedimiento detallado') "
                + "AS procedimiento, "
                + "COALESCE(a.estado_precertificacion, "
                + "a.estado, 'PENDIENTE') AS estado_mostrar "
                + "FROM autorizacion a "
                + "LEFT JOIN detalle_autorizacion d "
                + "ON d.autorizacion_id = a.id "
                + "LEFT JOIN procedimiento p "
                + "ON p.id = d.procedimiento_id "
                + "WHERE a.prestador_id = ? "
                + "AND (a.numero_precertificacion IS NOT NULL "
                + "OR a.estado_precertificacion IS NOT NULL) "
                + "GROUP BY a.id, a.numero_precertificacion, "
                + "a.fecha_solicitud, a.tipo_servicio, "
                + "a.estado_precertificacion, a.estado "
                + "ORDER BY a.fecha_solicitud DESC, a.id DESC";

        List<PrecertificacionPrestador> resultado = new ArrayList<>();

        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, prestadorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    resultado.add(new PrecertificacionPrestador(
                            rs.getString("numero_mostrar"),
                            rs.getTimestamp("fecha_solicitud")
                                    .toLocalDateTime(),
                            rs.getString("procedimiento"),
                            rs.getString("estado_mostrar")
                    ));
                }
            }
        }

        return resultado;
    }

    public List<ResumenReporte> obtenerReporte() throws SQLException {

        long prestadorId = obtenerPrestadorId();

        String sql = "SELECT estado, COUNT(*) AS cantidad, "
                + "COALESCE(SUM(monto_solicitado), 0) AS monto "
                + "FROM autorizacion "
                + "WHERE prestador_id = ? "
                + "GROUP BY estado ORDER BY estado";

        List<ResumenReporte> resultado = new ArrayList<>();

        long total = 0;
        java.math.BigDecimal montoTotal =
                java.math.BigDecimal.ZERO;

        try (Connection c = ConexionBD.getConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, prestadorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    long cantidad = rs.getLong("cantidad");

                    java.math.BigDecimal monto =
                            rs.getBigDecimal("monto");

                    String estado = rs.getString("estado");

                    resultado.add(new ResumenReporte(
                            estado,
                            cantidad,
                            "Monto solicitado: RD$ "
                                    + monto.toPlainString()
                    ));

                    total += cantidad;
                    montoTotal = montoTotal.add(monto);
                }
            }
        }

        resultado.add(0, new ResumenReporte(
                "TOTAL DE SOLICITUDES",
                total,
                "Monto solicitado acumulado: RD$ "
                        + montoTotal.toPlainString()
        ));

        return resultado;
    }

    private long obtenerPrestadorId() {

        Long id = Sesion.getPrestadorId();

        if (id == null || id <= 0) {
            throw new IllegalStateException(
                    "La sesión activa no corresponde a un prestador válido.");
        }

        return id;
    }

    private boolean contiene(String texto, String buscado) {
        return texto != null
                && texto.toLowerCase(Locale.ROOT).contains(buscado);
    }

    public static final class SolicitudPrestador {

        private final String numero;
        private final LocalDateTime fecha;
        private final String tipo;
        private final String estado;

        public SolicitudPrestador(
                String numero,
                LocalDateTime fecha,
                String tipo,
                String estado) {

            this.numero = numero;
            this.fecha = fecha;
            this.tipo = tipo;
            this.estado = estado;
        }

        public String getNumero() {
            return numero;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }

        public String getTipo() {
            return tipo;
        }

        public String getEstado() {
            return estado;
        }
    }

    public static final class PrecertificacionPrestador {

        private final String numero;
        private final LocalDateTime fecha;
        private final String procedimiento;
        private final String estado;

        public PrecertificacionPrestador(
                String numero,
                LocalDateTime fecha,
                String procedimiento,
                String estado) {

            this.numero = numero;
            this.fecha = fecha;
            this.procedimiento = procedimiento;
            this.estado = estado;
        }

        public String getNumero() {
            return numero;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }

        public String getProcedimiento() {
            return procedimiento;
        }

        public String getEstado() {
            return estado;
        }
    }

    public static final class ResumenReporte {

        private final String concepto;
        private final long cantidad;
        private final String detalle;

        public ResumenReporte(
                String concepto,
                long cantidad,
                String detalle) {

            this.concepto = concepto;
            this.cantidad = cantidad;
            this.detalle = detalle;
        }

        public String getConcepto() {
            return concepto;
        }

        public long getCantidad() {
            return cantidad;
        }

        public String getDetalle() {
            return detalle;
        }
    }
}
