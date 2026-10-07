package edu.uasd.vitasalud.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto único para obtener conexiones JDBC a MySQL.
 * <p>
 * Cada llamada abre una conexión nueva; el que la pide la cierra con try-with-resources.
 * Los DAO deben usar siempre {@code PreparedStatement} (nunca concatenar SQL).
 */
public final class ConexionBD {

    private static final Logger LOG = Logger.getLogger(ConexionBD.class.getName());

    private ConexionBD() {
    }

    /**
     * @throws SQLException si el servidor no responde o las credenciales son inválidas
     */
    public static Connection getConexion() throws SQLException {
        try {
            return DriverManager.getConnection(
                    AppConfig.get("db.url"), AppConfig.get("db.user"), AppConfig.get("db.password"));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo conectar a MySQL", e);
            throw e;
        }
    }
}
