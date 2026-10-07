package edu.uasd.vitasalud.app;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;

/** Punto de entrada de ARS VitaSalud. Ejecutar con {@code ./mvnw javafx:run}. */
public class MainApp extends Application {

    private static final String[] FUENTES = {
            "Figtree-Regular.ttf", "Figtree-Medium.ttf", "Figtree-SemiBold.ttf", "Figtree-Bold.ttf"};

    @Override
    public void start(Stage stage) {
        cargarFuentes();
        Navegador.iniciar(stage);
    }

    /**
     * Carga Figtree desde {@code resources/fonts}. Si los archivos aún no están en el repositorio,
     * el CSS usa "Segoe UI" como respaldo (PRD 11.3).
     */
    private void cargarFuentes() {
        for (String archivo : FUENTES) {
            try (InputStream in = MainApp.class.getResourceAsStream("/fonts/" + archivo)) {
                if (in != null) {
                    Font.loadFont(in, 14);
                }
            } catch (Exception ignorada) {
                // La fuente es opcional: se usa el respaldo del CSS.
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
