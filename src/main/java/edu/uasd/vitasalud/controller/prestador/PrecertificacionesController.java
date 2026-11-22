package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.service.ConsultaPrestadorService;
import edu.uasd.vitasalud.service.ConsultaPrestadorService.PrecertificacionPrestador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class PrecertificacionesController {

    @FXML
    private TableView<PrecertificacionPrestador> tablaPrecertificaciones;

    @FXML
    private TableColumn<PrecertificacionPrestador, String> columnaNumero;

    @FXML
    private TableColumn<PrecertificacionPrestador, String> columnaFecha;

    @FXML
    private TableColumn<PrecertificacionPrestador, String> columnaProcedimiento;

    @FXML
    private TableColumn<PrecertificacionPrestador, String> columnaEstado;

    @FXML
    private Label etiquetaEstado;

    private final ObservableList<PrecertificacionPrestador> filas =
            FXCollections.observableArrayList();

    private final ConsultaPrestadorService servicio =
            new ConsultaPrestadorService();

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {

        columnaNumero.setCellValueFactory(
                new PropertyValueFactory<>("numero"));

        columnaFecha.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFecha() == null
                                ? ""
                                : c.getValue().getFecha().format(FORMATO)));

        columnaProcedimiento.setCellValueFactory(
                new PropertyValueFactory<>("procedimiento"));

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado"));

        tablaPrecertificaciones.setItems(filas);

        actualizarPrecertificaciones();
    }

    @FXML
    private void actualizarPrecertificaciones() {

        try {

            filas.setAll(servicio.listarPrecertificaciones());

            etiquetaEstado.setText(
                    filas.size()
                            + " precertificación(es) encontrada(s).");

        } catch (SQLException | IllegalStateException e) {

            etiquetaEstado.setText(
                    "No se pudieron consultar las precertificaciones: "
                            + e.getMessage());
        }
    }
}