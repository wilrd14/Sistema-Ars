package edu.uasd.vitasalud.controller.prestador;
import edu.uasd.vitasalud.model.*;
import edu.uasd.vitasalud.service.AutorizacionService;
import edu.uasd.vitasalud.util.Alertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.*;
/** Pantalla provisional de consulta: muestra estados, notas y montos exclusivamente del prestador. */
public class MisSolicitudesDemoController {
 @FXML private ListView<Autorizacion> lista;
 @FXML private Label detalle;
 private final AutorizacionService service=new AutorizacionService();
 @FXML public void initialize(){
     lista.setCellFactory(v->new ListCell<>(){
       @Override protected void updateItem(Autorizacion a,boolean empty){super.updateItem(a,empty);
          setText(empty||a==null?null:a.numero()+" · "+a.afiliadoNombre()+" · "+a.estado());}
     });
     lista.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{
         detalle.setText(b==null?"Seleccione autorización":
             b.numero()+"\nEstado: "+b.estado()+"\nCubierto: "+b.montoCubierto()+
             "\nCopago: "+b.copago()+"\nNota de revisión: "+b.notaRevision());
     });
     recargar();
 }
 @FXML private void recargar(){try{lista.setItems(FXCollections.observableArrayList(service.listar()));}
     catch(RuntimeException e){Alertas.error("Mis solicitudes",e.getMessage());}}
}
