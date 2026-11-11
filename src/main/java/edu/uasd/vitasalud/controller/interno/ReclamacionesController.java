package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.model.*;
import edu.uasd.vitasalud.service.ReclamacionService;
import edu.uasd.vitasalud.util.Alertas;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;

/**
 * Panel de auditoría REC: transiciones, glosas y pagos; no guarda SQL desde
 * aquí.
 */
public class ReclamacionesController {
    @FXML
    private TableView<Reclamacion> tabla;
    @FXML
    private TableColumn<Reclamacion, String> colRec, colAut, colPrestador, colMonto, colEstado;
    @FXML
    private Label detalle;
    @FXML
    private TextArea txtMotivo, txtObs;
    @FXML
    private TextField txtGlosa, txtReferencia;
    @FXML
    private ComboBox<String> cbMotivo;
    @FXML
    private DatePicker dpPago;
    private final ReclamacionService service = new ReclamacionService();

    @FXML
    public void initialize() {
        colRec.setCellValueFactory(c -> prop(c.getValue().numero()));
        colAut.setCellValueFactory(c -> prop(c.getValue().numeroAutorizacion()));
        colPrestador.setCellValueFactory(c -> prop(c.getValue().prestadorNombre()));
        colMonto.setCellValueFactory(c -> prop(String.valueOf(c.getValue().montoReclamado())));
        colEstado.setCellValueFactory(c -> prop(c.getValue().estado().name()));
        cbMotivo.setItems(FXCollections.observableArrayList("SERVICIO_NO_AUTORIZADO", "MONTO_SUPERIOR_TARIFA",
                "DOCUMENTACION_INCOMPLETA", "OTRO"));
        cbMotivo.setValue("OTRO");
        dpPago.setValue(java.time.LocalDate.now());
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> mostrar(b));
        recargar();
    }

    private SimpleStringProperty prop(String s) {
        return new SimpleStringProperty(s == null ? "" : s);
    }

    @FXML
    private void recargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(service.listar()));
        } catch (RuntimeException e) {
            Alertas.error("REC", e.getMessage());
        }
    }

    private void mostrar(Reclamacion r) {
        if (r == null) {
            detalle.setText("Seleccione reclamación");
            return;
        }
        StringBuilder sb = new StringBuilder(
                r.numero() + " → " + r.estado() + "\nAutorización: " + r.numeroAutorizacion() +
                        "\nFactura: " + r.facturaPrestador() + "\nReclamado: RD$ " + r.montoReclamado() +
                        "\nGlosado: RD$ " + r.montoGlosado() + "\nA pagar: RD$ " + r.montoPagar());
        if (r.motivoDevolucion() != null)
            sb.append("\nObservación: ").append(r.motivoDevolucion());
        try {
            for (Glosa g : service.glosas(r.id()))
                sb.append("\n• Glosa ").append(g.motivo()).append(": RD$ ").append(g.monto());
        } catch (RuntimeException e) {
            sb.append("\nError leyendo glosas: ").append(e.getMessage());
        }
        detalle.setText(sb.toString());
    }

    private void cambiar(EstadoReclamacion e) {
        Reclamacion r = tabla.getSelectionModel().getSelectedItem();
        if (r == null) {
            Alertas.error("REC", "Seleccione reclamación");
            return;
        }
        try {
            service.cambiar(r.id(), e, txtMotivo.getText());
            recargar();
            Alertas.info("Auditoría", "Estado actualizado: " + e);
        } catch (RuntimeException ex) {
            Alertas.error("Auditoría", ex.getMessage());
        }
    }

    @FXML
    private void auditar() {
        cambiar(EstadoReclamacion.EN_AUDITORIA);
    }

    @FXML
    private void aprobar() {
        cambiar(EstadoReclamacion.APROBADA);
    }

    @FXML
    private void devolver() {
        cambiar(EstadoReclamacion.DEVUELTA);
    }

    @FXML
    private void rechazar() {
        cambiar(EstadoReclamacion.RECHAZADA);
    }

    @FXML
    private void glosar() {
        Reclamacion r = tabla.getSelectionModel().getSelectedItem();
        if (r == null) {
            Alertas.error("Glosa", "Seleccione reclamación");
            return;
        }
        try {
            service.glosar(r.id(), cbMotivo.getValue(), new BigDecimal(txtGlosa.getText().trim()), txtObs.getText());
            recargar();
            Alertas.info("Glosa", "Glosa registrada y monto recalculado");
        } catch (RuntimeException e) {
            Alertas.error("Glosa", e.getMessage());
        }
    }

    @FXML
    private void pagar() {
        Reclamacion r = tabla.getSelectionModel().getSelectedItem();
        if (r == null) {
            Alertas.error("Pago", "Seleccione reclamación");
            return;
        }
        try {
            service.pagar(r.id(), dpPago.getValue(), txtReferencia.getText());
            recargar();
            Alertas.info("Pago", "Reclamación cerrada");
        } catch (RuntimeException e) {
            Alertas.error("Pago", e.getMessage());
        }
    }
}
