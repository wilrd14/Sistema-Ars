package edu.uasd.vitasalud.model;

/** Ciclo de una autorización (RF-AUT-07). El valor del enum se guarda en la columna {@code estado}. */
public enum EstadoAutorizacion implements ConChip {
    PENDIENTE("Pendiente", "chip-pendiente"),
    EN_REVISION("En revisión", "chip-revision"),
    APROBADA("Aprobada", "chip-aprobada"),
    NEGADA("Negada", "chip-negada"),
    ANULADA("Anulada", "chip-negada"),
    VENCIDA("Vencida", "chip-negada");

    private final String etiqueta;
    private final String claseChip;

    EstadoAutorizacion(String etiqueta, String claseChip) {
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
