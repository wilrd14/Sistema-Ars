package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.AfiliadoDAO;
import edu.uasd.vitasalud.dao.BitacoraDAO;
import edu.uasd.vitasalud.dao.CoberturaDAO;
import edu.uasd.vitasalud.dao.PlanDAO;
import edu.uasd.vitasalud.dao.PrestadorDAO;
import edu.uasd.vitasalud.model.Afiliado;
import edu.uasd.vitasalud.model.Bitacora;
import edu.uasd.vitasalud.model.Cobertura;
import edu.uasd.vitasalud.model.EstadoAfiliado;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Plan;
import edu.uasd.vitasalud.model.Prestador;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RF-SEG-07: los servicios de AFI, PSS y PLA registran en la bitácora sus altas, cambios de estado y
 * eliminaciones. Usa DAO en memoria (no necesita MySQL).
 */
class BitacoraCableadoTest {

    private List<Bitacora> eventos;
    private boolean bitacoraCaida;
    private BitacoraService bitacora;

    @BeforeEach
    void preparar() {
        eventos = new ArrayList<>();
        bitacoraCaida = false;
        bitacora = new BitacoraService(new BitacoraDAO() {
            @Override
            public void insertar(Bitacora b) throws SQLException {
                if (bitacoraCaida) {
                    throw new SQLException("BD caída");
                }
                eventos.add(b);
            }

            @Override
            public List<Bitacora> buscar(String modulo, LocalDate desde, LocalDate hasta, int limite) {
                return eventos;
            }
        });
    }

    @AfterEach
    void limpiarSesion() {
        Sesion.cerrar();
    }

    private static void iniciarSesion(long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setPerfil(Perfil.INTERNO);
        u.setRol(Rol.ADMINISTRADOR);
        Sesion.iniciar(u);
    }

    private Bitacora unico() {
        assertEquals(1, eventos.size());
        return eventos.get(0);
    }

    // ------------------------------------------------------------ registrarDeSesion

    @Test
    void registrarDeSesionUsaAlUsuarioActivo() {
        iniciarSesion(9L);
        bitacora.registrarDeSesion("AFI", "CREAR", "afiliado", 5L, "detalle");
        Bitacora b = unico();
        assertEquals(9L, b.getUsuarioId());
        assertEquals("AFI", b.getModulo());
        assertEquals("CREAR", b.getAccion());
        assertEquals("afiliado", b.getTabla());
        assertEquals(5L, b.getRegistroId());
    }

    @Test
    void registrarDeSesionSinSesionGuardaUsuarioNulo() {
        bitacora.registrarDeSesion("PSS", "CREAR", "prestador", 1L, null);
        assertNull(unico().getUsuarioId());
    }

    @Test
    void registrarDeSesionNoLanzaSiLaBitacoraFalla() {
        bitacoraCaida = true;
        assertDoesNotThrow(() -> bitacora.registrarDeSesion("PLA", "CREAR", "plan", 1L, "x"));
        assertTrue(eventos.isEmpty());
    }

    // ------------------------------------------------------------------ AFI

    @Test
    void registrarAfiliadoDejaEventoCrear() throws Exception {
        iniciarSesion(3L);
        AfiliadoService service = new AfiliadoService(new AfiliadoFalso(), bitacora);
        long id = service.registrarAfiliado(titular());
        Bitacora b = unico();
        assertEquals("AFI", b.getModulo());
        assertEquals("CREAR", b.getAccion());
        assertEquals("afiliado", b.getTabla());
        assertEquals(id, b.getRegistroId());
        assertEquals(3L, b.getUsuarioId());
    }

    @Test
    void afiliadoRechazadoPorValidacionNoSeAudita() {
        AfiliadoService service = new AfiliadoService(new AfiliadoFalso(), bitacora);
        Afiliado invalido = titular();
        invalido.setNombres("");
        assertThrows(IllegalArgumentException.class, () -> service.registrarAfiliado(invalido));
        assertTrue(eventos.isEmpty());
    }

