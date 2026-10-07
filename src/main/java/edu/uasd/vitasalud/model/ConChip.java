package edu.uasd.vitasalud.model;

/**
 * Un estado que se muestra como chip (PRD 11.2). Los estados siempre llevan texto, nunca solo color.
 * Las clases CSS disponibles son: chip-pendiente, chip-revision, chip-aprobada y chip-negada.
 */
public interface ConChip {

    /** Texto visible del chip. */
    String etiqueta();

    /** Clase CSS de {@code vitasalud.css} que da el color al chip. */
    String claseChip();
}
