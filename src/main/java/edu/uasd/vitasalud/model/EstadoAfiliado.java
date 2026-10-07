package edu.uasd.vitasalud.model;

/** Estado de afiliación (RF-AFI-07). Sirve también para el estado del contrato de un prestador. */
public enum EstadoAfiliado implements ConChip {
    ACTIVO("Activo", "chip-aprobada"),
    SUSPENDIDO("Suspendido", "chip-negada"),
    CANCELADO("Cancelado", "chip-negada");

    private final String etiqueta;
    private final String claseChip;

    EstadoAfiliado(String etiqueta, String claseChip) {
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