    @Test
    void afiliadoSeRegistraAunqueLaBitacoraFalle() throws Exception {
        bitacoraCaida = true;
        AfiliadoService service = new AfiliadoService(new AfiliadoFalso(), bitacora);
        assertEquals(42L, service.registrarAfiliado(titular()));
    }

    @Test
    void afiliadoSinBitacoraInyectadaFuncionaIgual() throws Exception {
        AfiliadoService service = new AfiliadoService(new AfiliadoFalso());
        assertEquals(42L, service.registrarAfiliado(titular()));
        assertTrue(eventos.isEmpty());
    }

    // ------------------------------------------------------------------ PSS

    @Test
    void registrarPrestadorDejaEventoCrear() throws Exception {
        PrestadorService service = new PrestadorService(new PrestadorFalso(), bitacora);
        long id = service.registrarPrestador(prestador(null));
        Bitacora b = unico();
        assertEquals("PSS", b.getModulo());
        assertEquals("CREAR", b.getAccion());
        assertEquals("prestador", b.getTabla());
        assertEquals(id, b.getRegistroId());
    }

    @Test
    void actualizarPrestadorDejaEventoActualizar() throws Exception {
        PrestadorService service = new PrestadorService(new PrestadorFalso(), bitacora);
        service.actualizarPrestador(prestador(7L));
        Bitacora b = unico();
        assertEquals("ACTUALIZAR", b.getAccion());
        assertEquals(7L, b.getRegistroId());
    }

    @Test
    void cambiarEstadoDePrestadorDejaEventoConElEstado() throws Exception {
        PrestadorService service = new PrestadorService(new PrestadorFalso(), bitacora);
        service.cambiarEstadoContrato(7L, "SUSPENDIDO");
        Bitacora b = unico();
        assertEquals("CAMBIAR_ESTADO", b.getAccion());
        assertEquals(7L, b.getRegistroId());
        assertTrue(b.getDetalle().contains("SUSPENDIDO"));
    }

