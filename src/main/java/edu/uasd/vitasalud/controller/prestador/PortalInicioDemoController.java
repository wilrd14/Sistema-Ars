package edu.uasd.vitasalud.controller.prestador;
import edu.uasd.vitasalud.app.Navegador;
import edu.uasd.vitasalud.service.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
/** Inicio provisional para que el flujo de Joel sea fácil de probar antes de integrar PRE de Luis. */
public class PortalInicioDemoController {
 @FXML private Label lblEstado;
 @FXML public void initialize(){
     try{lblEstado.setText("Autorizaciones propias: "+new AutorizacionService().listar().size()+
             " | Reclamaciones propias: "+new ReclamacionService().listar().size());}
     catch(RuntimeException e){lblEstado.setText("Compruebe la conexión MySQL: "+e.getMessage());}
 }
 @FXML private void solicitar(){Navegador.irA("solicitar");}
 @FXML private void reclamar(){Navegador.irA("agregar-reclamacion");}
 @FXML private void misSolicitudes(){Navegador.irA("mis-solicitudes");}
}
