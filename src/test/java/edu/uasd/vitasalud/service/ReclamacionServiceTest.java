package edu.uasd.vitasalud.service;
import edu.uasd.vitasalud.model.EstadoReclamacion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/** RN-09: transiciones de auditoría, glosa y cierre inmutable. */
class ReclamacionServiceTest {
  @Test void recibidaEntraAuditoria(){
     assertTrue(ReclamacionService.transicionPermitida(EstadoReclamacion.RECIBIDA,EstadoReclamacion.EN_AUDITORIA));
  }
  @Test void recibidaNoSePuedePagarDirectamente(){
     assertFalse(ReclamacionService.transicionPermitida(EstadoReclamacion.RECIBIDA,EstadoReclamacion.PAGADA));
  }
  @Test void auditoriaPermiteAprobarDevolverRechazar(){
     assertTrue(ReclamacionService.transicionPermitida(EstadoReclamacion.EN_AUDITORIA,EstadoReclamacion.APROBADA));
     assertTrue(ReclamacionService.transicionPermitida(EstadoReclamacion.EN_AUDITORIA,EstadoReclamacion.DEVUELTA));
     assertTrue(ReclamacionService.transicionPermitida(EstadoReclamacion.EN_AUDITORIA,EstadoReclamacion.RECHAZADA));
  }
  @Test void pagadaNoPuedeMutar(){
     for(var destino:EstadoReclamacion.values())
         assertFalse(ReclamacionService.transicionPermitida(EstadoReclamacion.PAGADA,destino));
  }
  @Test void glosadaNoSePuedeRechazarLuegoDelCalculo(){
     assertFalse(ReclamacionService.transicionPermitida(EstadoReclamacion.GLOSADA,EstadoReclamacion.RECHAZADA));
  }

  @Test void rn07_filtroDeReclamacionesUsaPrestadorSesion(){
      var usuario=new edu.uasd.vitasalud.model.Usuario();
      usuario.setRol(edu.uasd.vitasalud.model.Rol.PRESTADOR);
      usuario.setPerfil(edu.uasd.vitasalud.model.Perfil.PRESTADOR);
      usuario.setPrestadorId(9L);
      edu.uasd.vitasalud.app.Sesion.iniciar(usuario);
      var recibido=new java.util.concurrent.atomic.AtomicReference<Long>();
      edu.uasd.vitasalud.dao.ReclamacionDAO dao=(edu.uasd.vitasalud.dao.ReclamacionDAO)
          java.lang.reflect.Proxy.newProxyInstance(edu.uasd.vitasalud.dao.ReclamacionDAO.class.getClassLoader(),
              new Class[]{edu.uasd.vitasalud.dao.ReclamacionDAO.class},(proxy,met,args)->{
                if(met.getName().equals("listar")){recibido.set((Long)args[0]);return java.util.List.of();}
                throw new IllegalStateException("Método inesperado");
              });
      try{new ReclamacionService(dao).listar();assertEquals(9,recibido.get().intValue());}
      finally{edu.uasd.vitasalud.app.Sesion.cerrar();}
  }
}
