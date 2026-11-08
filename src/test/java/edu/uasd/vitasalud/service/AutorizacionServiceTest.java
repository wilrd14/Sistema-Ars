package edu.uasd.vitasalud.service;
import edu.uasd.vitasalud.dao.AutorizacionDAO;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
/** Pruebas sin MySQL: demuestran RN-04, RN-05 y RN-06 con entradas controladas. */
class AutorizacionServiceTest {
  private final LocalDate hoy=LocalDate.of(2026,10,25);
  private AutorizacionDAO.Contexto ctx(String afi,String prest,LocalDate inicio,
                                      AutorizacionDAO.FilaCobertura... filas){
     return new AutorizacionDAO.Contexto(afi,prest,inicio,List.of(filas));
  }
  private AutorizacionDAO.FilaCobertura fila(long id,String cat,String solicit,String tarifa,
                                            String porcentaje,String tope,String usado,int carencia){
     return new AutorizacionDAO.FilaCobertura(id,cat,1,new BigDecimal(solicit),new BigDecimal(tarifa),
        new BigDecimal(porcentaje),null,new BigDecimal(tope),carencia,new BigDecimal(usado));
  }
  @Test void rn04_apruebaAfiliadoPrestadorActivos(){
     var r=AutorizacionService.calcular(ctx("ACTIVO","ACTIVO",hoy.minusDays(90),
        fila(1,"CONSULTA","1200","900","80","30000","0",0)),hoy);
     assertEquals(0,r.cubiertoTotal().compareTo(new BigDecimal("720.00")));
  }
  @Test void rn04_noApruebaAfiliadoSuspendido(){
     assertThrows(ServicioException.class,()->AutorizacionService.calcular(ctx("SUSPENDIDO","ACTIVO",hoy.minusDays(90),
        fila(1,"CONSULTA","1200","900","80","30000","0",0)),hoy));
  }
  @Test void rn04_rechazaCarenciaNoCumplida(){
     assertThrows(ServicioException.class,()->AutorizacionService.calcular(ctx("ACTIVO","ACTIVO",hoy.minusDays(10),
        fila(1,"IMAGENES","1000","900","70","30000","0",30)),hoy));
  }
  @Test void rn05_calculaCopagoPorDiferencia(){
     var r=AutorizacionService.calcular(ctx("ACTIVO","ACTIVO",hoy.minusDays(90),
        fila(1,"CONSULTA","1200","900","80","30000","0",0)),hoy);
     assertEquals(0,r.copagoTotal().compareTo(new BigDecimal("480.00")));
  }
  @Test void rn06_topeLimitaCobertura(){
     var r=AutorizacionService.calcular(ctx("ACTIVO","ACTIVO",hoy.minusDays(90),
        fila(1,"CONSULTA","1000","1000","80","30000","29750",0)),hoy);
     assertEquals(0,r.cubiertoTotal().compareTo(new BigDecimal("250.00")));
  }
  @Test void rn06_dosDetallesMismaCategoriaNoRepitenSaldo(){
     var r=AutorizacionService.calcular(ctx("ACTIVO","ACTIVO",hoy.minusDays(90),
         fila(1,"CONSULTA","1000","1000","80","1000","0",0),
         fila(2,"CONSULTA","1000","1000","80","1000","0",0)),hoy);
     assertEquals(0,r.cubiertoTotal().compareTo(new BigDecimal("1000.00")));
  }
  @Test void rn04_sinCoberturaPlanNoAprueba(){
     var sinCobertura=new AutorizacionDAO.FilaCobertura(1,"CIRUGIA",1,new BigDecimal("1200"),
         new BigDecimal("1000"),null,null,null,0,BigDecimal.ZERO);
     assertThrows(ServicioException.class,()->AutorizacionService.calcular(ctx("ACTIVO","ACTIVO",
             hoy.minusDays(90),sinCobertura),hoy));
  }

  @Test void rn07_filtroPrestadorSeEnviaAlDAO(){
      var usuario=new edu.uasd.vitasalud.model.Usuario();
      usuario.setRol(edu.uasd.vitasalud.model.Rol.PRESTADOR);
      usuario.setPerfil(edu.uasd.vitasalud.model.Perfil.PRESTADOR);
      usuario.setPrestadorId(2L);
      edu.uasd.vitasalud.app.Sesion.iniciar(usuario);
      var recibido=new java.util.concurrent.atomic.AtomicReference<Long>();
      AutorizacionDAO dao=(AutorizacionDAO)java.lang.reflect.Proxy.newProxyInstance(
          AutorizacionDAO.class.getClassLoader(),new Class[]{AutorizacionDAO.class},(proxy,met,args)->{
              if(met.getName().equals("listar")){recibido.set((Long)args[0]);return List.of();}
              throw new IllegalStateException("Método inesperado: "+met.getName());
          });
      try{
          new AutorizacionService(dao).listar();
          assertEquals(2,recibido.get().intValue());
      }finally{edu.uasd.vitasalud.app.Sesion.cerrar();}
  }
}
