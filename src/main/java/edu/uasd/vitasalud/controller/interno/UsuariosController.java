package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.UsuarioDAO.PrestadorOpcion;
import edu.uasd.vitasalud.model.ConChip;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;
import edu.uasd.vitasalud.service.ServicioException;
import edu.uasd.vitasalud.service.UsuarioService;
import edu.uasd.vitasalud.util.Alertas;
import edu.uasd.vitasalud.util.Chips;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Gestión de usuarios y roles (RF-SEG-04, RF-SEG-05, RF-SEG-06). Solo Administrador. */
public class UsuariosController {

    private static final Logger LOG = Logger.getLogger(UsuariosController.class.getName());

    @FXML private HBox boxRoles;
    @FXML private TextField txtFiltro;
    @FXML private TableView<Usuario> tabla;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colPerfil;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colPrestador;
    @FXML private TableColumn<Usuario, String> colEstado;

    @FXML private Label lblTituloForm;
    @FXML private TextField txtNombre;
    @FXML private TextField txtUsername;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private Label lblPerfil;
    @FXML private TextField txtCargo;
    @FXML private VBox boxPrestador;
    @FXML private ComboBox<PrestadorOpcion> cmbPrestador;
    @FXML private Label lblClave;
    @FXML private PasswordField txtClave;
    @FXML private Label lblAyudaClave;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Button btnClave;
    @FXML private Button btnDesbloquear;
    @FXML private Button btnEstado;

    private final UsuarioService service = new UsuarioService();
    /** Usuario en edición; null = formulario de alta. */
    private Usuario seleccionado;

    @FXML
    private void initialize() {
        construirTarjetasRoles();
        configurarTabla();

        cmbRol.setConverter(new StringConverter<>() {
            @Override
            public String toString(Rol rol) {
                return rol == null ? "" : rol.etiqueta();
            }

            @Override
            public Rol fromString(String texto) {
                return null;
            }
        });
        cmbRol.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> alCambiarRol(nuevo));

