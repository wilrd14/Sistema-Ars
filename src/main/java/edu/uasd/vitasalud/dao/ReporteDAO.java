package edu.uasd.vitasalud.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Consultas de solo lectura para el Panel y los Reportes (RF-REP-01 a RF-REP-04).
 * Los rangos {@code desde}/{@code hasta} son inclusivos y pueden ser null (sin límite).
 */
public interface ReporteDAO {

    /** Autorizaciones de un estado; {@code decididas} y {@code segundosRespuesta} solo cuentan las que tienen fecha_decision. */
    record ConteoEstado(String estado, long total, long decididas, long segundosRespuesta) {
    }

    record ReclamacionPrestador(String prestador, long total, BigDecimal reclamado, BigDecimal glosado,
                                BigDecimal aPagar) {
    }

    record DevolucionPrestador(String prestador, String motivo, long cantidad) {
    }

    record Pago(String referencia, String reclamacion, String prestador, LocalDate fechaPago, BigDecimal monto) {
    }

    record ConteoPrecertificacion(String estado, long cantidad) {
    }

    record ActividadUsuario(String usuario, String modulo, long acciones, LocalDateTime ultima) {
    }

    record SolicitudAntigua(String numero, String prestador, LocalDateTime fechaSolicitud) {
    }

    List<ConteoEstado> autorizacionesPorEstado(LocalDate desde, LocalDate hasta) throws SQLException;

    List<ReclamacionPrestador> reclamacionesPorPrestador(LocalDate desde, LocalDate hasta) throws SQLException;

    List<DevolucionPrestador> devoluciones(LocalDate desde, LocalDate hasta) throws SQLException;

    List<Pago> pagos(LocalDate desde, LocalDate hasta) throws SQLException;

    List<ConteoPrecertificacion> precertificaciones(LocalDate desde, LocalDate hasta) throws SQLException;

    List<ActividadUsuario> actividadUsuarios(LocalDate desde, LocalDate hasta) throws SQLException;

    /** Las {@code limite} solicitudes PENDIENTE más antiguas. */
    List<SolicitudAntigua> pendientesMasAntiguas(int limite) throws SQLException;

    long reclamacionesEnAuditoria() throws SQLException;

    long cuentasBloqueadas() throws SQLException;
}
