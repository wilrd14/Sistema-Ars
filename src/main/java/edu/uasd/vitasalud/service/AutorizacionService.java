package edu.uasd.vitasalud.service;
import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.AutorizacionDAO;
import edu.uasd.vitasalud.dao.impl.AutorizacionDAOJdbc;
import edu.uasd.vitasalud.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
/**
 * REGLAS DE NEGOCIO AUT (RN-04..07 y RN-11).
 * El controlador solo recoge entradas y muestra resultados.
 * La calculadora pura es testeable sin MySQL, para que Joel comprenda cada fórmula.
 */
public class AutorizacionService {
    private final AutorizacionDAO dao;
    public AutorizacionService() { this(new AutorizacionDAOJdbc()); }
    public AutorizacionService(AutorizacionDAO dao) { this.dao=Objects.requireNonNull(dao); }

    /** Identificador del prestador autenticado. Ningún formulario puede suplantarlo (RN-07). */
    private long prestadorActual() {
        if (Sesion.getRol()!=Rol.PRESTADOR || Sesion.getPrestadorId()==null)
            throw new ServicioException("Solo los prestadores pueden realizar esta operación");
        return Sesion.getPrestadorId();
    }
    private void verificarAnalista() {
        if(Sesion.getRol()!=Rol.ADMINISTRADOR && Sesion.getRol()!=Rol.ANALISTA_AUTORIZACIONES)
            throw new ServicioException("Debe ser analista de autorizaciones o administrador");
    }
    private Long filtro() {return Sesion.getRol()==Rol.PRESTADOR?prestadorActual():null;}
    private static ServicioException error(String msg, SQLException cause) {
        return new ServicioException(msg+": "+cause.getMessage(),cause);
    }
    public List<AfiliadoBusqueda> buscarAfiliados(String texto) {
        prestadorActual();
        if(texto==null||texto.trim().length()<2) throw new ServicioException("Escriba al menos dos caracteres");
        try {return dao.buscarAfiliados(texto.trim());}catch(SQLException e){throw error("Búsqueda no disponible",e);}
    }
    public List<ProcedimientoBusqueda> procedimientos() {
        prestadorActual();
        try{return dao.procedimientos();}catch(SQLException e){throw error("No se pudo leer el catálogo",e);}
    }
    public String solicitar(AfiliadoBusqueda af,String servicio,LocalDate fechaServicio,String tipoTelefono,
            String medico,String telefono,String correo,
            String cie,String diagnostico,boolean laboral,List<DetalleAutorizacion> detalles) {
        long prestador=prestadorActual();
        if(af==null)throw new ServicioException("Seleccione un afiliado del buscador");
        if(!"ACTIVO".equals(af.estado()))throw new ServicioException("El afiliado no está ACTIVO");
        if(vacio(servicio)||fechaServicio==null||vacio(tipoTelefono)||vacio(medico)||vacio(telefono)||vacio(cie)||vacio(diagnostico))
            throw new ServicioException("Complete servicio, médico, teléfono y diagnóstico");
        if(detalles==null||detalles.isEmpty())throw new ServicioException("Incluya al menos un procedimiento");
        BigDecimal solicitado=BigDecimal.ZERO;
        for(DetalleAutorizacion d:detalles){
            if(d.cantidad()<1||d.montoSolicitado()==null||d.montoSolicitado().signum()<=0)
                throw new ServicioException("Cantidad y monto de cada procedimiento deben ser positivos");
            solicitado=solicitado.add(d.montoSolicitado());
        }
        Autorizacion a=new Autorizacion(null,null,af.id(),prestador,Sesion.getUsuario().getId(),
            af.nombreCompleto(),null,LocalDateTime.now(),servicio,fechaServicio,tipoTelefono,medico,telefono,correo,cie,diagnostico,
            laboral,solicitado,null,null,EstadoAutorizacion.PENDIENTE,null,null);
        try {String numero=dao.crear(a,detalles);
            auditar("CREAR",null,"Nueva solicitud "+numero);return numero;}catch(SQLException e){throw error("No se guardó la autorización",e);}
    }
    public List<Autorizacion> listar(){
        try{return dao.listar(filtro());}catch(SQLException e){throw error("No se pudo abrir la bandeja",e);}
    }
    public Autorizacion buscar(long id){
        try{return dao.buscar(id,filtro()).orElseThrow(()->new ServicioException("Autorización no disponible"));}
        catch(SQLException e){throw error("Consulta fallida",e);}
    }
    public List<DetalleAutorizacion> detalles(long id){
        try{return dao.detalles(id,filtro());}catch(SQLException e){throw error("No se pudieron leer los procedimientos",e);}
    }
    public void aprobar(long id){
        verificarAnalista();
        try {dao.aprobar(id,Sesion.getUsuario().getId(),ctx->calcular(ctx,LocalDate.now()));
            auditar("APROBAR",id,"Autorización aprobada");}
        catch(SQLException e){throw error("No se pudo aprobar",e);}
    }
    public void decidir(long id,EstadoAutorizacion estado,String nota){
        verificarAnalista();
        if(estado!=EstadoAutorizacion.NEGADA && estado!=EstadoAutorizacion.EN_REVISION && estado!=EstadoAutorizacion.ANULADA)
            throw new ServicioException("Use aprobar() o un estado de revisión válido");
        if((estado==EstadoAutorizacion.NEGADA||estado==EstadoAutorizacion.EN_REVISION)&&vacio(nota))
            throw new ServicioException("La nota de revisión es obligatoria");
        try {
            if(!dao.cambiarEstado(id,Sesion.getUsuario().getId(),estado,nota))
                throw new ServicioException("La transición no es válida o ya cambió el estado");
            auditar(estado.name(),id,nota);
        }catch(SQLException e){throw error("No se pudo guardar la decisión",e);}
    }
    public int vencer(){
        verificarAnalista();
        try{return dao.vencerSinReclamacion();}catch(SQLException e){throw error("Error al vencer autorizaciones",e);}
    }
    public void ingreso(long id,LocalDateTime fecha,String nota){
        long pid=prestadorActual();if(fecha==null)throw new ServicioException("Fecha requerida");
        try{dao.registrarIngreso(id,pid,fecha,nota);}catch(SQLException e){throw error("No se pudo registrar el ingreso",e);}
    }
    public void egreso(long id,LocalDateTime fecha){
        long pid=prestadorActual();if(fecha==null)throw new ServicioException("Fecha requerida");
        try{dao.registrarEgreso(id,pid,fecha);}catch(SQLException e){throw error("No se pudo registrar el egreso",e);}
    }
    public List<Hospitalizacion> hospitalizaciones(){
        long pid=prestadorActual();
        try{return dao.hospitalizaciones(pid);}catch(SQLException e){throw error("Error leyendo hospitalizaciones",e);}
    }

