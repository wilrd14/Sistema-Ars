package edu.uasd.vitasalud.model;
import java.math.BigDecimal;
/** Catálogo de procedimientos; se usa para elegir las líneas de una solicitud. */
public record ProcedimientoBusqueda(long id, String codigo, String descripcion,
                                    String categoria, BigDecimal precioReferencia) {
    @Override public String toString() { return codigo + " - " + descripcion; }
}
