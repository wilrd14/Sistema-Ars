package edu.uasd.vitasalud.controller.comun;

import edu.uasd.vitasalud.app.Menu;
import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.model.ResultadoBusqueda;
import edu.uasd.vitasalud.model.ResultadoBusqueda.Tipo;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.service.BusquedaGlobalService;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Campo de búsqueda global del encabezado de la Consola interna (RF-SEG-08): espera 300 ms tras la
 * última tecla, consulta en un hilo de fondo (mínimo 3 caracteres) y muestra los resultados en un
 * {@link Popup} bajo el campo. Ctrl+K enfoca el campo. Al elegir un resultado navega a su pantalla
 * (si el rol la tiene) y deja el valor en {@link Navegador#setBusquedaPendiente}.
 */
final class BuscadorGlobal {

    private static final KeyCodeCombination ATAJO = new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN);

    private final TextField campo;
    private final Set<String> pantallasPermitidas;
    private final BusquedaGlobalService servicio = new BusquedaGlobalService();
    private final PauseTransition retardo = new PauseTransition(Duration.millis(300));
    private final Popup popup = new Popup();
    private final VBox contenido = new VBox(2);
    /** Número de la última búsqueda lanzada: descarta respuestas que llegan tarde. */
    private int ultima;
    private List<ResultadoBusqueda> mostrados = List.of();

    BuscadorGlobal(TextField campo, Rol rol) {
        this.campo = campo;
        this.pantallasPermitidas = Menu.paraRol(rol).stream().map(Menu.Item::id).collect(Collectors.toSet());
    }

    void iniciar() {
        contenido.getStyleClass().add("busqueda-popup");
        popup.getContent().add(contenido);
        popup.setAutoHide(true);
        popup.setAutoFix(true);

        retardo.setOnFinished(e -> buscar());
        campo.textProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null || ahora.trim().length() < BusquedaGlobalService.MIN_CARACTERES) {
                ultima++;
                retardo.stop();
                popup.hide();
            } else {
                retardo.playFromStart();
            }
        });
        campo.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                popup.hide();
            } else if (e.getCode() == KeyCode.ENTER && !mostrados.isEmpty() && popup.isShowing()) {
                elegir(mostrados.get(0));
            }
        });

        // Ctrl+K: el atajo vive en la Scene compartida, así que se quita al salir de esta consola.
        Runnable enfocar = () -> {
            campo.requestFocus();
            campo.selectAll();
        };
        campo.sceneProperty().addListener((obs, anterior, nueva) -> {
            if (anterior != null) {
                anterior.getAccelerators().remove(ATAJO);
            }
            if (nueva != null) {
                registrarAtajo(nueva, enfocar);
            }
        });
        if (campo.getScene() != null) {
            registrarAtajo(campo.getScene(), enfocar);
        }
    }

    private void registrarAtajo(Scene escena, Runnable enfocar) {
        escena.getAccelerators().put(ATAJO, enfocar);
    }

    private void buscar() {
        String termino = campo.getText();
        int numero = ++ultima;
        Task<List<ResultadoBusqueda>> tarea = new Task<>() {
            @Override
            protected List<ResultadoBusqueda> call() {
                return servicio.buscar(termino);
            }
        };
        tarea.setOnSucceeded(e -> {
            if (numero == ultima) {
                mostrar(tarea.getValue());
            }
        });
        tarea.setOnFailed(e -> {
            if (numero == ultima) {
                mostrarMensaje(tarea.getException().getMessage());
            }
        });
        Thread hilo = new Thread(tarea, "busqueda-global");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrar(List<ResultadoBusqueda> resultados) {
        // Solo se ofrecen los tipos cuya pantalla puede abrir el rol (RF-SEG-03).
        mostrados = resultados.stream().filter(r -> pantallasPermitidas.contains(r.tipo().pantallaId())).toList();
        if (mostrados.isEmpty()) {
            mostrarMensaje("Sin resultados para \"" + campo.getText().trim() + "\".");
            return;
        }
        contenido.getChildren().clear();
        for (Tipo tipo : Tipo.values()) {
            List<ResultadoBusqueda> delTipo = mostrados.stream().filter(r -> r.tipo() == tipo).toList();
            if (delTipo.isEmpty()) {
                continue;
            }
            Label grupo = new Label(tipo.etiqueta().toUpperCase() + "S");
            grupo.getStyleClass().add("busqueda-grupo");
            contenido.getChildren().add(grupo);
            for (ResultadoBusqueda r : delTipo) {
                contenido.getChildren().add(fila(r));
            }
        }
        abrirPopup();
    }

    private VBox fila(ResultadoBusqueda r) {
        Label titulo = new Label(r.titulo());
        titulo.getStyleClass().add("busqueda-item-titulo");
        Label detalle = new Label(r.detalle());
        detalle.getStyleClass().add("texto-secundario");
        VBox fila = new VBox(1, titulo, detalle);
        fila.getStyleClass().add("busqueda-item");
        fila.setOnMouseClicked(e -> elegir(r));
        return fila;
    }

    private void mostrarMensaje(String mensaje) {
        mostrados = List.of();
        Label aviso = new Label(mensaje);
        aviso.getStyleClass().add("busqueda-vacio");
        aviso.setWrapText(true);
        contenido.getChildren().setAll(aviso);
        abrirPopup();
    }

    private void abrirPopup() {
        if (campo.getScene() == null || campo.getScene().getWindow() == null) {
            return;
        }
        var borde = campo.localToScreen(0, campo.getHeight() + 4);
        if (borde != null) {
            popup.show(campo, borde.getX(), borde.getY());
        }
    }

    private void elegir(ResultadoBusqueda r) {
        popup.hide();
        String pantalla = r.tipo().pantallaId();
        if (!pantallasPermitidas.contains(pantalla)) {
            return;
        }
        Navegador.setBusquedaPendiente(pantalla, r.clave());
        Navegador.irA(pantalla);
        campo.clear();
    }
}
