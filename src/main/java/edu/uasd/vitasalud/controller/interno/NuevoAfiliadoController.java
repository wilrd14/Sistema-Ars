package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.dao.CatalogoDAO;
import edu.uasd.vitasalud.dao.impl.CatalogoDAOJdbc;
import edu.uasd.vitasalud.model.Afiliado;
import edu.uasd.vitasalud.model.Opcion;
import edu.uasd.vitasalud.service.AfiliadoService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Controlador del formulario "Nuevo afiliado" (RF-AFI-01 a RF-AFI-03). */
public class NuevoAfiliadoController {

    private final AfiliadoService afiliadoService = new AfiliadoService();
    private final CatalogoDAO catalogoDAO = new CatalogoDAOJdbc();

    // Titular encontrado cuando se registra un dependiente.
    private Afiliado titularEncontrado;

    // ----- 1. Datos personales -----
    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private TextField txtNumeroDocumento;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtNss;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> cmbSexo;
    @FXML private ComboBox<String> cmbEstadoCivil;
    @FXML private TextField txtNacionalidad;

    // ----- 2. Contacto y dirección -----
    @FXML private ComboBox<String> cmbTipoTelefono;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<Opcion> cmbProvincia;
    @FXML private ComboBox<Opcion> cmbMunicipio;
    @FXML private TextField txtSector;
    @FXML private TextField txtDireccion;

    // ----- 3. Plan y afiliación -----
    @FXML private ComboBox<String> cmbTipoAfiliado;
    @FXML private ComboBox<Opcion> cmbPlan;
    @FXML private DatePicker dpInicioCobertura;
    @FXML private TextField txtEmpleadorRnc;
    @FXML private CheckBox chkEsEstudiante;
    @FXML private VBox bloqueDependiente;
    @FXML private TextField txtCedulaTitular;
    @FXML private Label lblTitularEncontrado;
    @FXML private ComboBox<String> cmbParentesco;

    // ----- Pie -----
    @FXML private HBox panelExito;
    @FXML private Label lblExito;
    @FXML private Label lblMensaje;
    @FXML private Button btnCancelar;
    @FXML private Button btnCrear;

    @FXML
    private void initialize() {
        // Combos con valores fijos: el combo guarda el código de la BD
        // y muestra la etiqueta al usuario.
        llenarCombo(cmbTipoDocumento, opciones(
                "CEDULA", "Cédula",
                "PASAPORTE", "Pasaporte",
                "ACTA_NACIMIENTO", "Acta de nacimiento"));

        llenarCombo(cmbSexo, opciones(
                "M", "Masculino",
                "F", "Femenino"));

        llenarCombo(cmbEstadoCivil, opciones(
                "SOLTERO", "Soltero/a",
                "CASADO", "Casado/a",
                "UNION_LIBRE", "Unión libre",
                "DIVORCIADO", "Divorciado/a",
                "VIUDO", "Viudo/a"));

        llenarCombo(cmbTipoTelefono, opciones(
                "CELULAR", "Celular",
                "RESIDENCIAL", "Residencial",
                "TRABAJO", "Trabajo"));

        llenarCombo(cmbTipoAfiliado, opciones(
                "TITULAR", "Titular",
                "DEPENDIENTE", "Dependiente"));

        llenarCombo(cmbParentesco, opciones(
                "CONYUGE", "Cónyuge",
                "HIJO", "Hijo/a",
                "PADRE", "Padre",
                "MADRE", "Madre"));

        // Combos que vienen de la base de datos.
        try {
            cmbProvincia.getItems().setAll(catalogoDAO.listarProvincias());
            cmbPlan.getItems().setAll(catalogoDAO.listarPlanesActivos());
        } catch (SQLException e) {
            lblMensaje.setText("No se pudieron cargar provincias y planes. "
                    + "Revisa la conexión con la base de datos.");
            e.printStackTrace();
        }

        // El municipio depende de la provincia elegida.
        cmbProvincia.valueProperty().addListener(
                (obs, anterior, actual) -> cargarMunicipios(actual));

        // El bloque del titular solo aparece para los dependientes.
        cmbTipoAfiliado.valueProperty().addListener((obs, anterior, actual) -> {
            boolean dependiente = "DEPENDIENTE".equals(actual);
            bloqueDependiente.setVisible(dependiente);
            bloqueDependiente.setManaged(dependiente);
        });

        // Si cambia la cédula, hay que volver a buscar al titular.
        txtCedulaTitular.textProperty().addListener((obs, anterior, actual) -> {
            titularEncontrado = null;
            lblTitularEncontrado.setText("");
        });

        valoresIniciales();
    }

    @FXML
    private void buscarTitular() {
        lblMensaje.setText("");

        try {
            titularEncontrado = afiliadoService
                    .buscarTitularPorCedula(txtCedulaTitular.getText());

            lblTitularEncontrado.setText("Titular: "
                    + titularEncontrado.getNombres() + " "
                    + titularEncontrado.getApellidos()
                    + " (" + titularEncontrado.getNumeroAfiliado() + ")");

        } catch (IllegalArgumentException e) {
            titularEncontrado = null;
            lblTitularEncontrado.setText("");
            lblMensaje.setText(e.getMessage());
        } catch (SQLException e) {
            lblMensaje.setText("No se pudo consultar la base de datos.");
            e.printStackTrace();
        }
    }

