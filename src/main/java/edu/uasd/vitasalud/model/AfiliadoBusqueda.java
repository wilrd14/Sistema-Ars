package edu.uasd.vitasalud.model;
import java.time.LocalDate;
/** Resultado mínimo de la búsqueda de afiliados; no sustituye el modelo AFI de Liannelis. */
public record AfiliadoBusqueda(long id, String numero, String documento, String nss,
                              String nombres, String apellidos, String estado,
                              LocalDate nacimiento) {
    public String nombreCompleto() { return nombres + " " + apellidos; }
}
