package edu.uasd.vitasalud.app;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Cambia entre el inicio de sesión y la consola del usuario, y carga las pantallas dentro de ella.
 * Todo el sistema usa una sola {@link Scene} y una sola hoja de estilos ({@code vitasalud.css}).
 */
public final class Navegador {

    /** Lo implementa la pantalla contenedora (barra lateral + encabezado) para navegar por id de {@link Menu.Item}. */
    public interface Shell {
        void irA(String idPantalla);
    }

    private static final String RAIZ_FXML = "/fxml/";
    private static final String CSS = "/css/vitasalud.css";

    private static Stage stage;
    private static Scene scene;
    private static Shell shellActual;
    private static final Map<String, String> busquedasPendientes = new HashMap<>();

    private Navegador() {
    }

    public static void iniciar(Stage escenario) {
        stage = escenario;
        scene = new Scene(new Region(), 1280, 800);
        scene.getStylesheets().add(Objects.requireNonNull(
                Navegador.class.getResource(CSS), "No se encontró " + CSS).toExternalForm());
        stage.setTitle("ARS VitaSalud");
        stage.setScene(scene);
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        mostrarLogin();
        stage.setMaximized(true);
        stage.show();
    }

    public static void mostrarLogin() {
        shellActual = null;
        scene.setRoot(cargarFxml("comun/login.fxml"));
    }

    /** Abre la consola que corresponde al perfil de la sesión actual. */
    public static void mostrarShell() {
        scene.setRoot(cargarFxml("comun/shell.fxml"));
    }

    public static void cerrarSesion() {
        Sesion.cerrar();
        busquedasPendientes.clear();
        mostrarLogin();
    }

    /** Navega a otra pantalla de la consola abierta (por ejemplo, desde una acción rápida del Panel). */
    public static void irA(String idPantalla) {
        if (shellActual == null) {
            throw new IllegalStateException("No hay una consola abierta");
        }
        shellActual.irA(idPantalla);
    }

    /**
     * Deja un valor de búsqueda para que la pantalla {@code tipo} lo recoja al abrirse (búsqueda global,
     * RF-SEG-08). {@code tipo} es el id de la pantalla: {@code "afiliados"} (valor = número de afiliado),
     * {@code "bandeja"} (valor = número de autorización) o {@code "prestadores"} (valor = RNC/cédula).
     * <p>
     * <b>Para quien hace esas pantallas:</b> en {@code initialize()} llama a
     * {@code Navegador.consumirBusquedaPendiente("afiliados").ifPresent(v -> { txtBuscar.setText(v); buscar(); })}.
     */
    public static void setBusquedaPendiente(String tipo, String valor) {
        busquedasPendientes.put(tipo, valor);
    }

    /**
     * Devuelve y borra el valor dejado con {@link #setBusquedaPendiente}; vacío si no hay ninguno.
     * Al consumirlo no se vuelve a aplicar la próxima vez que se abra la pantalla.
     */
    public static Optional<String> consumirBusquedaPendiente(String tipo) {
        return Optional.ofNullable(busquedasPendientes.remove(tipo));
    }

    public static void registrarShell(Shell shell) {
        shellActual = shell;
    }

    /** Carga un FXML. {@code ruta} es relativa a {@code /fxml/}, por ejemplo {@code "interno/panel.fxml"}. */
    public static Parent cargarFxml(String ruta) {
        URL url = Navegador.class.getResource(RAIZ_FXML + ruta);
        if (url == null) {
            throw new IllegalArgumentException("No existe el FXML " + RAIZ_FXML + ruta);
        }
        try {
            return FXMLLoader.load(url);
        } catch (IOException e) {
            throw new UncheckedIOException("Error al cargar " + ruta, e);
        }
    }

    /**
     * Carga la vista de una opción del menú. Si su FXML todavía no existe, devuelve un aviso
     * "en construcción" con el responsable, para que el menú funcione desde el primer día.
     */
    public static Node cargarVista(Menu.Item item) {
        if (Navegador.class.getResource(RAIZ_FXML + item.fxml()) != null) {
            return cargarFxml(item.fxml());
        }
        Label titulo = new Label("Pantalla en construcción");
        titulo.getStyleClass().add("section-title");
        Label detalle = new Label("Responsable: " + item.responsable()
                + "\nArchivo esperado: src/main/resources/fxml/" + item.fxml());
        detalle.getStyleClass().add("texto-secundario");
        VBox tarjeta = new VBox(8, titulo, detalle);
        tarjeta.getStyleClass().add("card");
        tarjeta.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        StackPane envoltorio = new StackPane(tarjeta);
        envoltorio.setAlignment(Pos.TOP_LEFT);
        return envoltorio;
    }
}
