package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.UsuarioDAO;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-SEG-01, RF-SEG-04, RF-SEG-05 y RF-SEG-06 con un DAO en memoria (no necesita MySQL). */
class UsuarioServiceTest {

    /** DAO falso: guarda los usuarios en una lista. */
    private static class DaoMemoria implements UsuarioDAO {
        final List<Usuario> usuarios = new ArrayList<>();
        final List<PrestadorOpcion> prestadores = List.of(new PrestadorOpcion(7, "Clínica Corazones", "ACTIVO"));
        long siguienteId = 100;

        @Override
        public Optional<Usuario> buscarPorUsername(String username) {
            return usuarios.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(long id) {
            return usuarios.stream().filter(u -> u.getId() == id).findFirst();
        }

        @Override
        public List<Usuario> listar(String filtro) {
            String f = filtro == null ? "" : filtro.trim().toLowerCase();
            return usuarios.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(f) || u.getUsername().toLowerCase().contains(f))
                    .toList();
        }

        @Override
        public List<PrestadorOpcion> listarPrestadores() {
            return prestadores;
        }

        @Override
        public long insertar(Usuario usuario) {
            usuario.setId(siguienteId++);
            usuarios.add(usuario);
            return usuario.getId();
        }

        @Override
        public void actualizar(Usuario usuario) {
            // El objeto ya es el mismo de la lista.
        }

        @Override
        public void actualizarPassword(long usuarioId, String passwordHash) {
            buscarPorId(usuarioId).orElseThrow().setPasswordHash(passwordHash);
        }

        @Override
        public int registrarIntentoFallido(long usuarioId) {
            Usuario u = buscarPorId(usuarioId).orElseThrow();
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            return u.getIntentosFallidos();
        }

        @Override
        public void reiniciarIntentos(long usuarioId) {
            buscarPorId(usuarioId).orElseThrow().setIntentosFallidos(0);
        }

        @Override
        public void cambiarEstado(long usuarioId, String estado) {
            buscarPorId(usuarioId).orElseThrow().setEstado(estado);
        }
    }

    /** Bitácora falsa: guarda las acciones registradas y nunca toca la base de datos. */
    private static class BitacoraFalsa extends BitacoraService {
        final List<String> acciones = new ArrayList<>();

        BitacoraFalsa() {
            super(null);
        }

        @Override
        public void registrar(Long usuarioId, String modulo, String accion, String tabla, Long registroId,
                              String detalle) {
            acciones.add(accion);
        }
    }

    private DaoMemoria dao;
    private BitacoraFalsa bitacora;
    private Usuario admin;
    private Usuario agente;
    private Usuario actor;
    private UsuarioService service;

    private static Usuario crearUsuario(long id, String username, Perfil perfil, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Persona " + username);
        u.setUsername(username);
        u.setPasswordHash(BCrypt.hashpw("Vita2026!", BCrypt.gensalt(4)));
        u.setPerfil(perfil);
        u.setRol(rol);
        u.setEstado(Usuario.ESTADO_ACTIVO);
        return u;
    }

    @BeforeEach
    void preparar() {
        dao = new DaoMemoria();
        bitacora = new BitacoraFalsa();
        admin = crearUsuario(1, "admin", Perfil.INTERNO, Rol.ADMINISTRADOR);
        agente = crearUsuario(2, "agente", Perfil.INTERNO, Rol.AGENTE_AFILIACION);
        dao.usuarios.add(admin);
        dao.usuarios.add(agente);
        actor = admin;
        service = new UsuarioService(dao, bitacora, () -> actor);
    }

    // ---------------------------------------------------------------- login (RF-SEG-01 / RF-SEG-06)

    @Test
    void credencialesCorrectasEntranPorSuPerfil() {
        assertEquals("agente", service.autenticar("agente", "Vita2026!", Perfil.INTERNO).getUsername());
        assertTrue(bitacora.acciones.contains("LOGIN"));
    }

    @Test
    void unUsuarioInternoNoEntraPorElPerfilPrestador() {
        assertThrows(ServicioException.class, () -> service.autenticar("agente", "Vita2026!", Perfil.PRESTADOR));
    }

    @Test
    void claveIncorrectaSeRechazaYQuedaEnBitacora() {
        assertThrows(ServicioException.class, () -> service.autenticar("agente", "mala", Perfil.INTERNO));
        assertTrue(bitacora.acciones.contains("LOGIN_FALLIDO"));
        assertEquals(1, agente.getIntentosFallidos());
    }

