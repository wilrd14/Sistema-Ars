package edu.uasd.vitasalud.model;
import java.time.LocalDateTime;
/** RF-PRE-06 P2: registro de ingreso/egreso vinculado a autorización aprobada. */
public record Hospitalizacion(long id, long autorizacionId, LocalDateTime ingreso,
                              LocalDateTime egreso, String observacion) { }
