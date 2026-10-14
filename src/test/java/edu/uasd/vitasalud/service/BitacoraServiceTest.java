package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.BitacoraDAO;
import edu.uasd.vitasalud.model.Bitacora;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-SEG-07 con un DAO en memoria (no necesita MySQL). */
class BitacoraServiceTest {

    private List<Bitacora> guardados;
    private boolean fallar;
    private int ultimoLimite;
    private BitacoraService service;

    @BeforeEach
    void preparar() {
        guardados = new ArrayList<>();
        fallar = false;
        service = new BitacoraService(new BitacoraDAO() {
            @Override
            public void insertar(Bitacora b) throws SQLException {
                if (fallar) {
                    throw new SQLException("BD caída");
                }
                guardados.add(b);
            }

            @Override
            public List<Bitacora> buscar(String modulo, LocalDate desde, LocalDate hasta, int limite)
                    throws SQLException {
                if (fallar) {
                    throw new SQLException("BD caída");
                }
                ultimoLimite = limite;
                return guardados.stream()
                        .filter(b -> modulo == null || modulo.isBlank() || modulo.equals(b.getModulo()))
                        .toList();
            }
        });
    }

    @AfterEach
    void limpiarSesion() {
        Sesion.cerrar();
    }

    @Test
    void registraTodosLosCampos() {
        service.registrar(7L, "AFI", "CREAR", "afiliado", 42L, "Afiliado nuevo");
        assertEquals(1, guardados.size());
        Bitacora b = guardados.get(0);
        assertEquals(7L, b.getUsuarioId());
        assertEquals("AFI", b.getModulo());
        assertEquals("CREAR", b.getAccion());
        assertEquals("afiliado", b.getTabla());
        assertEquals(42L, b.getRegistroId());
    }

    @Test
    void registrarAceptaUsuarioYRegistroNulos() {
        service.registrar(null, "SEG", "LOGIN_FALLIDO", null, null, null);
        assertNull(guardados.get(0).getUsuarioId());
    }

    @Test
    void registrarNuncaLanzaExcepcion() {
        fallar = true;
        assertDoesNotThrow(() -> service.registrar(1L, "SEG", "LOGIN", "usuario", 1L, null));
    }

    @Test
    void recortaDetalleLargo() {
        service.registrar(1L, "SEG", "LOGIN", null, null, "x".repeat(600));
        assertEquals(500, guardados.get(0).getDetalle().length());
    }

    @Test
    void consultaFiltraPorModuloYUsaLimite() {
        service.registrar(1L, "SEG", "LOGIN", null, null, null);
        service.registrar(1L, "AFI", "CREAR", "afiliado", 1L, null);
        assertEquals(1, service.consultar("AFI", null, null, 50).size());
        assertEquals(50, ultimoLimite);
        assertEquals(2, service.listarRecientes(0).size());
        assertEquals(200, ultimoLimite);
    }

    @Test
    void rechazaRangoDeFechasInvertido() {
        assertThrows(ServicioException.class,
                () -> service.consultar(null, LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 1), 10));
    }

    @Test
    void errorDeBaseDeDatosEnConsultaSeConvierteEnServicioException() {
        fallar = true;
        assertThrows(ServicioException.class, () -> service.listarRecientes(10));
    }

    @Test
    void sinDaoInyectadoSoloElAdministradorConsulta() {
        BitacoraService real = new BitacoraService();
        assertThrows(ServicioException.class, () -> real.listarRecientes(10));

        Usuario u = new Usuario();
        u.setPerfil(Perfil.INTERNO);
        u.setRol(Rol.AGENTE_AFILIACION);
        Sesion.iniciar(u);
        assertThrows(ServicioException.class, () -> real.listarRecientes(10));
    }
}
