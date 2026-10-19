
package edu.uasd.vitasalud.controller.prestador;

import edu.uasd.vitasalud.app.Navegador;
import javafx.fxml.FXML;

public class InicioController {

    @FXML
    private void abrirBusqueda() {
        Navegador.irA("busqueda-avanzada");
    }

    @FXML
    private void abrirMisSolicitudes() {
        Navegador.irA("mis-solicitudes");
    }

    @FXML
    private void abrirReportes() {
        Navegador.irA("reportes-prestador");
    }

    @FXML
    private void abrirPrecertificaciones() {
        Navegador.irA("precertificaciones");
    }
}
