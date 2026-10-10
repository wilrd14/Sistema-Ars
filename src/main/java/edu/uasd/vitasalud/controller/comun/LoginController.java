package edu.uasd.vitasalud.controller.comun;

import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.config.AppConfig;
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

import java.util.logging.Level;
import java.util.logging.Logger;

/** Inicio de sesión con selector de perfil Prestador / Personal interno (RF-SEG-01). */
public class LoginController {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

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
            if (causa instanceof ServicioException) {
                mostrarError(causa.getMessage());
            } else if (causa instanceof IllegalStateException) {
                // Configuración ausente (db.properties): el mensaje ya explica cómo corregirlo.
                LOG.log(Level.SEVERE, "Configuración inválida", causa);
                mostrarError(causa.getMessage());
            } else {
                LOG.log(Level.SEVERE, "Error inesperado al iniciar sesión", causa);
                mostrarError("Ocurrió un error inesperado. Revisa el archivo logs/vitasalud.log.");
            }
        });
        Thread hilo = new Thread(tarea, "login");
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void mostrarAyuda() {
        // Si db.properties no se puede leer, se muestran los datos por defecto en vez de fallar.
        String nombre = "Administrador de ARS VitaSalud";
        String correo = "soporte@vitasalud.example";
        String telefono = "809-555-0100";
        String horario = "Lunes a viernes, 8:00 a.m. - 5:00 p.m.";
        try {
            nombre = AppConfig.get("ayuda.contacto.nombre", nombre);
            correo = AppConfig.get("ayuda.contacto.correo", correo);
            telefono = AppConfig.get("ayuda.contacto.telefono", telefono);
            horario = AppConfig.get("ayuda.contacto.horario", horario);
        } catch (IllegalStateException ignorada) {
            // Sin db.properties se usan los valores por defecto.
        }
        Alertas.info("¿Necesitas ayuda para acceder?",
                "Si olvidaste tu contraseña o tu cuenta fue bloqueada, solicita al administrador que la restablezca "
                        + "o la desbloquee.\n\n"
                        + nombre + "\nCorreo: " + correo + "\nTeléfono: " + telefono + "\nHorario: " + horario);
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
