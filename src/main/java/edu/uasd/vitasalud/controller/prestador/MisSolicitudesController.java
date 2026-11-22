package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.service.ConsultaPrestadorService;
import edu.uasd.vitasalud.service.ConsultaPrestadorService.SolicitudPrestador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class MisSolicitudesController {

    @FXML
    private TableView<SolicitudPrestador> tablaSolicitudes;

    @FXML
    private TableColumn<SolicitudPrestador, String> columnaNumero;

    @FXML
    private TableColumn<SolicitudPrestador, String> columnaFecha;

    @FXML
    private TableColumn<SolicitudPrestador, String> columnaTipo;

    @FXML
    private TableColumn<SolicitudPrestador, String> columnaEstado;

    @FXML
    private Label etiquetaEstado;

    private final ObservableList<SolicitudPrestador> solicitudes =
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

        columnaTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo"));

        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado"));

        tablaSolicitudes.setItems(solicitudes);

        actualizarSolicitudes();
    }

    @FXML
    private void actualizarSolicitudes() {

        try {

            solicitudes.setAll(servicio.listarSolicitudes());

            etiquetaEstado.setText(
                    solicitudes.size()
                            + " solicitud(es) encontrada(s).");

        } catch (SQLException | IllegalStateException e) {

            etiquetaEstado.setText(
                    "No se pudieron consultar las solicitudes: "
                            + e.getMessage());
        }
    }
}