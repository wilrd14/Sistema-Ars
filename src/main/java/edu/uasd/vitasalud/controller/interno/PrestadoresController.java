package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.CatalogoDAO;
import edu.uasd.vitasalud.dao.impl.CatalogoDAOJdbc;
import edu.uasd.vitasalud.model.Opcion;
import edu.uasd.vitasalud.model.Prestador;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.service.PrestadorService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Controlador de "Red de prestadores": lista, filtro, alta, edición y estado (RF-PSS-01 a 03). */
public class PrestadoresController {

    private static final String TODOS = "TODOS";

    private static final Map<String, String> TIPOS = new LinkedHashMap<>();
    private static final Map<String, String> ESTADOS = new LinkedHashMap<>();

    static {
        TIPOS.put("CLINICA", "Clínica");
        TIPOS.put("HOSPITAL", "Hospital");
        TIPOS.put("LABORATORIO", "Laboratorio");
        TIPOS.put("CENTRO_DIAGNOSTICO", "Centro de diagnóstico");
        TIPOS.put("MEDICO_INDEPENDIENTE", "Médico independiente");
        TIPOS.put("FARMACIA", "Farmacia");

        ESTADOS.put("ACTIVO", "Activo");
        ESTADOS.put("SUSPENDIDO", "Suspendido");
        ESTADOS.put("TERMINADO", "Terminado");
    }

    private final PrestadorService prestadorService = new PrestadorService();
    private final CatalogoDAO catalogoDAO = new CatalogoDAOJdbc();
    private final Map<Integer, String> provinciasPorId = new HashMap<>();

    // Prestador que se está editando; null cuando es un alta.
    private Prestador prestadorEnEdicion;

    // ----- Filtros y tabla -----
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Prestador> tablaPrestadores;
    @FXML private TableColumn<Prestador, String> colRnc;
    @FXML private TableColumn<Prestador, String> colNombre;
    @FXML private TableColumn<Prestador, String> colTipo;
    @FXML private TableColumn<Prestador, String> colProvincia;
    @FXML private TableColumn<Prestador, String> colTelefono;
    @FXML private TableColumn<Prestador, String> colEstado;

    // ----- Acciones -----
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private ComboBox<String> cmbNuevoEstado;
    @FXML private Button btnAplicarEstado;

    // ----- Aviso y formulario -----
    @FXML private HBox panelExito;
    @FXML private Label lblExito;
    @FXML private VBox panelFormulario;
    @FXML private Label lblTituloFormulario;
    @FXML private TextField txtRnc;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<Opcion> cmbProvincia;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private Label lblMensaje;