    @FXML
    private void crearAfiliado() {
        lblMensaje.setText("");
        ocultarExito();

        try {
            Afiliado afiliado = leerFormulario();

            afiliadoService.registrarAfiliado(afiliado);

            lblExito.setText("Afiliado " + afiliado.getNumeroAfiliado()
                    + " creado correctamente. Ya puede recibir servicios.");
            panelExito.setVisible(true);
            panelExito.setManaged(true);

            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            // Mensajes de validación de las reglas de negocio.
            lblMensaje.setText(e.getMessage());
        } catch (SQLException e) {
            lblMensaje.setText("No se pudo guardar el afiliado. "
                    + "Revisa la conexión con la base de datos.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelar() {
        Navegador.irA("afiliados");
    }

    // Lee los campos y arma el objeto Afiliado.
    private Afiliado leerFormulario() {
        Afiliado a = new Afiliado();

        a.setTipoDocumento(cmbTipoDocumento.getValue());
        a.setNumeroDocumento(texto(txtNumeroDocumento));
        a.setNss(texto(txtNss));
        a.setNombres(texto(txtNombres));
        a.setApellidos(texto(txtApellidos));
        a.setFechaNacimiento(dpFechaNacimiento.getValue());

        String sexo = cmbSexo.getValue();
        a.setSexo(sexo == null ? ' ' : sexo.charAt(0));

        a.setEstadoCivil(cmbEstadoCivil.getValue());
        a.setNacionalidad(texto(txtNacionalidad));

        a.setTipoTelefono(cmbTipoTelefono.getValue());
        a.setTelefono(texto(txtTelefono));
        a.setCorreo(texto(txtCorreo));

        Opcion provincia = cmbProvincia.getValue();
        a.setProvinciaId(provincia == null ? null : provincia.id());

        Opcion municipio = cmbMunicipio.getValue();
        a.setMunicipioId(municipio == null ? null : municipio.id());

        a.setSector(texto(txtSector));
        a.setDireccion(texto(txtDireccion));

        Opcion plan = cmbPlan.getValue();
        a.setPlanId(plan == null ? 0 : plan.id());

        a.setTipoAfiliado(cmbTipoAfiliado.getValue());
        a.setInicioCobertura(dpInicioCobertura.getValue());
        a.setEmpleadorRnc(texto(txtEmpleadorRnc));
        a.setEsEstudiante(chkEsEstudiante.isSelected());

        if ("DEPENDIENTE".equals(a.getTipoAfiliado())) {
            if (titularEncontrado == null) {
                throw new IllegalArgumentException(
                        "Busca al titular por su cédula antes de crear el dependiente.");
            }
            if (cmbParentesco.getValue() == null) {
                throw new IllegalArgumentException("Selecciona el parentesco.");
            }
            a.setTitularId(titularEncontrado.getId());
            a.setParentesco(cmbParentesco.getValue());
        }

        return a;
    }

    private void cargarMunicipios(Opcion provincia) {
        cmbMunicipio.getItems().clear();
        cmbMunicipio.setValue(null);

        if (provincia == null) {
            return;
        }

        try {
            cmbMunicipio.getItems().setAll(
                    catalogoDAO.listarMunicipios(provincia.id()));
        } catch (SQLException e) {
            lblMensaje.setText("No se pudieron cargar los municipios.");
            e.printStackTrace();
        }
    }

    private void valoresIniciales() {
        cmbTipoAfiliado.setValue("TITULAR");
        dpInicioCobertura.setValue(LocalDate.now());
    }

    private void limpiarFormulario() {
        cmbTipoDocumento.setValue(null);
        txtNumeroDocumento.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtNss.clear();
        dpFechaNacimiento.setValue(null);
        cmbSexo.setValue(null);
        cmbEstadoCivil.setValue(null);
        txtNacionalidad.clear();
        cmbTipoTelefono.setValue(null);
        txtTelefono.clear();
        txtCorreo.clear();
        cmbProvincia.setValue(null);
        txtSector.clear();
        txtDireccion.clear();
        cmbPlan.setValue(null);
        txtEmpleadorRnc.clear();
        chkEsEstudiante.setSelected(false);
        txtCedulaTitular.clear();
        cmbParentesco.setValue(null);
        titularEncontrado = null;
        valoresIniciales();
    }

    private void ocultarExito() {
        panelExito.setVisible(false);
        panelExito.setManaged(false);
    }

    // Devuelve el texto sin espacios, o null si está vacío.
    private String texto(TextField campo) {
        String valor = campo.getText();
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    // Arma un mapa código -> etiqueta a partir de pares.
    private Map<String, String> opciones(String... pares) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            mapa.put(pares[i], pares[i + 1]);
        }
        return mapa;
    }

    // El combo guarda el código y muestra la etiqueta.
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
}