    @Test
    void estadoInvalidoDePrestadorNoSeAudita() {
        PrestadorService service = new PrestadorService(new PrestadorFalso(), bitacora);
        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstadoContrato(7L, "INVENTADO"));
        assertTrue(eventos.isEmpty());
    }

    // ------------------------------------------------------------------ PLA

    @Test
    void registrarPlanDejaEventoCrear() throws Exception {
        PlanService service = new PlanService(new PlanFalso(), new CoberturaFalsa(), bitacora);
        service.registrar(plan(4));
        Bitacora b = unico();
        assertEquals("PLA", b.getModulo());
        assertEquals("CREAR", b.getAccion());
        assertEquals("plan", b.getTabla());
        assertEquals(4L, b.getRegistroId());
    }

    @Test
    void registrarPlanSinIdGuardaRegistroNulo() throws Exception {
        PlanService service = new PlanService(new PlanFalso(), new CoberturaFalsa(), bitacora);
        service.registrar(plan(null));
        assertNull(unico().getRegistroId());
    }

    @Test
    void actualizarPlanDejaEventoActualizar() throws Exception {
        PlanService service = new PlanService(new PlanFalso(), new CoberturaFalsa(), bitacora);
        service.actualizar(plan(4));
        assertEquals("ACTUALIZAR", unico().getAccion());
    }

    @Test
    void eliminarPlanDejaEventoEliminar() throws Exception {
        PlanService service = new PlanService(new PlanFalso(), new CoberturaFalsa(), bitacora);
        service.eliminar(4);
        Bitacora b = unico();
        assertEquals("ELIMINAR", b.getAccion());
        assertEquals(4L, b.getRegistroId());
    }

    @Test
    void planConCoberturasNoSeEliminaNiSeAudita() {
        CoberturaFalsa coberturas = new CoberturaFalsa();
        Cobertura c = new Cobertura(1L, 4, "CONSULTA", new BigDecimal("80"), BigDecimal.ZERO,
                new BigDecimal("50000"), 0, false);
        coberturas.lista.add(c);
        PlanService service = new PlanService(new PlanFalso(), coberturas, bitacora);
        assertThrows(IllegalStateException.class, () -> service.eliminar(4));
        assertTrue(eventos.isEmpty());
    }

    @Test
    void planSinBitacoraInyectadaFuncionaIgual() {
        PlanService service = new PlanService(new PlanFalso(), new CoberturaFalsa());
        assertDoesNotThrow(() -> service.registrar(plan(4)));
        assertTrue(eventos.isEmpty());
    }

    // ------------------------------------------------------------ datos de prueba

    private static Afiliado titular() {
        Afiliado a = new Afiliado();
        a.setTipoDocumento("PASAPORTE");
        a.setNumeroDocumento("P-1001");
        a.setNss("NSS-1");
        a.setNombres("Persona");
        a.setApellidos("De Prueba");
        a.setFechaNacimiento(LocalDate.now().minusYears(40));
        a.setSexo('M');
        a.setPlanId(1);
        a.setTipoAfiliado("TITULAR");
        a.setInicioCobertura(LocalDate.now());
        return a;
    }

    private static Prestador prestador(Long id) {
        Prestador p = new Prestador();
        p.setId(id);
        p.setNombre("Clínica Central");
        p.setRncCedula("101234567");
        p.setTipo("CLINICA");
        p.setEstadoContrato("ACTIVO");
        return p;
    }

    private static Plan plan(Integer id) {
        return new Plan(id, "PLN-1", "Plan Básico", "Plan de prueba", new BigDecimal("1500.00"), "ACTIVO");
    }

    // ------------------------------------------------------------------ DAO falsos

    private static class AfiliadoFalso implements AfiliadoDAO {
        @Override public Optional<Afiliado> buscarPorNumeroDocumento(String t, String n) { return Optional.empty(); }
        @Override public long registrarAfiliado(Afiliado a) { return 42L; }
        @Override public void actualizarAfiliado(Afiliado a) { }
        @Override public void cambiarEstado(long id, EstadoAfiliado e) { }
        @Override public List<Afiliado> listarAfiliados() { return List.of(); }
        @Override public List<Afiliado> buscarPorNombre(String n) { return List.of(); }
        @Override public Optional<Afiliado> buscarPorNss(String nss) { return Optional.empty(); }
        @Override public boolean existeConyugePorTitular(long id) { return false; }
        @Override public Optional<Afiliado> buscarPorId(long id) { return Optional.empty(); }
        @Override public List<Afiliado> listarDependientes(long id) { return List.of(); }
        @Override public List<Afiliado> buscar(String criterio) { return List.of(); }
    }

    private static class PrestadorFalso implements PrestadorDAO {
        @Override public Optional<Prestador> buscarPorRncCedula(String r) { return Optional.empty(); }
        @Override public long registrarPrestador(Prestador p) { return 11L; }
        @Override public void actualizarPrestador(Prestador p) { }
        @Override public void cambiarEstadoContrato(Long id, String e) { }
        @Override public List<Prestador> listarPrestadores() { return List.of(); }
        @Override public List<Prestador> buscarPorNombre(String n) { return List.of(); }
    }

    private static class PlanFalso implements PlanDAO {
        @Override public Optional<Plan> buscarPorId(String id) { return Optional.empty(); }
        @Override public List<Plan> listarTodo() { return List.of(); }
        @Override public void registrar(Plan p) { }
        @Override public void actualizar(Plan p) { }
        @Override public void eliminar(int id) { }
    }

    private static class CoberturaFalsa implements CoberturaDAO {
        final List<Cobertura> lista = new ArrayList<>();
        @Override public Optional<Cobertura> buscarPorId(Long id) { return Optional.empty(); }
        @Override public List<Cobertura> listarTodo() { return lista; }
        @Override public void registrar(Cobertura c) { }
        @Override public void actualizar(Cobertura c) { }
        @Override public void eliminar(Long id) { }
    }
}
