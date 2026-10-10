package edu.uasd.vitasalud.model;

import java.util.EnumSet;
import java.util.Set;

import static edu.uasd.vitasalud.model.Rol.ADMINISTRADOR;
import static edu.uasd.vitasalud.model.Rol.ANALISTA_AUTORIZACIONES;
import static edu.uasd.vitasalud.model.Rol.AUDITOR_MEDICO;

/** Reportes de la Consola interna (RF-REP-02) y qué rol puede ver cada uno. */
public enum Reporte {
    AUTORIZACIONES("Autorizaciones por estado", "Volumen y tiempo promedio de respuesta por estado.",
            EnumSet.of(ADMINISTRADOR, ANALISTA_AUTORIZACIONES, AUDITOR_MEDICO)),
    RECLAMACIONES("Reclamaciones por prestador", "Cantidad y montos reclamados, glosados y a pagar.",
            EnumSet.of(ADMINISTRADOR, AUDITOR_MEDICO)),
    DEVOLUCIONES("Devoluciones", "Motivos de devolución y prestadores reincidentes.",
            EnumSet.of(ADMINISTRADOR, AUDITOR_MEDICO)),
    PAGOS("Pagos y cheques", "Reclamaciones pagadas con su referencia de pago.",
            EnumSet.of(ADMINISTRADOR, AUDITOR_MEDICO)),
    PRECERTIFICACIONES("Precertificaciones", "Precertificaciones por estado.",
            EnumSet.of(ADMINISTRADOR, ANALISTA_AUTORIZACIONES)),
    ACTIVIDAD("Actividad de usuarios", "Acciones registradas en la bitácora por usuario y módulo.",
            EnumSet.of(ADMINISTRADOR));

    private final String titulo;
    private final String descripcion;
    private final Set<Rol> roles;

    Reporte(String titulo, String descripcion, Set<Rol> roles) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.roles = roles;
    }

    public String titulo() {
        return titulo;
    }

    public String descripcion() {
        return descripcion;
    }

    public boolean permitidoPara(Rol rol) {
        return roles.contains(rol);
    }
}
