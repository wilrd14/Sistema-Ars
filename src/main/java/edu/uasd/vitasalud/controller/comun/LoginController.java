package edu.uasd.vitasalud.controller.comun;

import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Usuario;
import edu.uasd.vitasalud.service.ServicioException;
import edu.uasd.vitasalud.service.UsuarioService;
import edu.uasd.vitasalud.util.Alertas;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/** Inicio de sesión con selector de perfil Prestador / Personal interno (RF-SEG-01). */
public class LoginController {

    @FXML private ToggleGroup grupoPerfil;
    @FXML private ToggleButton tglPrestador;
    @FXML private ToggleButton tglInterno;
    @FXML private Label lblUsuario;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblError;
    @FXML private Button btnAcceder;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    private void initialize() {
        // El selector siempre debe tener un perfil elegido: no se permite deseleccionar.
        grupoPerfil.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            } else {
                lblUsuario.setText(perfilElegido().etiquetaUsuario());
                ocultarError();
            }
        });
        tglPrestador.setUserData(Perfil.PRESTADOR);
        tglInterno.setUserData(Perfil.INTERNO);
        lblUsuario.setText(perfilElegido().etiquetaUsuario());
        ocultarError();
    }

    @FXML
    private void acceder() {
        ocultarError();
        String usuario = txtUsuario.getText();
        String clave = txtClave.getText();
        Perfil perfil = perfilElegido();

        // La consulta a MySQL va en segundo plano para no congelar la ventana si el servidor tarda.
        Task<Usuario> tarea = new Task<>() {
            @Override
            protected Usuario call() {
                return usuarioService.autenticar(usuario, clave, perfil);
            }
        };
        tarea.setOnRunning(e -> btnAcceder.setDisable(true));
        tarea.setOnSucceeded(e -> {
            btnAcceder.setDisable(false);
            Sesion.iniciar(tarea.getValue());
            Navegador.mostrarShell();
        });
        tarea.setOnFailed(e -> {
            btnAcceder.setDisable(false);
            Throwable causa = tarea.getException();
            mostrarError(causa instanceof ServicioException
                    ? causa.getMessage()
                    : "Ocurrió un error inesperado. Revisa el archivo de log.");
        });
        Thread hilo = new Thread(tarea, "login");
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void mostrarAyuda() {
        Alertas.info("¿Necesitas ayuda para acceder?",
                "Comunícate con el administrador de ARS VitaSalud para recuperar o desbloquear tu acceso.");
    }

    private Perfil perfilElegido() {
        Toggle seleccionado = grupoPerfil.getSelectedToggle();
        return seleccionado == null ? Perfil.PRESTADOR : (Perfil) seleccionado.getUserData();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setManaged(true);
        lblError.setVisible(true);
    }

    private void ocultarError() {
        lblError.setManaged(false);
        lblError.setVisible(false);
    }
}