    @Test
    void cuentaSeBloqueaTrasCincoIntentosFallidos() {
        for (int i = 0; i < UsuarioService.MAX_INTENTOS - 1; i++) {
            assertThrows(ServicioException.class, () -> service.autenticar("agente", "mala", Perfil.INTERNO));
            assertEquals(Usuario.ESTADO_ACTIVO, agente.getEstado());
        }
        assertThrows(ServicioException.class, () -> service.autenticar("agente", "mala", Perfil.INTERNO));
        assertEquals(Usuario.ESTADO_BLOQUEADO, agente.getEstado());
        assertTrue(bitacora.acciones.contains("BLOQUEO_CUENTA"));
        // Aun con la clave correcta, una cuenta bloqueada no entra.
        assertThrows(ServicioException.class, () -> service.autenticar("agente", "Vita2026!", Perfil.INTERNO));
    }

    // ---------------------------------------------------------------- crear usuario (RF-SEG-04)

    @Test
    void crearUsuarioInternoValidoGuardaClaveCifrada() {
        long id = service.crear("Ana Pérez", "aperez", "Clave2026", Rol.AUDITOR_MEDICO, "Auditora", null);
        Usuario creado = dao.buscarPorId(id).orElseThrow();
        assertEquals(Perfil.INTERNO, creado.getPerfil());
        assertEquals(Usuario.ESTADO_ACTIVO, creado.getEstado());
        assertNull(creado.getPrestadorId());
        assertTrue(BCrypt.checkpw("Clave2026", creado.getPasswordHash()));
        assertTrue(bitacora.acciones.contains("CREAR_USUARIO"));
    }

    @Test
    void usuarioDuplicadoSeRechaza() {
        assertThrows(ServicioException.class,
                () -> service.crear("Otro Agente", "AGENTE", "Clave2026", Rol.AGENTE_AFILIACION, null, null));
        assertDoesNotThrow(
                () -> service.crear("Otro Agente", "agente2", "Clave2026", Rol.AGENTE_AFILIACION, null, null));
    }

    @Test
    void usuarioDePrestadorExigePrestadorExistente() {
        assertThrows(ServicioException.class,
                () -> service.crear("Dr. Gómez", "dgomez", "Clave2026", Rol.PRESTADOR, null, null));
        assertThrows(ServicioException.class,
                () -> service.crear("Dr. Gómez", "dgomez", "Clave2026", Rol.PRESTADOR, null, 999L));
        long id = service.crear("Dr. Gómez", "dgomez", "Clave2026", Rol.PRESTADOR, null, 7L);
        assertEquals(Perfil.PRESTADOR, dao.buscarPorId(id).orElseThrow().getPerfil());
        assertEquals(7L, dao.buscarPorId(id).orElseThrow().getPrestadorId());
    }

    @Test
    void usuarioInternoNoLlevaPrestador() {
        assertThrows(ServicioException.class,
                () -> service.crear("Ana Pérez", "aperez", "Clave2026", Rol.AUDITOR_MEDICO, null, 7L));
    }

    @Test
    void claveDebeTenerLongitudLetrasYNumeros() {
        assertThrows(ServicioException.class,
                () -> service.crear("Ana Pérez", "aperez", "corta1", Rol.AUDITOR_MEDICO, null, null));
        assertThrows(ServicioException.class,
                () -> service.crear("Ana Pérez", "aperez", "soloLetrasAqui", Rol.AUDITOR_MEDICO, null, null));
        assertDoesNotThrow(() -> service.crear("Ana Pérez", "aperez", "Clave2026", Rol.AUDITOR_MEDICO, null, null));
    }

    @Test
    void nombreYUsuarioSonObligatorios() {
        assertThrows(ServicioException.class,
                () -> service.crear(" ", "aperez", "Clave2026", Rol.AUDITOR_MEDICO, null, null));
        assertThrows(ServicioException.class,
                () -> service.crear("Ana Pérez", "a b", "Clave2026", Rol.AUDITOR_MEDICO, null, null));
    }

    // ---------------------------------------------------------------- editar

