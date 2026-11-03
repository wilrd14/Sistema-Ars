package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.app.Menu;
import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.model.ResumenPanel;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.service.ReporteService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Panel de la Consola interna (RF-REP-01). Las cifras se calculan en la base de datos desde un hilo de
 * fondo; el Shell recarga esta vista cada vez que se vuelve al Panel, así que siempre muestra datos frescos.
 */
public class PanelController {

    @FXML private Label kpiPendientes;
    @FXML private Label kpiRevision;
    @FXML private Label kpiAprobadas;
    @FXML private Label kpiNegadas;
    @FXML private Label lblAviso;
    @FXML private Label lblCargando;
    @FXML private Button btnRefrescar;
    @FXML private Button btnNuevoAfiliado;
    @FXML private Button btnAbrirBandeja;
    @FXML private VBox listaAtencion;

    private final ReporteService servicio = new ReporteService();

    @FXML
    private void initialize() {
        Rol rol = Sesion.getRol();
        boolean puedeAfiliar = puedeVer(rol, "nuevo-afiliado");
        boolean puedeBandeja = puedeVer(rol, "bandeja");
        btnNuevoAfiliado.setVisible(puedeAfiliar);
        btnNuevoAfiliado.setManaged(puedeAfiliar);
        btnAbrirBandeja.setVisible(puedeBandeja);
        btnAbrirBandeja.setManaged(puedeBandeja);
        refrescar();
    }

    private static boolean puedeVer(Rol rol, String idPantalla) {
        return Menu.paraRol(rol).stream().anyMatch(i -> i.id().equals(idPantalla));
    }

    @FXML
    private void refrescar() {
        Rol rol = Sesion.getRol();
        btnRefrescar.setDisable(true);
        lblCargando.setVisible(true);
        Task<ResumenPanel> tarea = new Task<>() {
            @Override
            protected ResumenPanel call() {
                return servicio.cargarPanel(rol);
            }
        };
        tarea.setOnSucceeded(e -> {
            mostrar(tarea.getValue());
            terminarCarga();
        });
        tarea.setOnFailed(e -> {
            mostrar(ResumenPanel.vacio("No se pudo cargar el panel. Pulsa Actualizar para reintentar."));
            terminarCarga();
        });
        Thread hilo = new Thread(tarea, "panel-carga");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void terminarCarga() {
        btnRefrescar.setDisable(false);
        lblCargando.setVisible(false);
    }

    private void mostrar(ResumenPanel p) {
        kpiPendientes.setText(String.valueOf(p.pendientes()));
        kpiRevision.setText(String.valueOf(p.enRevision()));
        kpiAprobadas.setText(String.valueOf(p.aprobadas()));
        kpiNegadas.setText(String.valueOf(p.negadas()));

        boolean hayAviso = p.aviso() != null;
        lblAviso.setText(hayAviso ? p.aviso() : "");
        lblAviso.setVisible(hayAviso);
        lblAviso.setManaged(hayAviso);

        listaAtencion.getChildren().clear();
        if (p.atencion().isEmpty()) {
            Label vacio = new Label(hayAviso ? "Sin información disponible." : "Todo al día. No hay nada pendiente de atención.");
            vacio.getStyleClass().add("texto-secundario");
            listaAtencion.getChildren().add(vacio);
        } else {
            for (String texto : p.atencion()) {
                Label fila = new Label(texto);
                fila.setWrapText(true);
                listaAtencion.getChildren().add(fila);
            }
        }
    }

    @FXML
    private void nuevoAfiliado() {
        navegar("nuevo-afiliado");
    }

    @FXML
    private void abrirBandeja() {
        navegar("bandeja");
    }

    private void navegar(String id) {
        if (puedeVer(Sesion.getRol(), id)) {
            Navegador.irA(id);
        }
    }
}
