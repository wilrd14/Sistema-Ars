package edu.uasd.vitasalud.app;

/**
 * Clase principal real de la aplicación. No extiende {@code Application} a propósito:
 * si la clase con {@code main} extiende {@code Application} y JavaFX está en el classpath
 * (botón Run de VS Code, JAR ejecutable), Java falla con
 * "JavaFX runtime components are missing". Este lanzador evita ese chequeo.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