    @FXML
    private void initialize() {
        llenarCombo(cmbFiltroTipo, conTodos(TIPOS));
        llenarCombo(cmbFiltroEstado, conTodos(ESTADOS));
        cmbFiltroTipo.setValue(TODOS);
        cmbFiltroEstado.setValue(TODOS);

        llenarCombo(cmbTipo, TIPOS);
        llenarCombo(cmbNuevoEstado, ESTADOS);

        try {
            for (Opcion provincia : catalogoDAO.listarProvincias()) {
                provinciasPorId.put(provincia.id(), provincia.nombre());
                cmbProvincia.getItems().add(provincia);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        colRnc.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRncCedula()));
        colNombre.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNombre()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                TIPOS.getOrDefault(c.getValue().getTipo(), c.getValue().getTipo())));
        colProvincia.setCellValueFactory(c -> {
            Integer id = c.getValue().getProvinciaId();
            return new SimpleStringProperty(
                    id == null ? "-" : provinciasPorId.getOrDefault(id, "-"));
        });
        colTelefono.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTelefono() == null ? "-" : c.getValue().getTelefono()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(
                ESTADOS.getOrDefault(c.getValue().getEstadoContrato(),
                        c.getValue().getEstadoContrato())));

        // Enter en el buscador también busca.
        txtBuscar.setOnAction(e -> buscar());

        // Solo el Administrador gestiona la red; los demás roles la consultan.
        boolean puedeGestionar = Sesion.getRol() == Rol.ADMINISTRADOR;
        btnNuevo.setDisable(!puedeGestionar);
        btnEditar.setDisable(!puedeGestionar);
        cmbNuevoEstado.setDisable(!puedeGestionar);
        btnAplicarEstado.setDisable(!puedeGestionar);

        // Búsqueda global (RF-SEG-08): llega con el RNC/cédula ya escrito.
        Navegador.consumirBusquedaPendiente("prestadores").ifPresent(txtBuscar::setText);
        buscar();
    }

    @FXML
    private void buscar() {
        try {
            tablaPrestadores.setItems(FXCollections.observableArrayList(
                    prestadorService.filtrar(
                            txtBuscar.getText(),
                            valorFiltro(cmbFiltroTipo),
                            valorFiltro(cmbFiltroEstado))));
        } catch (SQLException e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "Prestadores",
                    "No se pudo consultar la base de datos.");
        }
    }

    @FXML
    private void nuevoPrestador() {
        prestadorEnEdicion = null;
        limpiarFormulario();
        lblTituloFormulario.setText("Nuevo prestador");
        mostrarFormulario(true);
    }

    @FXML
    private void editarPrestador() {
        Prestador seleccionado = tablaPrestadores.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            alerta(Alert.AlertType.WARNING, "Prestadores",
                    "Selecciona un prestador de la tabla.");
            return;
        }

        prestadorEnEdicion = seleccionado;
        limpiarFormulario();
        lblTituloFormulario.setText("Editar prestador");

        txtRnc.setText(seleccionado.getRncCedula());
        txtNombre.setText(seleccionado.getNombre());
        cmbTipo.setValue(seleccionado.getTipo());
        txtDireccion.setText(seleccionado.getDireccion());
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo());

        Integer provinciaId = seleccionado.getProvinciaId();
        if (provinciaId != null) {
            for (Opcion provincia : cmbProvincia.getItems()) {
                if (provincia.id() == provinciaId) {
                    cmbProvincia.setValue(provincia);
                    break;
                }
            }
        }

        mostrarFormulario(true);
    }

    @FXML
    private void guardarPrestador() {
        lblMensaje.setText("");

        Prestador p = new Prestador();

        if (prestadorEnEdicion != null) {
            p.setId(prestadorEnEdicion.getId());
            p.setEstadoContrato(prestadorEnEdicion.getEstadoContrato());
        }

        p.setRncCedula(texto(txtRnc));
        p.setNombre(texto(txtNombre));
        p.setTipo(cmbTipo.getValue());
        p.setDireccion(texto(txtDireccion));

        Opcion provincia = cmbProvincia.getValue();
        p.setProvinciaId(provincia == null ? null : provincia.id());

        p.setTelefono(texto(txtTelefono));
        p.setCorreo(texto(txtCorreo));

        try {
            boolean eraAlta = prestadorEnEdicion == null;

            if (eraAlta) {
                prestadorService.registrarPrestador(p);
            } else {
                prestadorService.actualizarPrestador(p);
            }

            mostrarFormulario(false);
            mostrarExito(eraAlta
                    ? "Prestador registrado correctamente."
                    : "Prestador actualizado correctamente.");
            buscar();

        } catch (IllegalArgumentException e) {
            // Mensajes de validación de las reglas de negocio.
            lblMensaje.setText(e.getMessage());
        } catch (SQLException e) {
            lblMensaje.setText("No se pudo guardar. Revisa la conexión con la base de datos.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelarFormulario() {
        mostrarFormulario(false);
    }

    @FXML
    private void aplicarEstado() {
        Prestador seleccionado = tablaPrestadores.getSelectionModel().getSelectedItem();
        String nuevoEstado = cmbNuevoEstado.getValue();

        if (seleccionado == null || nuevoEstado == null) {
            alerta(Alert.AlertType.WARNING, "Prestadores",
                    "Selecciona un prestador y el nuevo estado.");
            return;
        }

        // Confirmación antes de cambiar el estado del contrato.
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cambiar estado del contrato");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Cambiar el contrato de " + seleccionado.getNombre()
                + " a " + ESTADOS.get(nuevoEstado) + "?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            prestadorService.cambiarEstadoContrato(seleccionado.getId(), nuevoEstado);
            mostrarExito("Estado del contrato actualizado.");
            buscar();
        } catch (IllegalArgumentException e) {
            alerta(Alert.AlertType.WARNING, "Prestadores", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            alerta(Alert.AlertType.ERROR, "Prestadores",
                    "No se pudo cambiar el estado.");
        }
    }

    // ----- Utilidades -----

    private void mostrarFormulario(boolean visible) {
        panelFormulario.setVisible(visible);
        panelFormulario.setManaged(visible);
        lblMensaje.setText("");
    }

    private void mostrarExito(String mensaje) {
        lblExito.setText(mensaje);
        panelExito.setVisible(true);
        panelExito.setManaged(true);
    }

    private void limpiarFormulario() {
        txtRnc.clear();
        txtNombre.clear();
        cmbTipo.setValue(null);
        cmbProvincia.setValue(null);
        txtDireccion.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        lblMensaje.setText("");
    }

    // Texto sin espacios sobrantes, o null si está vacío.
    private String texto(TextField campo) {
        String valor = campo.getText();
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    // Valor de un combo de filtro; null significa "Todos".
    private String valorFiltro(ComboBox<String> combo) {
        String valor = combo.getValue();
        return (valor == null || TODOS.equals(valor)) ? null : valor;
    }

    // Copia del mapa con la opción "Todos" al inicio.
    private Map<String, String> conTodos(Map<String, String> opciones) {
        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put(TODOS, "Todos");
        mapa.putAll(opciones);
        return mapa;
    }

    // El combo guarda el código de la BD y muestra la etiqueta.
    private void llenarCombo(ComboBox<String> combo, Map<String, String> opciones) {
        combo.getItems().setAll(opciones.keySet());
        combo.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String codigo) {
                return codigo == null ? "" : opciones.get(codigo);
            }

            @Override
            public String fromString(String texto) {
                return null;
            }
        });
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}