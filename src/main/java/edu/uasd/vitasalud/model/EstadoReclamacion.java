package edu.uasd.vitasalud.model;

/** Ciclo de una reclamación (RF-REC-03). */
public enum EstadoReclamacion implements ConChip {
    RECIBIDA("Recibida", "chip-pendiente"),
    EN_AUDITORIA("En auditoría", "chip-revision"),
    APROBADA("Aprobada", "chip-aprobada"),
    GLOSADA("Glosada", "chip-pendiente"),
    DEVUELTA("Devuelta", "chip-negada"),
    RECHAZADA("Rechazada", "chip-negada"),
    PAGADA("Pagada", "chip-aprobada");

    private final String etiqueta;
    private final String claseChip;

    EstadoReclamacion(String etiqueta, String claseChip) {
        this.etiqueta = etiqueta;
        this.claseChip = claseChip;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }

    @Override
    public String claseChip() {
        return claseChip;
    }
}
