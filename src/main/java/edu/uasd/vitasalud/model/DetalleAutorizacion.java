package edu.uasd.vitasalud.model;

import java.math.BigDecimal;

/**
 * Esta es la que representa un procedimiento asociado a una autorizacion. 
 * Es la linea de una autorizacion: es to incluye procedimiento, cantidad y montos; una autorizacion
 * tiene 1..N lineas.
 */
public record DetalleAutorizacion(Long id, long autorizacionId, long procedimientoId,
        String codigo, String descripcion, String categoria, int cantidad,
        BigDecimal montoSolicitado, BigDecimal montoCubierto) {
}
