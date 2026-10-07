package edu.uasd.vitasalud.controller.comun;

import edu.uasd.vitasalud.app.Menu;
import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.model.Usuario;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estructura común de la Consola interna y del Portal del prestador (RF-SEG-02 y RF-SEG-03):
 * barra lateral con las opciones que permite el rol, encabezado con título y usuario, y el área
 * de contenido donde se cargan las pantallas de {@link Menu}.
 */
public class ShellController implements Navegador.Shell {

    @FXML private Label lblSubtituloMarca;
    @FXML private VBox navBox;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblAvatar;
    @FXML private Label lblNombre;
    @FXML private Label lblRol;
    @FXML private StackPane contenido;

    private final ToggleGroup grupoNav = new ToggleGroup();
    private final Map<String, ToggleButton> botones = new HashMap<>();
    private final Map<String, Menu.Item> items = new HashMap<>();

    @FXML
    private void initialize() {
        Usuario u = Sesion.getUsuario();
        lblSubtituloMarca.setText(u.getPerfil().subtituloMarca());
        lblNombre.setText(u.getNombre());
        lblAvatar.setText(u.iniciales());
        lblRol.setText(u.getCargo() != null && !u.getCargo().isBlank() ? u.getCargo() : u.getRol().etiqueta());

        // Evita que un clic sobre la opción activa la deje sin selección.
        grupoNav.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            }
        });

        List<Menu.Item> opciones = Menu.paraRol(u.getRol());
        String grupoActual = null;
        for (Menu.Item item : opciones) {
            if (item.grupo() != null && !item.grupo().equals(grupoActual)) {
                grupoActual = item.grupo();
                Label titulo = new Label(grupoActual);
                titulo.getStyleClass().add("nav-group");
                navBox.getChildren().add(titulo);
            }
            ToggleButton boton = new ToggleButton(item.titulo());
            boton.getStyleClass().add("nav");
            boton.setToggleGroup(grupoNav);
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setAlignment(Pos.CENTER_LEFT);
            boton.setOnAction(e -> mostrar(item));
            navBox.getChildren().add(boton);
            botones.put(item.id(), boton);
            items.put(item.id(), item);
        }

        Navegador.registrarShell(this);
        if (!opciones.isEmpty()) {
            irA(opciones.get(0).id());
        }
    }

    @Override
    public void irA(String idPantalla) {
        Menu.Item item = items.get(idPantalla);
        if (item == null) {
            throw new IllegalArgumentException("Tu rol no tiene acceso a la pantalla '" + idPantalla + "'");
        }
        botones.get(idPantalla).setSelected(true);
        mostrar(item);
    }

    @FXML
    private void cerrarSesion() {
        Navegador.cerrarSesion();
    }

    private void mostrar(Menu.Item item) {
        lblTitulo.setText(item.titulo());
        lblSubtitulo.setText(item.subtitulo());
        contenido.getChildren().setAll(Navegador.cargarVista(item));
    }
}
