package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.UsuarioDAO;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RF-SEG-01 y RF-SEG-06 con un DAO en memoria (no necesita MySQL). */
class UsuarioServiceTest {

    private Usuario usuario;
    private int intentos;
    private UsuarioService service;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("agente");
        usuario.setPasswordHash(BCrypt.hashpw("Vita2026!", BCrypt.gensalt(4)));
        usuario.setPerfil(Perfil.INTERNO);
        usuario.setRol(Rol.AGENTE_AFILIACION);
        usuario.setEstado(Usuario.ESTADO_ACTIVO);
        intentos = 0;

        service = new UsuarioService(new UsuarioDAO() {
            @Override
            public Optional<Usuario> buscarPorUsername(String username) {
                return username.equals(usuario.getUsername()) ? Optional.of(usuario) : Optional.empty();
            }

            @Override
            public int registrarIntentoFallido(long usuarioId) {
                return ++intentos;
            }

            @Override
            public void reiniciarIntentos(long usuarioId) {
                intentos = 0;
            }

            @Override
            public void cambiarEstado(long usuarioId, String estado) {
                usuario.setEstado(estado);
            }
        });
    }

    @Test
    void credencialesCorrectasEntranPorSuPerfil() {
        assertEquals("agente", service.autenticar("agente", "Vita2026!", Perfil.INTERNO).getUsername());
    }

    @Test
    void unUsuarioInternoNoEntraPorElPerfilPrestador() {
        assertThrows(ServicioException.class, () -> service.autenticar("agente", "Vita2026!", Perfil.PRESTADOR));
    }

    @Test
    void claveIncorrectaSeRechaza() {
        assertThrows(ServicioException.class, () -> service.autenticar("agente", "mala", Perfil.INTERNO));
    }

    @Test
    void cuentaSeBloqueaTrasCincoIntentosFallidos() {
        for (int i = 0; i < UsuarioService.MAX_INTENTOS; i++) {
            assertThrows(ServicioException.class, () -> service.autenticar("agente", "mala", Perfil.INTERNO));
        }
        assertEquals(Usuario.ESTADO_BLOQUEADO, usuario.getEstado());
        // Aun con la clave correcta, una cuenta bloqueada no entra.
        assertThrows(ServicioException.class, () -> service.autenticar("agente", "Vita2026!", Perfil.INTERNO));
    }
}
