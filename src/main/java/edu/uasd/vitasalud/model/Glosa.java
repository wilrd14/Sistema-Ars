package edu.uasd.vitasalud.model;

import java.math.BigDecimal;

/**
 *Esta representa el importe y motivo objetado de una reclamacion. 
 Durante la auditoria de una reclamacion, el auditor puede objetar parte del monto reclamado.
 * Glosa: es la parte objetada de una factura medica y el motivo de la
 * auditoraa.
 */
public record Glosa(Long id, long reclamacionId, String motivo, BigDecimal monto,
        String observacion, long usuarioId) {
}
