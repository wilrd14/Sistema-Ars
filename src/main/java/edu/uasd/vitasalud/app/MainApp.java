package edu.uasd.vitasalud.app;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Punto de entrada de ARS VitaSalud. Ejecutar con {@code ./mvnw javafx:run}. */
public class MainApp extends Application {

    private static final String[] FUENTES = {
            "Figtree-Regular.ttf", "Figtree-Medium.ttf", "Figtree-SemiBold.ttf", "Figtree-Bold.ttf"};

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    @Override
    public void start(Stage stage) {
        cargarFuentes();
        Navegador.iniciar(stage);
    }

    /**
     * Carga Figtree desde {@code resources/fonts}. Si algún archivo falta,
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

    /**
     * Envía los registros del sistema a {@code logs/vitasalud.log} (RF-SEG-10) para que los errores de
     * conexión queden guardados aunque el usuario solo vea un mensaje comprensible.
     */
    private static void configurarLog() {
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler archivo = new FileHandler("logs/vitasalud.log", 1_000_000, 3, true);
            archivo.setEncoding("UTF-8");
            archivo.setFormatter(new SimpleFormatter());
            Logger raiz = Logger.getLogger("");
            raiz.addHandler(archivo);
            raiz.setLevel(Level.INFO);
        } catch (IOException | SecurityException e) {
            LOG.log(Level.WARNING, "No se pudo abrir logs/vitasalud.log; se registra solo en consola", e);
        }
        Thread.setDefaultUncaughtExceptionHandler(
                (hilo, error) -> LOG.log(Level.SEVERE, "Error no controlado en el hilo " + hilo.getName(), error));
    }

    public static void main(String[] args) {
        configurarLog();
        launch(args);
    }
}
