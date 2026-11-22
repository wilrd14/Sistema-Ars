package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.service.ConsultaPrestadorService;
import edu.uasd.vitasalud.service.ConsultaPrestadorService.ResumenReporte;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class ReportesPrestadorController {

    @FXML
    private TableView<ResumenReporte> tablaReporte;

    @FXML
    private TableColumn<ResumenReporte, String> columnaConcepto;

    @FXML
    private TableColumn<ResumenReporte, Number> columnaCantidad;

    @FXML
    private TableColumn<ResumenReporte, String> columnaDetalle;

    @FXML
    private Label etiquetaEstado;

    private final ObservableList<ResumenReporte> filas =
            FXCollections.observableArrayList();

    private final ConsultaPrestadorService servicio =
            new ConsultaPrestadorService();

    @FXML
    private void initialize() {

        columnaConcepto.setCellValueFactory(
                new PropertyValueFactory<>("concepto"));

        columnaCantidad.setCellValueFactory(
                new PropertyValueFactory<>("cantidad"));

        columnaDetalle.setCellValueFactory(
                new PropertyValueFactory<>("detalle"));

        tablaReporte.setItems(filas);

        actualizarReporte();
    }

    @FXML
    private void actualizarReporte() {

        try {

            filas.setAll(servicio.obtenerReporte());

            etiquetaEstado.setText(
                    "Reporte actualizado con los datos de tu prestador.");

        } catch (SQLException | IllegalStateException e) {

            etiquetaEstado.setText(
                    "No se pudo generar el reporte: "
                            + e.getMessage());
        }
    }
}