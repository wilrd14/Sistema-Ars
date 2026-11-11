package edu.uasd.vitasalud.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Esta es la entidad de la tabla reclamacion (REC). Representa una reclamación de una autorización aprobada.
 * Una reclamación es un reclamo de pago de una factura medica asociada a una autorización aprobada. 
 * La reclamación puede ser aprobada, rechazada o devuelta para corrección.
 * Entidad REC: una autorización aprobada tiene como máximo una reclamación
 * (UNIQUE SQL).
 */
public record Reclamacion(Long id, String numero, long autorizacionId, long prestadorId,
        String numeroAutorizacion, String prestadorNombre, String facturaPrestador,
        LocalDate fechaServicio, BigDecimal montoReclamado, BigDecimal montoGlosado,
        BigDecimal montoPagar, EstadoReclamacion estado, String motivoDevolucion,
        LocalDate fechaPago, String referenciaPago) {
}
