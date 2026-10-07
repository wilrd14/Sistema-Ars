package edu.uasd.vitasalud.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTest {

    @Test
    void cedulaConFormatoValido() {
        assertTrue(Validador.esCedula("001-1234567-8"));
        assertFalse(Validador.esCedula("00112345678"));
        assertFalse(Validador.esCedula(null));
    }

    @Test
    void rncDeNueveDigitos() {
        assertTrue(Validador.esRnc("101000011"));
        assertFalse(Validador.esRnc("10100001"));
    }

    @Test
    void telefonoConFormato() {
        assertTrue(Validador.esTelefono("(809) 555-0101"));
        assertFalse(Validador.esTelefono("809-555-0101"));
    }

    @Test
    void correoBasico() {
        assertTrue(Validador.esCorreo("a@b.com"));
        assertFalse(Validador.esCorreo("a@b"));
    }
}