        try {
            cmbPrestador.setItems(FXCollections.observableArrayList(service.listarPrestadores()));
        } catch (ServicioException e) {
            mostrarError(e.getMessage());
        }

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                editar(nuevo);
            }
        });
        nuevo();
        buscar();
    }

    // ------------------------------------------------------------------ RF-SEG-05: tarjetas por rol

    private void construirTarjetasRoles() {
        for (Rol rol : Rol.values()) {
            Label titulo = new Label(rol.etiqueta());
            titulo.getStyleClass().add("section-title");
            Label descripcion = new Label(rol.descripcion());
            descripcion.setWrapText(true);
            Label alcance = new Label("Alcance de datos: " + rol.alcance());
            alcance.getStyleClass().add("texto-secundario");
            alcance.setWrapText(true);

            VBox tarjeta = new VBox(8, titulo, descripcion, alcance);
            tarjeta.getStyleClass().add("card");
            tarjeta.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tarjeta, Priority.ALWAYS);
            tarjeta.setPrefWidth(10);
            boxRoles.getChildren().add(tarjeta);
        }
    }

    // ------------------------------------------------------------------ tabla

    private void configurarTabla() {
        colNombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));
        colUsuario.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));
        colPerfil.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPerfil().etiqueta()));
        colRol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getRol().etiqueta()));
        colPrestador.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getPrestadorNombre() == null ? "—" : c.getValue().getPrestadorNombre()));
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEstado()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label chip = new Label();

            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                } else {
                    Chips.aplicar(chip, chipDeEstado(estado));
                    setGraphic(chip);
                }
            }
        });
    }

    private static ConChip chipDeEstado(String estado) {
        return switch (estado) {
            case Usuario.ESTADO_ACTIVO -> chip("Activo", "chip-aprobada");
            case Usuario.ESTADO_BLOQUEADO -> chip("Bloqueado", "chip-pendiente");
            default -> chip("Inactivo", "chip-negada");
        };
    }

    private static ConChip chip(String etiqueta, String clase) {
        return new ConChip() {
            @Override
            public String etiqueta() {
                return etiqueta;
            }

            @Override
            public String claseChip() {
                return clase;
            }
        };
    }

    @FXML
    private void buscar() {
        try {
            List<Usuario> usuarios = service.listar(txtFiltro.getText());
            tabla.setItems(FXCollections.observableArrayList(usuarios));
        } catch (ServicioException e) {
            Alertas.error("Usuarios", e.getMessage());
        } catch (RuntimeException e) {
            falloInesperado(e);
        }
    }

    // ------------------------------------------------------------------ formulario

    /** Deja el formulario listo para crear un usuario. */
    @FXML
    private void nuevo() {
        seleccionado = null;
        tabla.getSelectionModel().clearSelection();
        lblTituloForm.setText("Nuevo usuario");
        txtNombre.clear();
        txtUsername.clear();
        txtUsername.setDisable(false);
        txtCargo.clear();
        txtClave.clear();
        cmbPrestador.getSelectionModel().clearSelection();
        cmbRol.setItems(FXCollections.observableArrayList(Rol.values()));
        cmbRol.getSelectionModel().select(Rol.AGENTE_AFILIACION);
        lblClave.setText("Contraseña inicial");
        lblAyudaClave.setText("Mínimo " + UsuarioService.CLAVE_MINIMA + " caracteres, con letras y números.");
        btnGuardar.setText("Crear usuario");
        alternarAcciones(false);
        ocultarError();
    }

    private void editar(Usuario u) {
        seleccionado = u;
        lblTituloForm.setText("Editar usuario");
        txtNombre.setText(u.getNombre());
        txtUsername.setText(u.getUsername());
        txtUsername.setDisable(true);
        txtCargo.setText(u.getCargo() == null ? "" : u.getCargo());
        txtClave.clear();
        // El perfil no cambia al editar: solo se ofrecen los roles de ese perfil.
        cmbRol.setItems(FXCollections.observableArrayList(
                List.of(Rol.values()).stream().filter(r -> r.perfil() == u.getPerfil()).toList()));
        cmbRol.getSelectionModel().select(u.getRol());
        cmbPrestador.getSelectionModel().clearSelection();
        if (u.getPrestadorId() != null) {
            cmbPrestador.getItems().stream().filter(p -> p.id() == u.getPrestadorId()).findFirst()
                    .ifPresent(p -> cmbPrestador.getSelectionModel().select(p));
        }
        lblClave.setText("Nueva contraseña (solo para restablecer)");
        lblAyudaClave.setText("Déjala vacía si solo editas los datos.");
        btnGuardar.setText("Guardar cambios");
        alternarAcciones(true);
        btnEstado.setText(Usuario.ESTADO_ACTIVO.equals(u.getEstado()) || Usuario.ESTADO_BLOQUEADO.equals(u.getEstado())
                ? "Desactivar" : "Activar");
        btnDesbloquear.setVisible(Usuario.ESTADO_BLOQUEADO.equals(u.getEstado()));
        btnDesbloquear.setManaged(btnDesbloquear.isVisible());
        ocultarError();
    }

    private void alternarAcciones(boolean edicion) {
        for (Button b : List.of(btnClave, btnEstado, btnDesbloquear)) {
            b.setVisible(edicion);
            b.setManaged(edicion);
        }
    }

    private void alCambiarRol(Rol rol) {
        boolean prestador = rol != null && rol.perfil() == edu.uasd.vitasalud.model.Perfil.PRESTADOR;
        boxPrestador.setVisible(prestador);
        boxPrestador.setManaged(prestador);
        lblPerfil.setText(rol == null ? "" : "Perfil de acceso: " + rol.perfil().etiqueta());
    }

    // ------------------------------------------------------------------ acciones

    @FXML
    private void guardar() {
        ocultarError();
        Rol rol = cmbRol.getValue();
        PrestadorOpcion prestador = cmbPrestador.getValue();
        Long prestadorId = rol != null && rol.perfil() == edu.uasd.vitasalud.model.Perfil.PRESTADOR && prestador != null
                ? prestador.id() : null;
        try {
            if (seleccionado == null) {
                service.crear(txtNombre.getText(), txtUsername.getText(), txtClave.getText(), rol,
                        txtCargo.getText(), prestadorId);
                Alertas.info("Usuario creado", "El usuario " + txtUsername.getText().trim() + " ya puede iniciar sesión.");
            } else {
                service.editar(seleccionado.getId(), txtNombre.getText(), rol, txtCargo.getText(), prestadorId);
                Alertas.info("Usuario actualizado", "Los datos de " + seleccionado.getUsername() + " se guardaron.");
            }
            nuevo();
            buscar();
        } catch (ServicioException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            falloInesperado(e);
        }
    }

    @FXML
    private void restablecerClave() {
        if (seleccionado == null) {
            return;
        }
        ocultarError();
        try {
            // La validación de la clave es del servicio; aquí solo se confirma la acción.
            if (txtClave.getText().isEmpty()) {
                mostrarError("Escribe la nueva contraseña en el campo de arriba y vuelve a pulsar el botón.");
                return;
            }
            if (!Alertas.confirmar("Restablecer contraseña",
                    "Se cambiará la contraseña de " + seleccionado.getUsername() + ". ¿Continuar?")) {
                return;
            }
            service.restablecerClave(seleccionado.getId(), txtClave.getText());
            txtClave.clear();
            Alertas.info("Contraseña restablecida", "Comunica la nueva contraseña a " + seleccionado.getNombre() + ".");
        } catch (ServicioException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            falloInesperado(e);
        }
    }

    @FXML
    private void desbloquear() {
        if (seleccionado == null) {
            return;
        }
        try {
            if (!Alertas.confirmar("Desbloquear cuenta",
                    "Se reactivará la cuenta de " + seleccionado.getUsername() + " y se reiniciarán sus intentos fallidos.")) {
                return;
            }
            service.desbloquear(seleccionado.getId());
            nuevo();
            buscar();
        } catch (ServicioException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            falloInesperado(e);
        }
    }

    @FXML
    private void cambiarEstado() {
        if (seleccionado == null) {
            return;
        }
        boolean activar = Usuario.ESTADO_INACTIVO.equals(seleccionado.getEstado());
        try {
            if (seleccionado.getId() == Sesion.getUsuario().getId() && !activar) {
                mostrarError("No puedes desactivar tu propia cuenta.");
                return;
            }
            String mensaje = activar
                    ? "Se reactivará la cuenta de " + seleccionado.getUsername() + "."
                    : "La cuenta de " + seleccionado.getUsername()
                    + " dejará de poder iniciar sesión. Sus registros se conservan y podrás activarla de nuevo.";
            if (!Alertas.confirmar(activar ? "Activar usuario" : "Desactivar usuario", mensaje)) {
                return;
            }
            service.cambiarEstado(seleccionado.getId(), activar);
            nuevo();
            buscar();
        } catch (ServicioException e) {
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            falloInesperado(e);
        }
    }

    private void falloInesperado(RuntimeException e) {
        LOG.log(Level.SEVERE, "Error inesperado en usuarios", e);
        mostrarError("Ocurrió un error inesperado. Revisa el archivo de log.");
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
