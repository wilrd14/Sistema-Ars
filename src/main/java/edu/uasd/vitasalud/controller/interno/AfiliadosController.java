package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.model.Afiliado;
import edu.uasd.vitasalud.service.AfiliadoService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** Controlador de "Afiliados": búsqueda y ficha (RF-AFI-06). */
public class AfiliadosController {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AfiliadoService afiliadoService = new AfiliadoService();

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<Afiliado> tablaAfiliados;

    @FXML
    private TableColumn<Afiliado, String> colNumero;

    @FXML
    private TableColumn<Afiliado, String> colDocumento;

    @FXML
    private TableColumn<Afiliado, String> colNombres;

    @FXML
    private TableColumn<Afiliado, String> colApellidos;

    @FXML
    private TableColumn<Afiliado, String> colEstado;

    @FXML
    private void initialize() {
        colNumero.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNumeroAfiliado()));
        colDocumento.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNumeroDocumento()));
        colNombres.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNombres()));
        colApellidos.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getApellidos()));
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstado().etiqueta()));

        // Enter en el buscador también busca.
        txtBuscar.setOnAction(e -> buscarAfiliados());

        // Doble clic en una fila abre la ficha del afiliado.
        tablaAfiliados.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                Afiliado seleccionado =
                        tablaAfiliados.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    mostrarFicha(seleccionado);
                }
            }
        });

        // Búsqueda global (RF-SEG-08): llega con el número de afiliado ya escrito.
        Navegador.consumirBusquedaPendiente("afiliados").ifPresent(txtBuscar::setText);
        cargar(txtBuscar.getText() == null ? "" : txtBuscar.getText().trim());
    }

    @FXML
    private void buscarAfiliados() {
        cargar(txtBuscar.getText().trim());
    }

    @FXML
    private void nuevoAfiliado() {
        Navegador.irA("nuevo-afiliado");
    }

    private void cargar(String criterio) {
        try {
            tablaAfiliados.setItems(FXCollections.observableArrayList(
                    afiliadoService.buscar(criterio)));
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje(Alert.AlertType.ERROR, "Afiliados",
                    "No se pudo consultar la base de datos.");
        }
    }

    // Ficha del afiliado con sus dependientes.
    private void mostrarFicha(Afiliado a) {
        StringBuilder ficha = new StringBuilder();

        ficha.append("N.º de afiliado: ").append(a.getNumeroAfiliado()).append("\n");
        ficha.append("Nombre: ").append(a.getNombres()).append(" ")
                .append(a.getApellidos()).append("\n");
        ficha.append("Documento: ").append(a.getTipoDocumento()).append(" ")
                .append(a.getNumeroDocumento()).append("\n");
        ficha.append("NSS: ").append(a.getNss()).append("\n");
        ficha.append("Fecha de nacimiento: ")
                .append(a.getFechaNacimiento().format(FECHA)).append("\n");
        ficha.append("Teléfono: ").append(valor(a.getTelefono())).append("\n");
        ficha.append("Correo: ").append(valor(a.getCorreo())).append("\n");
        ficha.append("Tipo: ").append(a.getTipoAfiliado()).append("\n");
        ficha.append("Inicio de cobertura: ")
                .append(a.getInicioCobertura().format(FECHA)).append("\n");
        ficha.append("Estado: ").append(a.getEstado().etiqueta()).append("\n");

        if ("TITULAR".equalsIgnoreCase(a.getTipoAfiliado())) {
            ficha.append("\nDependientes:\n");
            try {
                List<Afiliado> dependientes =
                        afiliadoService.listarDependientes(a.getId());

                if (dependientes.isEmpty()) {
                    ficha.append("  (sin dependientes)\n");
                }
                for (Afiliado d : dependientes) {
                    ficha.append("  - ").append(d.getNombres()).append(" ")
                            .append(d.getApellidos()).append(" (")
                            .append(valor(d.getParentesco())).append(", ")
                            .append(d.getEstado().etiqueta()).append(")\n");
                }
            } catch (SQLException e) {
                ficha.append("  (no se pudieron cargar)\n");
                e.printStackTrace();
            }
        }

        mostrarMensaje(Alert.AlertType.INFORMATION, "Ficha del afiliado",
                ficha.toString());
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}