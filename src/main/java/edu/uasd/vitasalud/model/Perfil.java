package edu.uasd.vitasalud.model;

/** Perfil de acceso elegido en el inicio de sesión: define qué consola se abre. */
public enum Perfil {
    INTERNO("Personal interno", "Usuario corporativo", "Consola interna"),
    PRESTADOR("Prestador", "Usuario del prestador", "Portal de prestadores");

    private final String etiqueta;
    private final String etiquetaUsuario;
    private final String subtituloMarca;

    Perfil(String etiqueta, String etiquetaUsuario, String subtituloMarca) {
        this.etiqueta = etiqueta;
        this.etiquetaUsuario = etiquetaUsuario;
        this.subtituloMarca = subtituloMarca;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Texto de la etiqueta del campo usuario en el login. */
    public String etiquetaUsuario() {
        return etiquetaUsuario;
    }

    /** Subtítulo bajo el logotipo en la barra lateral. */
    public String subtituloMarca() {
        return subtituloMarca;
    }
}
