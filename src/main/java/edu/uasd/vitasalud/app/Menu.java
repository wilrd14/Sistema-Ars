package edu.uasd.vitasalud.app;

import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static edu.uasd.vitasalud.model.Rol.ADMINISTRADOR;
import static edu.uasd.vitasalud.model.Rol.AGENTE_AFILIACION;
import static edu.uasd.vitasalud.model.Rol.ANALISTA_AUTORIZACIONES;
import static edu.uasd.vitasalud.model.Rol.AUDITOR_MEDICO;
import static edu.uasd.vitasalud.model.Rol.PRESTADOR;

/**
 * Catálogo de pantallas de las dos consolas (PRD 11.4) y de qué rol ve cada una (RF-SEG-03).
 * <p>
 * <b>Para agregar tu pantalla:</b> crea tu FXML en la ruta indicada en {@code fxml} y ya aparece en el
 * menú; mientras el archivo no exista se muestra una pantalla "en construcción" con tu nombre.
 * La ruta es relativa a {@code /fxml/}.
 */
public final class Menu {

    /**
     * @param id           identificador estable para navegar desde código ({@code Navegador.irA("afiliados")})
     * @param grupo        título de grupo en la barra lateral (solo Portal del prestador); null si no hay
     * @param titulo       texto del menú y título del encabezado
     * @param subtitulo    texto de apoyo bajo el título del encabezado
     * @param fxml         ruta del FXML relativa a /fxml/
     * @param roles        roles que ven la opción
     * @param responsable  dueño de la pantalla (para el aviso "en construcción")
     */
    public record Item(String id, String grupo, String titulo, String subtitulo, String fxml,
                       Set<Rol> roles, String responsable) {
    }

    private static final String WILLIAMS = "Williams Villavizar";
    private static final String LIANNELIS = "Liannelis Pérez";
    private static final String LUIS = "Luis Miguel Magallanes";
    private static final String JOEL = "Joel Marte";

    private static final Set<Rol> INTERNOS = EnumSet.of(
            ADMINISTRADOR, ANALISTA_AUTORIZACIONES, AUDITOR_MEDICO, AGENTE_AFILIACION);

    private static final List<Item> INTERNO = List.of(
            new Item("panel", null, "Panel", "Resumen de la operación de hoy",
                    "interno/panel.fxml", INTERNOS, WILLIAMS),
            new Item("bandeja", null, "Bandeja de autorizaciones", "Revisa y decide las solicitudes de los prestadores",
                    "interno/bandeja_autorizaciones.fxml",
                    EnumSet.of(ADMINISTRADOR, ANALISTA_AUTORIZACIONES, AUDITOR_MEDICO), JOEL),
            new Item("reclamaciones", null, "Reclamaciones", "Audita las reclamaciones presentadas por los prestadores",
                    "interno/reclamaciones.fxml", EnumSet.of(ADMINISTRADOR, AUDITOR_MEDICO), JOEL),
            new Item("afiliados", null, "Afiliados", "Busca y consulta afiliados y dependientes",
                    "interno/afiliados.fxml",
                    EnumSet.of(ADMINISTRADOR, ANALISTA_AUTORIZACIONES, AGENTE_AFILIACION), LIANNELIS),
            new Item("nuevo-afiliado", null, "Nuevo afiliado", "Registra un titular y sus dependientes",
        "interno/NuevoAfiliado.fxml", EnumSet.of(ADMINISTRADOR, AGENTE_AFILIACION), LIANNELIS),
            new Item("prestadores", null, "Red de prestadores", "Clínicas, laboratorios y médicos contratados",
                    "interno/prestadores.fxml", EnumSet.of(ADMINISTRADOR, ANALISTA_AUTORIZACIONES), LIANNELIS),
            new Item("planes", null, "Planes y coberturas", "Planes, coberturas, procedimientos y tarifario",
                    "interno/planes.fxml",
                    EnumSet.of(ADMINISTRADOR, ANALISTA_AUTORIZACIONES, AGENTE_AFILIACION), LUIS),
            new Item("reportes", null, "Reportes", "Indicadores por período, estado y prestador",
                    "interno/reportes.fxml", EnumSet.of(ADMINISTRADOR, ANALISTA_AUTORIZACIONES, AUDITOR_MEDICO), WILLIAMS),
            new Item("usuarios", null, "Usuarios y roles", "Cuentas de acceso y alcance de cada rol",
                    "interno/usuarios.fxml", EnumSet.of(ADMINISTRADOR), WILLIAMS));

    private static final String GRUPO_AUT = "AUTORIZACIONES";
    private static final String GRUPO_GESTION = "GESTIÓN";
    private static final Set<Rol> SOLO_PRESTADOR = EnumSet.of(PRESTADOR);

    private static final List<Item> PRESTADOR_ITEMS = List.of(
            new Item("inicio", GRUPO_AUT, "Inicio", "Tus autorizaciones recientes y accesos directos",
                    "prestador/inicio.fxml", SOLO_PRESTADOR, LUIS),
            new Item("solicitar", GRUPO_AUT, "Solicitar autorización", "Registra una nueva solicitud en tres pasos",
                    "prestador/solicitar_autorizacion.fxml", SOLO_PRESTADOR, JOEL),
            new Item("busqueda", GRUPO_AUT, "Búsqueda avanzada", "Consulta tus autorizaciones por número, asegurado o fechas",
                    "prestador/busqueda_avanzada.fxml", SOLO_PRESTADOR, LUIS),
            new Item("precertificaciones", GRUPO_AUT, "Precertificaciones", "Confirma procedimientos programados ya aprobados",
                    "prestador/precertificaciones.fxml", SOLO_PRESTADOR, LUIS),
            new Item("hospitalizacion", GRUPO_AUT, "Hospitalización", "Registro de ingreso y egreso de pacientes",
                    "prestador/hospitalizacion.fxml", SOLO_PRESTADOR, JOEL),
            new Item("mis-solicitudes", GRUPO_GESTION, "Mis solicitudes", "Estado, nota de revisión y monto cubierto",
                    "prestador/mis_solicitudes.fxml", SOLO_PRESTADOR, LUIS),
            new Item("reportes-prestador", GRUPO_GESTION, "Reportes", "Estados de cuenta, devoluciones y cheques",
                    "prestador/reportes.fxml", SOLO_PRESTADOR, LUIS));

    private Menu() {
    }

    /** Opciones que el rol dado puede ver, en el orden del menú de su perfil. */
    public static List<Item> paraRol(Rol rol) {
        List<Item> visibles = new ArrayList<>();
        List<Item> origen = rol.perfil() == Perfil.PRESTADOR ? PRESTADOR_ITEMS : INTERNO;
        for (Item item : origen) {
            if (item.roles().contains(rol)) {
                visibles.add(item);
            }
        }
        return visibles;
    }
}
