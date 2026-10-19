package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.model.Procedimiento;
import edu.uasd.vitasalud.service.ConsultaPrestadorService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class BusquedaController {

    @FXML
    private TextField campoBusqueda;

    @FXML
    private TableView<Procedimiento> tablaProcedimientos;

    @FXML
    private TableColumn<Procedimiento, String> columnaCodigo;

    @FXML
    private TableColumn<Procedimiento, String> columnaDescripcion;

    @FXML
    private TableColumn<Procedimiento, String> columnaCategoria;

    @FXML
    private TableColumn<Procedimiento, Number> columnaPrecio;

    @FXML
    private Label etiquetaEstado;

    private final ObservableList<Procedimiento> procedimientos =
            FXCollections.observableArrayList();

    private final ConsultaPrestadorService servicio =
            new ConsultaPrestadorService();

    @FXML
    private void initialize() {

        columnaCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo"));

        columnaDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion"));

        columnaCategoria.setCellValueFactory(
                new PropertyValueFactory<>("categoria"));

        columnaPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio_referencia"));

        tablaProcedimientos.setItems(procedimientos);

        etiquetaEstado.setText(
                "Introduce código, descripción o categoría.");
    }

    @FXML
    private void buscarProcedimientos() {

        String criterio = campoBusqueda.getText();

        if (criterio == null || criterio.isBlank()) {
            etiquetaEstado.setText(
                    "Escribe un criterio de búsqueda.");
            return;
        }

        try {

            List<Procedimiento> encontrados =
                    servicio.buscarProcedimientos(criterio);

            procedimientos.setAll(encontrados);

            etiquetaEstado.setText(
                    encontrados.size()
                            + " procedimiento(s) encontrado(s).");

        } catch (SQLException e) {

            etiquetaEstado.setText(
                    "No se pudo consultar los procedimientos. "
                            + "Verifica la conexión a la base de datos.");

        } catch (IllegalArgumentException e) {

            etiquetaEstado.setText(e.getMessage());
        }
    }

    @FXML
    private void limpiarBusqueda() {

        campoBusqueda.clear();
        procedimientos.clear();

        etiquetaEstado.setText(
                "Introduce código, descripción o categoría.");
    }
}