    @Test
    void editarCambiaNombreRolYCargo() {
        service.editar(2, "Agente Nuevo", Rol.ANALISTA_AUTORIZACIONES, "Analista", null);
        assertEquals("Agente Nuevo", agente.getNombre());
        assertEquals(Rol.ANALISTA_AUTORIZACIONES, agente.getRol());
        assertEquals("Analista", agente.getCargo());
    }

    @Test
    void editarNoPermiteRolDeOtroPerfilNiQuitarseElRolAdministrador() {
        assertThrows(ServicioException.class, () -> service.editar(2, "Agente", Rol.PRESTADOR, null, 7L));
        assertThrows(ServicioException.class, () -> service.editar(1, "Admin", Rol.AUDITOR_MEDICO, null, null));
        assertDoesNotThrow(() -> service.editar(1, "Admin Renombrado", Rol.ADMINISTRADOR, null, null));
    }

    // ---------------------------------------------------------------- permisos (solo Administrador)

    @Test
    void soloElAdministradorGestionaUsuarios() {
        actor = agente;
        assertThrows(ServicioException.class, () -> service.listar(null));
        assertThrows(ServicioException.class,
                () -> service.crear("Ana Pérez", "aperez", "Clave2026", Rol.AUDITOR_MEDICO, null, null));
        assertThrows(ServicioException.class, () -> service.cambiarEstado(2, false));
        actor = null;
        assertThrows(ServicioException.class, () -> service.listar(null));
        actor = admin;
        assertEquals(2, service.listar(null).size());
    }

    // ---------------------------------------------------------------- activar / desactivar (RN-10)

    @Test
    void desactivarYActivarOtroUsuarioConservaElRegistro() {
        service.cambiarEstado(2, false);
        assertEquals(Usuario.ESTADO_INACTIVO, agente.getEstado());
        assertEquals(2, dao.usuarios.size());
        service.cambiarEstado(2, true);
        assertEquals(Usuario.ESTADO_ACTIVO, agente.getEstado());
        assertTrue(bitacora.acciones.containsAll(List.of("DESACTIVAR_USUARIO", "ACTIVAR_USUARIO")));
    }

    @Test
    void elAdministradorNoPuedeDesactivarseASiMismo() {
        assertThrows(ServicioException.class, () -> service.cambiarEstado(1, false));
        assertEquals(Usuario.ESTADO_ACTIVO, admin.getEstado());
    }

    @Test
    void unaCuentaBloqueadaNoSeActivaSinDesbloquear() {
        agente.setEstado(Usuario.ESTADO_BLOQUEADO);
        assertThrows(ServicioException.class, () -> service.cambiarEstado(2, true));
    }

    // ---------------------------------------------------------------- desbloquear (RF-SEG-06)

    @Test
    void administradorDesbloqueaYReiniciaIntentos() {
        agente.setEstado(Usuario.ESTADO_BLOQUEADO);
        agente.setIntentosFallidos(5);
        service.desbloquear(2);
        assertEquals(Usuario.ESTADO_ACTIVO, agente.getEstado());
        assertEquals(0, agente.getIntentosFallidos());
        assertTrue(bitacora.acciones.contains("DESBLOQUEAR_USUARIO"));
    }

    @Test
    void desbloquearExigeAdministradorYCuentaBloqueada() {
        agente.setEstado(Usuario.ESTADO_BLOQUEADO);
        actor = agente;
        assertThrows(ServicioException.class, () -> service.desbloquear(2));
        assertEquals(Usuario.ESTADO_BLOQUEADO, agente.getEstado());
        actor = admin;
        service.desbloquear(2);
        assertThrows(ServicioException.class, () -> service.desbloquear(2));
    }

    // ---------------------------------------------------------------- restablecer clave

    @Test
    void restablecerClaveCifraLaNuevaClave() {
        service.restablecerClave(2, "NuevaClave9");
        assertTrue(BCrypt.checkpw("NuevaClave9", agente.getPasswordHash()));
        assertTrue(bitacora.acciones.contains("RESTABLECER_CLAVE"));
    }

    @Test
    void restablecerClaveRechazaClaveDebilYUsuarioInexistente() {
        assertThrows(ServicioException.class, () -> service.restablecerClave(2, "123"));
        assertThrows(ServicioException.class, () -> service.restablecerClave(999, "NuevaClave9"));
        actor = agente;
        assertThrows(ServicioException.class, () -> service.restablecerClave(2, "NuevaClave9"));
    }
}
