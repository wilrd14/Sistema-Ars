package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.service.AutorizacionService;
import edu.uasd.vitasalud.util.Alertas;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import java.time.*;

/** RF-PRE-06 (P2): registra ingreso y egreso con autorización aprobada. */
public class HospitalizacionController {
    @FXML
    private TextField txtAut, txtObs;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private ListView<String> lista;
    private final AutorizacionService service = new AutorizacionService();

    @FXML
    public void initialize() {
        dpFecha.setValue(LocalDate.now());
        recargar();
    }

    @FXML
    private void ingresar() {
        try {
            service.ingreso(Long.parseLong(txtAut.getText().trim()), dpFecha.getValue().atStartOfDay(),
                    txtObs.getText());
            recargar();
            Alertas.info("Hospitalización", "Ingreso registrado");
        } catch (RuntimeException e) {
            Alertas.error("Ingreso", e.getMessage());
        }
    }

    @FXML
    private void egresar() {
        try {
            service.egreso(Long.parseLong(txtAut.getText().trim()), dpFecha.getValue().atTime(23, 59));
            recargar();
            Alertas.info("Hospitalización", "Egreso registrado");
        } catch (RuntimeException e) {
            Alertas.error("Egreso", e.getMessage());
        }
    }

    @FXML
    private void recargar() {
        try {
            lista.setItems(FXCollections.observableArrayList(service.hospitalizaciones().stream()
                    .map(h -> "AUT id=" + h.autorizacionId() + " | Ingreso " + h.ingreso() + " | Egreso " + h.egreso())
                    .toList()));
        } catch (RuntimeException e) {
            Alertas.error("Hospitalización", e.getMessage());
        }
    }
}
