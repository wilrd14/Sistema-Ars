package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.model.*;
import edu.uasd.vitasalud.service.*;
import edu.uasd.vitasalud.util.Alertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** RF-REC-01: solo muestra autorizaciones propias aprobadas y no reclamadas. */
public class AgregarReclamacionController {
    @FXML
    private ComboBox<Autorizacion> cbAutorizacion;
    @FXML
    private TextField txtFactura, txtMonto;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private Label lblResultado;
    @FXML
    private ListView<String> listado;
    @FXML
    private ComboBox<Reclamacion> cbDevueltas;
    private final AutorizacionService auth = new AutorizacionService();
    private final ReclamacionService rec = new ReclamacionService();

    @FXML
    public void initialize() {
        cbAutorizacion.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Autorizacion a, boolean vacio) {
                super.updateItem(a, vacio);
                setText(vacio || a == null ? null
                        : a.numero() + " | " + a.afiliadoNombre() + " | RD$ " + a.montoCubierto());
            }
        });
        cbAutorizacion.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Autorizacion a, boolean vacio) {
                super.updateItem(a, vacio);
                setText(vacio || a == null ? null : a.numero() + " — RD$ " + a.montoCubierto());
            }
        });
        cbDevueltas.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Reclamacion r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : r.numero() + " · " + r.motivoDevolucion());
            }
        });
        cbDevueltas.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Reclamacion r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : r.numero());
            }
        });
        dpFecha.setValue(LocalDate.now());
        recargar();
    }

    @FXML
    private void recargar() {
        try {
            var reclamadas = rec.listar().stream().map(Reclamacion::autorizacionId)
                    .collect(java.util.stream.Collectors.toSet());
            cbAutorizacion.setItems(FXCollections.observableArrayList(auth.listar().stream()
                    .filter(a -> a.estado() == EstadoAutorizacion.APROBADA && !reclamadas.contains(a.id())).toList()));
            cbDevueltas.setItems(FXCollections.observableArrayList(rec.listar().stream()
                    .filter(r -> r.estado() == EstadoReclamacion.DEVUELTA).toList()));
            listado.setItems(FXCollections.observableArrayList(rec.listar().stream()
                    .map(r -> r.numero() + " · " + r.numeroAutorizacion() + " · " + r.estado() + " · RD$ "
                            + r.montoPagar())
                    .toList()));
        } catch (RuntimeException e) {
            Alertas.error("Reclamaciones", e.getMessage());
        }
    }

    @FXML
    private void corregir() {
        try {
            Reclamacion r = cbDevueltas.getValue();
            if (r == null)
                throw new ServicioException("Elija una reclamación DEVUELTA");
            rec.corregir(r.id(), txtFactura.getText(), dpFecha.getValue(), new BigDecimal(txtMonto.getText().trim()));
            recargar();
            Alertas.info("Corrección", "Reclamación reenviada para auditoría");
        } catch (RuntimeException e) {
            Alertas.error("Corrección", e.getMessage());
        }
    }

    @FXML
    private void registrar() {
        try {
            Autorizacion a = cbAutorizacion.getValue();
            if (a == null)
                throw new ServicioException("Elija una autorización aprobada");
            String numero = rec.crear(a.id(), txtFactura.getText(), dpFecha.getValue(),
                    new BigDecimal(txtMonto.getText().trim()));
            lblResultado.setText("Registrada: " + numero + " · RECIBIDA");
            recargar();
            Alertas.info("Reclamación", "Se registró " + numero);
        } catch (RuntimeException e) {
            Alertas.error("Reclamación", e.getMessage());
        }
    }
}
