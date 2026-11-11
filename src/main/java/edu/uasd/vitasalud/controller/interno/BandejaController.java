package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.model.*;
import edu.uasd.vitasalud.service.*;
import edu.uasd.vitasalud.util.Alertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;

/** Pantalla AUT interna: listado, revisión, decisión y comprobante PDF. */
public class BandejaController {
   @FXML
   private TableView<Autorizacion> tabla;
   @FXML
   private TableColumn<Autorizacion, String> colNumero, colFecha, colAfiliado, colPrestador, colEstado;
   @FXML
   private ComboBox<String> cbEstado;
   @FXML
   private Label lblDetalle, lblCobertura;
   @FXML
   private TextArea txtNota;
   private final AutorizacionService service = new AutorizacionService();
   private List<Autorizacion> datos = List.of();

   @FXML
   public void initialize() {
      colNumero.setCellValueFactory(c -> prop(c.getValue().numero()));
      colFecha.setCellValueFactory(c -> prop(String.valueOf(c.getValue().fechaSolicitud())));
      colAfiliado.setCellValueFactory(c -> prop(c.getValue().afiliadoNombre()));
      colPrestador.setCellValueFactory(c -> prop(c.getValue().prestadorNombre()));
      colEstado.setCellValueFactory(c -> prop(c.getValue().estado().name()));
      cbEstado.setItems(FXCollections.observableArrayList("TODAS", "PENDIENTE", "EN_REVISION", "APROBADA", "NEGADA",
            "ANULADA", "VENCIDA"));
      cbEstado.setValue("TODAS");
      cbEstado.valueProperty().addListener((o, a, b) -> filtrar());
      tabla.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> mostrar(b));
      recargar();
      Navegador.consumirBusquedaPendiente("bandeja").ifPresent(numero -> {
         datos.stream().filter(a -> numero.equalsIgnoreCase(a.numero())).findFirst()
               .ifPresent(tabla.getSelectionModel()::select);
      });
   }

   private SimpleStringProperty prop(String s) {
      return new SimpleStringProperty(s == null ? "" : s);
   }

   @FXML
   private void recargar() {
      try {
         datos = service.listar();
         filtrar();
      } catch (RuntimeException e) {
         Alertas.error("Bandeja", e.getMessage());
      }
   }

   private void filtrar() {
      if (cbEstado == null)
         return;
      String estado = cbEstado.getValue();
      tabla.setItems(FXCollections.observableArrayList(datos.stream()
            .filter(a -> "TODAS".equals(estado) || a.estado().name().equals(estado)).toList()));
   }

   private void mostrar(Autorizacion a) {
      if (a == null) {
         lblDetalle.setText("Seleccione una autorización");
         lblCobertura.setText("");
         return;
      }
      try {
         StringBuilder detalles = new StringBuilder();
         for (DetalleAutorizacion d : service.detalles(a.id()))
            detalles.append("• ").append(d.codigo()).append(" · ").append(d.descripcion())
                  .append(" · RD$ ").append(d.montoSolicitado()).append("\n");
         lblDetalle.setText(
               a.numero() + " | " + a.afiliadoNombre() + " | " + a.estado() + "\nPrestador: " + a.prestadorNombre() +
                     "\nServicio: " + a.tipoServicio() + "\nDiagnóstico: " + a.diagnosticoCie10() + " — "
                     + a.diagnosticoDescripcion() +
                     "\nProcedimientos:\n" + detalles);
         lblCobertura.setText("Solicitado: RD$ " + a.montoSolicitado() + " | Cubierto: RD$ " + a.montoCubierto() +
               " | Copago: RD$ " + a.copago());
         txtNota.setText(a.notaRevision() == null ? "" : a.notaRevision());
      } catch (RuntimeException e) {
         Alertas.error("Detalle", e.getMessage());
      }
   }

   @FXML
   private void aprobar() {
      operar(EstadoAutorizacion.APROBADA);
   }

   @FXML
   private void negar() {
      operar(EstadoAutorizacion.NEGADA);
   }

   @FXML
   private void pedir() {
      operar(EstadoAutorizacion.EN_REVISION);
   }

   @FXML
   private void anular() {
      operar(EstadoAutorizacion.ANULADA);
   }

   private void operar(EstadoAutorizacion destino) {
      Autorizacion a = tabla.getSelectionModel().getSelectedItem();
      if (a == null) {
         Alertas.error("Revisión", "Seleccione una autorización");
         return;
      }
      try {
         if (destino == EstadoAutorizacion.APROBADA)
            service.aprobar(a.id());
         else
            service.decidir(a.id(), destino, txtNota.getText());
         recargar();
         Alertas.info("Estado actualizado", "Autorización procesada: " + destino);
      } catch (RuntimeException e) {
         Alertas.error("No se pudo procesar", e.getMessage());
      }
   }

   @FXML
   private void vencer() {
      try {
         int n = service.vencer();
         recargar();
         Alertas.info("Vencimientos", n + " autorizaciones vencidas");
      } catch (RuntimeException e) {
         Alertas.error("Vencimientos", e.getMessage());
      }
   }

   @FXML
   private void comprobante() {
      Autorizacion a = tabla.getSelectionModel().getSelectedItem();
      if (a == null) {
         Alertas.error("PDF", "Seleccione una autorización");
         return;
      }
      if (a.estado() != EstadoAutorizacion.APROBADA) {
         Alertas.error("PDF", "El comprobante requiere autorización aprobada");
         return;
      }
      FileChooser ch = new FileChooser();
      ch.setInitialFileName(a.numero() + ".pdf");
      ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
      File f = ch.showSaveDialog(tabla.getScene().getWindow());
      if (f == null)
         return;
      try {
         new ComprobanteAutorizacionService().generar(a, service.detalles(a.id()), f.toPath());
         Alertas.info("Comprobante", "PDF guardado en " + f.getAbsolutePath());
      } catch (Exception e) {
         Alertas.error("PDF", e.getMessage());
      }
   }
}
