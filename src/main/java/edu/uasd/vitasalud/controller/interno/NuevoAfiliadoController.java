/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.uasd.vitasalud.controller.interno;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class NuevoAfiliadoController {

    // ----- Datos del titular -----
    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private TextField txtNumeroDocumento;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtNss;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> cmbSexo;
    @FXML private ComboBox<String> cmbEstadoCivil;
    @FXML private TextField txtNacionalidad;

    // ----- Contacto y ubicación -----
    @FXML private ComboBox<String> cmbTipoTelefono;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbProvincia;
    @FXML private ComboBox<String> cmbMunicipio;
    @FXML private TextField txtSector;
    @FXML private TextField txtDireccion;

    // ----- Afiliación y cobertura -----
    @FXML private ComboBox<String> cmbPlan;
    @FXML private DatePicker dpInicioCobertura;
    @FXML private TextField txtEmpleadorRnc;
    @FXML private CheckBox chkEsEstudiante;

    // ----- Dependientes -----
    @FXML private TableView<?> tablaDependientes;
    @FXML private TableColumn<?, ?> colDepNombres;
    @FXML private TableColumn<?, ?> colDepApellidos;
    @FXML private TableColumn<?, ?> colDepParentesco;
    @FXML private TableColumn<?, ?> colDepFechaNacimiento;
    @FXML private TableColumn<?, ?> colDepDocumento;
    @FXML private Button btnAgregarDependiente;
    @FXML private Button btnQuitarDependiente;

    // ----- Pie -----
    @FXML private Label lblMensaje;
    @FXML private Button btnCancelar;
    @FXML private Button btnBorrador;
    @FXML private Button btnGuardar;

    @FXML
    private void agregarDependiente() {
        mostrarMensaje("Dependientes", "Aquí agregaremos un dependiente a la tabla.");
    }

    @FXML
    private void quitarDependiente() {
        mostrarMensaje("Dependientes", "Aquí quitaremos el dependiente seleccionado.");
    }

    @FXML
    private void guardar() {
        mostrarMensaje("Guardar", "Aquí validaremos y guardaremos el titular con sus dependientes.");
    }

    @FXML
    private void guardarBorrador() {
        mostrarMensaje("Guardar borrador", "Aquí guardaremos el registro como borrador.");
    }

    @FXML
    private void cancelar() {
        mostrarMensaje("Cancelar", "Aquí volveremos a la lista de afiliados.");
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}