    /** La bitácora registra las acciones AUT, sin exponer datos sensibles en la nota. */
    private void auditar(String accion,Long id,String detalle){
        new BitacoraService().registrar(Sesion.getUsuario().getId(),"AUT",accion,"autorizacion",id,detalle);
    }
    private static boolean vacio(String s){return s==null||s.isBlank();}

    /**
     * RN-04/RN-05/RN-06: cálculo puro, independiente de JDBC y de la interfaz.
     * El mapa "reservado" evita sumar dos veces un mismo saldo en solicitudes con varias líneas de igual categoría.
     */
    public static AutorizacionDAO.Resultado calcular(AutorizacionDAO.Contexto ctx,LocalDate hoy){
        if(!"ACTIVO".equals(ctx.estadoAfiliado()))throw new ServicioException("Afiliado inactivo (RN-04)");
        if(!"ACTIVO".equals(ctx.estadoPrestador()))throw new ServicioException("Prestador sin contrato ACTIVO (RN-04)");
        if(ctx.filas().isEmpty())throw new ServicioException("No existen procedimientos");
        Map<String,BigDecimal> reservado=new HashMap<>();
        BigDecimal total=BigDecimal.ZERO,copago=BigDecimal.ZERO;
        List<AutorizacionDAO.LineaCalculada> lineas=new ArrayList<>();
        for(AutorizacionDAO.FilaCobertura d:ctx.filas()){
            if(d.porcentaje()==null||d.topeAnual()==null)
                throw new ServicioException("El plan no cubre "+d.categoria());
            if(ctx.inicioCobertura()==null||ChronoUnit.DAYS.between(ctx.inicioCobertura(),hoy)<d.diasCarencia())
                throw new ServicioException("No se cumplió carencia de "+d.categoria());
            // El importe solicitado de cada línea ya representa cantidad * precio solicitado.
            BigDecimal tarifaLinea=d.tarifa().multiply(BigDecimal.valueOf(d.cantidad()));
            BigDecimal base=d.solicitado().min(tarifaLinea);
            BigDecimal candidato=base.multiply(d.porcentaje()).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
            BigDecimal disponible=d.topeAnual().subtract(d.consumidoAnual())
                    .subtract(reservado.getOrDefault(d.categoria(),BigDecimal.ZERO)).max(BigDecimal.ZERO);
            BigDecimal cubierto=candidato.min(disponible);
            reservado.merge(d.categoria(),cubierto,BigDecimal::add);
            total=total.add(cubierto);
            BigDecimal cargo=d.copagoFijo()==null ? d.solicitado().subtract(cubierto) : d.copagoFijo();
            copago=copago.add(cargo.max(BigDecimal.ZERO));
            lineas.add(new AutorizacionDAO.LineaCalculada(d.detalleId(),cubierto));
        }
        if(total.signum()==0)throw new ServicioException("No hay saldo de cobertura disponible");
        return new AutorizacionDAO.Resultado(total,copago,lineas);
    }
}
