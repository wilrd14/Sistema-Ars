package edu.uasd.vitasalud.app;

import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-SEG-03: cada rol ve solo las opciones que le corresponden. */
class MenuTest {

    private static List<String> ids(Rol rol) {
        return Menu.paraRol(rol).stream().map(Menu.Item::id).toList();
    }

    @Test
    void administradorVeTodaLaConsolaInterna() {
        assertEquals(9, Menu.paraRol(Rol.ADMINISTRADOR).size());
        assertTrue(ids(Rol.ADMINISTRADOR).contains("usuarios"));
    }

    @Test
    void soloElAdministradorGestionaUsuarios() {
        for (Rol rol : Rol.values()) {
            if (rol != Rol.ADMINISTRADOR) {
                assertFalse(ids(rol).contains("usuarios"), rol + " no debe ver Usuarios y roles");
            }
        }
    }

    @Test
    void agenteDeAfiliacionNoVeLaBandejaNiReclamaciones() {
        List<String> ids = ids(Rol.AGENTE_AFILIACION);
        assertFalse(ids.contains("bandeja"));
        assertFalse(ids.contains("reclamaciones"));
        assertTrue(ids.contains("nuevo-afiliado"));
    }

    @Test
    void prestadorSoloVeElPortalYNuncaLaConsolaInterna() {
        List<String> ids = ids(Rol.PRESTADOR);
        assertEquals(7, ids.size());
        assertTrue(ids.contains("solicitar"));
        assertFalse(ids.contains("panel"));
        assertFalse(ids.contains("afiliados"));
    }

    @Test
    void cadaRolPerteneceAUnPerfil() {
        assertEquals(Perfil.PRESTADOR, Rol.PRESTADOR.perfil());
        assertEquals(Perfil.INTERNO, Rol.ADMINISTRADOR.perfil());
    }
}
