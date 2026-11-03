package edu.uasd.vitasalud.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración de la aplicación leída de {@code db.properties} (RF-SEG-10).
 * <p>
 * El archivo vive fuera del código fuente y del repositorio: se busca en el directorio
 * de trabajo (o en la ruta indicada con {@code -Dvitasalud.config=...}). Cada integrante
 * copia {@code db.properties.example} a {@code db.properties} y ajusta sus datos.
 */
public final class AppConfig {

    private static final String RUTA_POR_DEFECTO = "db.properties";
    private static Properties propiedades;

    private AppConfig() {
    }

    public static synchronized String get(String clave) {
        cargar();
        String valor = propiedades.getProperty(clave);
        if (valor == null) {
            throw new IllegalStateException("Falta la propiedad '" + clave + "' en db.properties");
        }
        return valor;
    }

    /** Como {@link #get(String)}, pero devuelve {@code porDefecto} si la propiedad no existe. */
    public static synchronized String get(String clave, String porDefecto) {
        cargar();
        return propiedades.getProperty(clave, porDefecto);
    }

    private static void cargar() {
        if (propiedades != null) {
            return;
        }
        Path ruta = Path.of(System.getProperty("vitasalud.config", RUTA_POR_DEFECTO));
        if (!Files.exists(ruta)) {
            throw new IllegalStateException("No se encontró " + ruta.toAbsolutePath()
                    + ". Copia db.properties.example a db.properties y ajusta la conexión a MySQL.");
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(ruta)) {
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ruta.toAbsolutePath(), e);
        }
        propiedades = p;
    }
}
