package edu.uasd.vitasalud.service;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** El hash de 02_datos_prueba.sql debe corresponder a la contraseña documentada en el README. */
class HashPruebaTest {

    @Test
    void hashDeLosDatosDePruebaEsVita2026() throws IOException {
        String sql = Files.readString(Path.of("database", "02_datos_prueba.sql"));
        Matcher m = Pattern.compile("SET @hash = '([^']+)'").matcher(sql);
        assertTrue(m.find(), "No se encontró SET @hash en 02_datos_prueba.sql");
        assertTrue(BCrypt.checkpw("Vita2026!", m.group(1)), "El hash no corresponde a Vita2026!");
    }
}
