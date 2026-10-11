/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.uasd.vitasalud.controller.interno;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AfiliadosController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<?> tablaAfiliados;

    @FXML
    private TableColumn<?, ?> colNumero;

    @FXML
    private TableColumn<?, ?> colDocumento;

    @FXML
    private TableColumn<?, ?> colNombres;

    @FXML
    private TableColumn<?, ?> colApellidos;

    @FXML
    private TableColumn<?, ?> colEstado;

    @FXML
    private void buscarAfiliados() {
        mostrarMensaje(
                "Búsqueda",
                "Aquí conectaremos la búsqueda con la base de datos."
        );
    }

    @FXML
    private void nuevoAfiliado() {
        mostrarMensaje(
                "Nuevo afiliado",
                "Aquí abriremos el formulario para registrar un afiliado."
        );
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}