package edu.uasd.vitasalud.model;

/**
 * Roles del sistema (PRD sección 5). {@code codigo} es el valor guardado en la tabla {@code rol}.
 */
public enum Rol {
    PRESTADOR("Prestador", Perfil.PRESTADOR,
            "Solicita autorizaciones, consulta su estado, presenta reclamaciones y consulta sus reportes.",
            "Solo la información de su propio centro."),
    ANALISTA_AUTORIZACIONES("Analista de autorizaciones", Perfil.INTERNO,
            "Revisa la bandeja; aprueba, niega o pide información adicional.",
            "Todas las autorizaciones; afiliados, planes y red en modo consulta."),
    AUDITOR_MEDICO("Auditor médico", Perfil.INTERNO,
            "Audita reclamaciones: aprueba, glosa, devuelve o rechaza, y registra pagos.",
            "Reclamaciones y autorizaciones; no modifica la red de prestadores."),
    AGENTE_AFILIACION("Agente de afiliación", Perfil.INTERNO,
            "Registra afiliados y dependientes, cambia estados y planes.",
            "Afiliados; planes en modo consulta."),
    ADMINISTRADOR("Administrador", Perfil.INTERNO,
            "Gestiona usuarios, roles, la red de prestadores, planes y catálogos; consulta la bitácora y los reportes.",
            "Acceso completo.");

    private final String etiqueta;
    private final Perfil perfil;
    private final String descripcion;
    private final String alcance;

    Rol(String etiqueta, Perfil perfil, String descripcion, String alcance) {
        this.etiqueta = etiqueta;
        this.perfil = perfil;
        this.descripcion = descripcion;
        this.alcance = alcance;
    }

    public String codigo() {
        return name();
    }

    public String etiqueta() {
        return etiqueta;
    }

    public Perfil perfil() {
        return perfil;
    }

    public String descripcion() {
        return descripcion;
    }

    public String alcance() {
        return alcance;
    }

    public static Rol deCodigo(String codigo) {
        return valueOf(codigo);
    }
}
