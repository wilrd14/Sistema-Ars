
package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.dao.CoberturaDAO;
import edu.uasd.vitasalud.dao.PlanDAO;
import edu.uasd.vitasalud.dao.impl.CoberturaDAOImpI;
import edu.uasd.vitasalud.dao.impl.PlanDAOlmpl;
import edu.uasd.vitasalud.model.Cobertura;
import edu.uasd.vitasalud.model.Plan;
import edu.uasd.vitasalud.service.PlanService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class PlanesController {

    @FXML private TableView<Plan> tablaPlanes;
    @FXML private TableColumn<Plan, Integer> colId;
    @FXML private TableColumn<Plan, String> colCodigo;
    @FXML private TableColumn<Plan, String> colNombre;
    @FXML private TableColumn<Plan, String> colDescripcion;
    @FXML private TableColumn<Plan, java.math.BigDecimal> colPrima;
    @FXML private TableColumn<Plan, String> colEstado;

    @FXML private TableView<Cobertura> tablaCoberturas;
    @FXML private TableColumn<Cobertura, Long> colCoberturaId;
    @FXML private TableColumn<Cobertura, String> colCategoria;
    @FXML private TableColumn<Cobertura, java.math.BigDecimal> colPorcentaje;
    @FXML private TableColumn<Cobertura, java.math.BigDecimal> colCopago;
    @FXML private TableColumn<Cobertura, java.math.BigDecimal> colTope;
    @FXML private TableColumn<Cobertura, Integer> colCarencia;
    @FXML private TableColumn<Cobertura, Boolean> colPrecertificacion;

    @FXML private ComboBox<String> filtroCategoria;
    @FXML private Label mensaje;

    private final PlanDAO planDAO = new PlanDAOlmpl();
    private final CoberturaDAO coberturaDAO = new CoberturaDAOImpI();
    private final PlanService planService = new PlanService(planDAO, coberturaDAO);

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrima.setCellValueFactory(new PropertyValueFactory<>("prima_mensual"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colCoberturaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentaje"));
        colCopago.setCellValueFactory(new PropertyValueFactory<>("copago_fijo"));
        colTope.setCellValueFactory(new PropertyValueFactory<>("tope_anual"));
        colCarencia.setCellValueFactory(new PropertyValueFactory<>("dias_carencia"));
        colPrecertificacion.setCellValueFactory(
                new PropertyValueFactory<>("requiere_precertificacion"));

        cargarDatos();
    }

    @FXML
    private void cargarDatos() {
        try {
            tablaPlanes.setItems(FXCollections.observableArrayList(
                    planService.listarPlanes()));

            tablaCoberturas.setItems(FXCollections.observableArrayList(
                    coberturaDAO.listarTodo()));

            filtroCategoria.setItems(FXCollections.observableArrayList(
                    coberturaDAO.listarTodo().stream()
                            .map(Cobertura::getCategoria)
                            .filter(c -> c != null && !c.isBlank())
                            .distinct()
                            .sorted()
                            .toList()));

            mensaje.setText("Planes y coberturas cargados correctamente.");
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los datos: " + e.getMessage());
        }
    }

    @FXML
    private void filtrarCoberturas() {
        String categoria = filtroCategoria.getValue();

        if (categoria == null) {
            tablaCoberturas.setItems(FXCollections.observableArrayList());
            cargarDatos();
            return;
        }

        try {
            tablaCoberturas.setItems(FXCollections.observableArrayList(
                    planService.consultarCoberturaPorCategoria(categoria)));
            mensaje.setText("Filtro aplicado: " + categoria);
        } catch (SQLException | IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void limpiarFiltro() {
        filtroCategoria.setValue(null);
        cargarDatos();
    }

    private void mostrarError(String detalle) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("ARS VitaSalud");
        alerta.setHeaderText("Error");
        alerta.setContentText(detalle);
        alerta.showAndWait();
    }
}
