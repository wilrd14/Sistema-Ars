package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.model.Reporte;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.TablaReporte;
import edu.uasd.vitasalud.service.ReporteService;
import edu.uasd.vitasalud.service.ServicioException;
import edu.uasd.vitasalud.util.Alertas;
import edu.uasd.vitasalud.util.Formatos;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Reportes de la Consola interna (RF-REP-02/03) con exportación a CSV (RF-REP-04). */
public class ReportesController {

    @FXML private FlowPane tarjetas;
    @FXML private VBox detalle;
    @FXML private Label lblTitulo;
    @FXML private Label lblResumen;
    @FXML private Label lblMensaje;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Button btnGenerar;
    @FXML private Button btnExportar;
    @FXML private TableView<List<String>> tabla;

    private final ReporteService servicio = new ReporteService();
    private final Map<Reporte, Button> botones = new EnumMap<>(Reporte.class);
    private Reporte actual;
    private TablaReporte ultima;

    @FXML
    private void initialize() {
        configurarFecha(dpDesde);
        configurarFecha(dpHasta);
        tabla.setPlaceholder(new Label("Sin datos para el período seleccionado."));
        Rol rol = Sesion.getRol();
        for (Reporte r : servicio.reportesPara(rol)) {
            Label titulo = new Label(r.titulo());
            titulo.getStyleClass().add("section-title");
            Label descripcion = new Label(r.descripcion());
            descripcion.getStyleClass().add("texto-secundario");
            descripcion.setWrapText(true);
            Button ver = new Button("Ver reporte");
            ver.getStyleClass().add("btn-ghost");
            ver.setOnAction(e -> seleccionar(r));
            botones.put(r, ver);
            VBox tarjeta = new VBox(8, titulo, descripcion, ver);
            tarjeta.getStyleClass().add("card");
            tarjeta.setPrefWidth(280);
            tarjeta.setMinHeight(150);
            tarjetas.getChildren().add(tarjeta);
        }
        if (botones.isEmpty()) {
            Label vacio = new Label("Tu rol no tiene reportes disponibles.");
            vacio.getStyleClass().add("texto-secundario");
            tarjetas.getChildren().add(vacio);
        }
    }

    /** DatePicker que muestra y acepta fechas como dd/mm/aaaa. */
    private static void configurarFecha(DatePicker dp) {
        dp.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                return Formatos.fecha(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(texto.trim(), Formatos.FECHA);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
    }

    private void seleccionar(Reporte reporte) {
        actual = reporte;
        ultima = null;
        lblTitulo.setText(reporte.titulo());
        detalle.setVisible(true);
        detalle.setManaged(true);
        botones.forEach((r, b) -> b.setText(r == reporte ? "Seleccionado" : "Ver reporte"));
        generar();
    }

    @FXML
    private void generar() {
        if (actual == null) {
            return;
        }
        Reporte reporte = actual;
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        Rol rol = Sesion.getRol();
        mensaje(null);
        btnGenerar.setDisable(true);
        Task<TablaReporte> tarea = new Task<>() {
            @Override
            protected TablaReporte call() {
                return servicio.generar(reporte, desde, hasta, rol);
            }
        };
        tarea.setOnSucceeded(e -> {
            btnGenerar.setDisable(false);
            if (reporte == actual) {
                mostrar(tarea.getValue());
            }
        });
        tarea.setOnFailed(e -> {
            btnGenerar.setDisable(false);
            Throwable causa = tarea.getException();
            mostrar(null);
            mensaje(causa instanceof ServicioException ? causa.getMessage()
                    : "No se pudo generar el reporte. Intenta de nuevo.");
        });
        Thread hilo = new Thread(tarea, "reporte-carga");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrar(TablaReporte t) {
        ultima = t;
        tabla.getColumns().clear();
        tabla.getItems().clear();
        btnExportar.setDisable(t == null || t.filas().isEmpty());
        lblResumen.setText(t == null || t.resumen() == null ? "" : t.resumen());
        if (t == null) {
            return;
        }
        for (int i = 0; i < t.columnas().size(); i++) {
            final int indice = i;
            TableColumn<List<String>, String> col = new TableColumn<>(t.columnas().get(i));
            col.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().get(indice)));
            col.setSortable(false);
            col.setPrefWidth(180);
            tabla.getColumns().add(col);
        }
        tabla.setItems(FXCollections.observableArrayList(t.filas()));
    }

    @FXML
    private void limpiar() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        generar();
    }

    /** RF-REP-04: CSV con BOM y ";" para Excel. PDF queda pendiente (no hay librería en el pom.xml). */
    @FXML
    private void exportar() {
        if (ultima == null || ultima.filas().isEmpty()) {
            Alertas.info("Exportar", "No hay datos para exportar.");
            return;
        }
        FileChooser selector = new FileChooser();
        selector.setTitle("Exportar reporte a CSV");
        selector.setInitialFileName(ReporteService.nombreArchivo(ultima.reporte()));
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv"));
        File destino = selector.showSaveDialog(tabla.getScene().getWindow());
        if (destino == null) {
            return;
        }
        try {
            servicio.exportarCsv(ultima, destino.toPath());
            Alertas.info("Exportar", "Reporte guardado en:\n" + destino.getAbsolutePath());
        } catch (ServicioException e) {
            Alertas.error("Exportar", e.getMessage());
        }
    }

    private void mensaje(String texto) {
        boolean hay = texto != null;
        lblMensaje.setText(hay ? texto : "");
        lblMensaje.setVisible(hay);
        lblMensaje.setManaged(hay);
    }
}
