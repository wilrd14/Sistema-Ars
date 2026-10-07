package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.UsuarioDAO;
import edu.uasd.vitasalud.dao.impl.UsuarioDAOJdbc;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Autenticación y gestión de usuarios (RF-SEG-01, RF-SEG-04, RF-SEG-06). */
public class UsuarioService {

    public static final int MAX_INTENTOS = 5;

    private static final Logger LOG = Logger.getLogger(UsuarioService.class.getName());
    /** Mismo mensaje para usuario inexistente, clave mala o perfil equivocado: no revela cuál falló. */
    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this(new UsuarioDAOJdbc());
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Valida credenciales para el perfil elegido en el login.
     *
     * @return el usuario autenticado
     * @throws ServicioException con un mensaje listo para mostrar al usuario
     */
    public Usuario autenticar(String username, String password, Perfil perfil) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ServicioException("Escribe tu usuario y tu contraseña.");
        }
        try {
            Optional<Usuario> encontrado = usuarioDAO.buscarPorUsername(username.trim());
            if (encontrado.isEmpty()) {
                throw new ServicioException(CREDENCIALES_INVALIDAS);
            }
            Usuario u = encontrado.get();

            if (Usuario.ESTADO_BLOQUEADO.equals(u.getEstado())) {
                throw new ServicioException("Tu cuenta está bloqueada. Solicita al administrador que la desbloquee.");
            }
            if (Usuario.ESTADO_INACTIVO.equals(u.getEstado())) {
                throw new ServicioException("Tu cuenta está desactivada. Contacta al administrador.");
            }
            if (!BCrypt.checkpw(password, u.getPasswordHash()) || u.getPerfil() != perfil) {
                registrarFallo(u);
                throw new ServicioException(CREDENCIALES_INVALIDAS);
            }
            usuarioDAO.reiniciarIntentos(u.getId());
            return u;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al iniciar sesión", e);
            throw new ServicioException("No se pudo conectar con la base de datos. Verifica que MySQL esté activo.", e);
        }
    }

    private void registrarFallo(Usuario u) throws SQLException {
        int intentos = usuarioDAO.registrarIntentoFallido(u.getId());
        if (intentos >= MAX_INTENTOS) {
            usuarioDAO.cambiarEstado(u.getId(), Usuario.ESTADO_BLOQUEADO);
            throw new ServicioException("Superaste " + MAX_INTENTOS
                    + " intentos. Tu cuenta fue bloqueada; solicita al administrador que la desbloquee.");
        }
    }
}
