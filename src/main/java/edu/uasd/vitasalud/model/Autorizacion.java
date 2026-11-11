package edu.uasd.vitasalud.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Es una entidad de la tabla autorizacion (AUT).
 * Basicamente esta se encarga de representar la cabecera de una autorizacion y sus datos de negocio.
 * Un record agrupa los campos; los accesorios se llaman id(), estado(), etc.
 * afiliadoNombre y prestadorNombre provienen de JOIN para mostrar en pantalla.
 * NO decide si se puede aprobar: la regla pertenece a AutorizacionService.
 */
public record Autorizacion(Long id, String numero, long afiliadoId, long prestadorId,
        long usuarioSolicitaId, String afiliadoNombre, String prestadorNombre,
        LocalDateTime fechaSolicitud, String tipoServicio, LocalDate fechaServicio,
        String tipoTelefono, String medicoPrescriptor,
        String telefonoContacto, String correoContacto, String diagnosticoCie10,
        String diagnosticoDescripcion, boolean origenLaboral, BigDecimal montoSolicitado,
        BigDecimal montoCubierto, BigDecimal copago, EstadoAutorizacion estado,
        String notaRevision, LocalDate fechaVencimiento) {
}
