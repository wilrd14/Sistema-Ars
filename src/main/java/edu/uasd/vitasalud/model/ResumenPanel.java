package edu.uasd.vitasalud.model;

import java.util.List;

/**
 * Datos del Panel (RF-REP-01). {@code aviso} es null si todo cargó bien; si la base de datos falló,
 * los contadores quedan en 0 y {@code aviso} explica el motivo.
 */
public record ResumenPanel(long pendientes, long enRevision, long aprobadas, long negadas,
                           List<String> atencion, String aviso) {

    public static ResumenPanel vacio(String aviso) {
        return new ResumenPanel(0, 0, 0, 0, List.of(), aviso);
    }
}
