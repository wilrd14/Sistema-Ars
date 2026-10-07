package edu.uasd.vitasalud.app;

import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;

/**
 * Sesión del usuario conectado. Los servicios y DAO la consultan para aplicar permisos por rol
 * y el filtro por prestador (RN-07): un usuario PRESTADOR solo ve registros de {@link #getPrestadorId()}.
 */
public final class Sesion {

    private static Usuario usuario;

    private Sesion() {
    }

    public static void iniciar(Usuario u) {
        usuario = u;
    }

    public static void cerrar() {
        usuario = null;
    }

    public static boolean activa() {
        return usuario != null;
    }

    public static Usuario getUsuario() {
        if (usuario == null) {
            throw new IllegalStateException("No hay una sesión iniciada");
        }
        return usuario;
    }

    public static Perfil getPerfil() {
        return getUsuario().getPerfil();
    }

    public static Rol getRol() {
        return getUsuario().getRol();
    }

    /** Id del prestador del usuario conectado; null si es personal interno. */
    public static Long getPrestadorId() {
        return getUsuario().getPrestadorId();
    }
}
