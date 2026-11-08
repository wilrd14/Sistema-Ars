package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.model.*;
import edu.uasd.vitasalud.service.AutorizacionService;
import edu.uasd.vitasalud.util.Alertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** RF-AUT-01..04: asistente de tres etapas. No consulta SQL directamente. */
public class SolicitarController {
    @FXML
    private TabPane pasos;
    @FXML
    private TextField txtBuscar, txtServicio, txtMedico, txtTelefono, txtCorreo, txtCie, txtDiagnostico, txtCantidad,
            txtMonto;
    @FXML
    private ComboBox<String> cbTipoTelefono;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private CheckBox chkLaboral;
    @FXML
    private ListView<AfiliadoBusqueda> listaAfiliados;
    @FXML
    private ComboBox<ProcedimientoBusqueda> cbProcedimiento;
    @FXML
    private ListView<DetalleAutorizacion> listaDetalles;
    @FXML
    private Label lblElegido, lblResultado;
    private final AutorizacionService servicio = new AutorizacionService();
    private final List<DetalleAutorizacion> detalles = new ArrayList<>();
    private AfiliadoBusqueda afiliado;

    @FXML
    public void initialize() {
        cbTipoTelefono.setItems(FXCollections.observableArrayList("CELULAR", "RESIDENCIAL", "TRABAJO"));
        cbTipoTelefono.setValue("CELULAR");
        dpFecha.setValue(LocalDate.now());
        txtCantidad.setText("1");
        listaAfiliados.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(AfiliadoBusqueda f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.nombreCompleto() + " | " + f.documento() + " | " + f.estado());
            }
        });
        listaDetalles.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(DetalleAutorizacion d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null
                        : d.codigo() + " · " + d.descripcion() + " · cantidad " + d.cantidad() + " · RD$ "
                                + d.montoSolicitado());
            }
        });
        listaAfiliados.getSelectionModel().selectedItemProperty().addListener((ob, a, b) -> {
            afiliado = b;
            lblElegido.setText(
                    b == null ? "Seleccione el afiliado" : "Afiliado: " + b.nombreCompleto() + " [" + b.estado() + "]");
        });
        try {
            cbProcedimiento.setItems(FXCollections.observableArrayList(servicio.procedimientos()));
        } catch (RuntimeException e) {
            lblResultado.setText(e.getMessage());
        }
    }

    @FXML
    private void buscar() {
        try {
            listaAfiliados.setItems(FXCollections.observableArrayList(servicio.buscarAfiliados(txtBuscar.getText())));
        } catch (RuntimeException e) {
            Alertas.error("Buscar afiliado", e.getMessage());
        }
    }

    @FXML
    private void siguiente1() {
        if (afiliado == null) {
            Alertas.error("Paso 1", "Seleccione un afiliado de la lista");
            return;
        }
        pasos.getSelectionModel().select(1);
    }

    @FXML
    private void siguiente2() {
        if (txtServicio.getText().isBlank() || txtMedico.getText().isBlank() || txtTelefono.getText().isBlank()
                || txtCie.getText().isBlank() || txtDiagnostico.getText().isBlank()) {
            Alertas.error("Paso 2", "Complete todos los datos clínicos obligatorios");
            return;
        }
        pasos.getSelectionModel().select(2);
    }

    @FXML
    private void agregar() {
        try {
            ProcedimientoBusqueda p = cbProcedimiento.getValue();
            if (p == null)
                throw new IllegalArgumentException("Elija un procedimiento");
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            if (cantidad <= 0 || monto.signum() <= 0)
                throw new IllegalArgumentException("La cantidad y monto deben ser positivos");
            // Se guarda monto total de la línea (cantidad × precio); el servicio comprueba
            // el límite.
            detalles.add(new DetalleAutorizacion(null, 0, p.id(), p.codigo(), p.descripcion(), p.categoria(), cantidad,
                    monto, null));
            listaDetalles.setItems(FXCollections.observableArrayList(detalles));
        } catch (RuntimeException e) {
            Alertas.error("Procedimiento", e.getMessage());
        }
    }

    @FXML
    private void quitar() {
        DetalleAutorizacion d = listaDetalles.getSelectionModel().getSelectedItem();
        if (d != null) {
            detalles.remove(d);
            listaDetalles.setItems(FXCollections.observableArrayList(detalles));
        }
    }

    @FXML
    private void enviar() {
        try {
            String numero = servicio.solicitar(afiliado, txtServicio.getText(), dpFecha.getValue(),
                    cbTipoTelefono.getValue(), txtMedico.getText(), txtTelefono.getText(), txtCorreo.getText(),
                    txtCie.getText(), txtDiagnostico.getText(), chkLaboral.isSelected(), List.copyOf(detalles));
            lblResultado.setText("Solicitud registrada: " + numero + " · estado PENDIENTE");
            Alertas.info("Solicitud enviada", "Autorización " + numero + " guardada en estado PENDIENTE");
            detalles.clear();
            listaDetalles.getItems().clear();
        } catch (RuntimeException e) {
            Alertas.error("No se pudo enviar", e.getMessage());
        }
    }
}
