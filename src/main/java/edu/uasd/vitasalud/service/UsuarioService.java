package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.UsuarioDAO;
import edu.uasd.vitasalud.dao.UsuarioDAO.PrestadorOpcion;
import edu.uasd.vitasalud.dao.impl.UsuarioDAOJdbc;
import edu.uasd.vitasalud.model.Perfil;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Autenticación y gestión de usuarios y roles (RF-SEG-01, RF-SEG-04, RF-SEG-05, RF-SEG-06). */
public class UsuarioService {

    public static final int MAX_INTENTOS = 5;
    public static final int CLAVE_MINIMA = 8;

    private static final Logger LOG = Logger.getLogger(UsuarioService.class.getName());
    /** Mismo mensaje para usuario inexistente, clave mala o perfil equivocado: no revela cuál falló. */
    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,50}");

    private final UsuarioDAO usuarioDAO;
    private final BitacoraService bitacora;
    /** Quién ejecuta la acción; por defecto, el usuario de la sesión. Se inyecta en las pruebas. */
    private final Supplier<Usuario> actor;

    public UsuarioService() {
        this(new UsuarioDAOJdbc());
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this(usuarioDAO, new BitacoraService());
    }

    public UsuarioService(UsuarioDAO usuarioDAO, BitacoraService bitacora) {
        this(usuarioDAO, bitacora, () -> Sesion.activa() ? Sesion.getUsuario() : null);
    }

    public UsuarioService(UsuarioDAO usuarioDAO, BitacoraService bitacora, Supplier<Usuario> actor) {
        this.usuarioDAO = usuarioDAO;
        this.bitacora = bitacora;
        this.actor = actor;
    }

    // ------------------------------------------------------------------ autenticación

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
                bitacora.registrar(null, "SEG", "LOGIN_FALLIDO", "usuario", null,
                        "Intento de acceso con un usuario inexistente: " + username.trim());
                throw new ServicioException(CREDENCIALES_INVALIDAS);
            }
            Usuario u = encontrado.get();

            if (Usuario.ESTADO_BLOQUEADO.equals(u.getEstado())) {
                bitacora.registrar(u.getId(), "SEG", "LOGIN_FALLIDO", "usuario", u.getId(),
                        "Intento de acceso con la cuenta bloqueada");
                throw new ServicioException("Tu cuenta está bloqueada. Solicita al administrador que la desbloquee.");
            }
            if (Usuario.ESTADO_INACTIVO.equals(u.getEstado())) {
                bitacora.registrar(u.getId(), "SEG", "LOGIN_FALLIDO", "usuario", u.getId(),
                        "Intento de acceso con la cuenta desactivada");
                throw new ServicioException("Tu cuenta está desactivada. Contacta al administrador.");
            }
            if (!BCrypt.checkpw(password, u.getPasswordHash()) || u.getPerfil() != perfil) {
                registrarFallo(u);
                throw new ServicioException(CREDENCIALES_INVALIDAS);
            }
            usuarioDAO.reiniciarIntentos(u.getId());
            bitacora.registrar(u.getId(), "SEG", "LOGIN", "usuario", u.getId(), "Inicio de sesión exitoso");
            return u;
        } catch (SQLException e) {
            throw errorBd("Error de base de datos al iniciar sesión", e);
        }
    }

    private void registrarFallo(Usuario u) throws SQLException {
        int intentos = usuarioDAO.registrarIntentoFallido(u.getId());
        bitacora.registrar(u.getId(), "SEG", "LOGIN_FALLIDO", "usuario", u.getId(),
                "Credenciales incorrectas (intento " + intentos + " de " + MAX_INTENTOS + ")");
        if (intentos >= MAX_INTENTOS) {
            usuarioDAO.cambiarEstado(u.getId(), Usuario.ESTADO_BLOQUEADO);
            bitacora.registrar(u.getId(), "SEG", "BLOQUEO_CUENTA", "usuario", u.getId(),
                    "Cuenta bloqueada tras " + MAX_INTENTOS + " intentos fallidos");
            throw new ServicioException("Superaste " + MAX_INTENTOS
                    + " intentos. Tu cuenta fue bloqueada; solicita al administrador que la desbloquee.");
        }
    }

    // ------------------------------------------------------------------ gestión de usuarios (solo Administrador)

    /** Lista usuarios; el filtro busca en nombre, usuario y cargo (null o vacío = todos). */
    public List<Usuario> listar(String filtro) {
        exigirAdministrador();
        try {
            return usuarioDAO.listar(filtro);
        } catch (SQLException e) {
            throw errorBd("Error al listar usuarios", e);
        }
    }

    /** Prestadores disponibles para asociar a un usuario de perfil Prestador. */
    public List<PrestadorOpcion> listarPrestadores() {
        exigirAdministrador();
        try {
            return usuarioDAO.listarPrestadores();
        } catch (SQLException e) {
            throw errorBd("Error al listar prestadores", e);
        }
    }

    /** Crea un usuario activo con la clave cifrada con BCrypt. Devuelve el id nuevo. */
    public long crear(String nombre, String username, String clave, Rol rol, String cargo, Long prestadorId) {
        Usuario admin = exigirAdministrador();
        validarNombre(nombre);
        if (username == null || !USERNAME.matcher(username.trim()).matches()) {
            throw new ServicioException(
                    "El usuario debe tener entre 3 y 50 caracteres: letras, números, punto, guion o guion bajo.");
        }
        validarClave(clave);
        if (rol == null) {
            throw new ServicioException("Elige el rol del usuario.");
        }
        try {
            validarPrestador(rol, prestadorId);
            if (usuarioDAO.buscarPorUsername(username.trim()).isPresent()) {
                throw new ServicioException("Ya existe un usuario con el nombre \"" + username.trim() + "\".");
            }
            Usuario u = new Usuario();
            u.setNombre(nombre.trim());
            u.setUsername(username.trim());
            u.setPasswordHash(BCrypt.hashpw(clave, BCrypt.gensalt()));
            u.setPerfil(rol.perfil());
            u.setRol(rol);
            u.setPrestadorId(rol.perfil() == Perfil.PRESTADOR ? prestadorId : null);
            u.setCargo(textoOpcional(cargo));
            u.setEstado(Usuario.ESTADO_ACTIVO);
            long id = usuarioDAO.insertar(u);
            bitacora.registrar(admin.getId(), "SEG", "CREAR_USUARIO", "usuario", id,
                    "Usuario " + u.getUsername() + " creado con el rol " + rol.etiqueta());
            return id;
        } catch (SQLException e) {
            throw errorBd("Error al crear el usuario", e);
        }
    }

    /** Edita nombre, rol, cargo y prestador. El perfil y el nombre de usuario no cambian. */
    public void editar(long id, String nombre, Rol rol, String cargo, Long prestadorId) {
        Usuario admin = exigirAdministrador();
        validarNombre(nombre);
        if (rol == null) {
            throw new ServicioException("Elige el rol del usuario.");
        }
        try {
            Usuario u = obtener(id);
            if (rol.perfil() != u.getPerfil()) {
                throw new ServicioException("El rol \"" + rol.etiqueta() + "\" no corresponde al perfil "
                        + u.getPerfil().etiqueta().toLowerCase() + " de este usuario.");
            }
            if (admin.getId() == id && rol != Rol.ADMINISTRADOR) {
                throw new ServicioException("No puedes quitarte a ti mismo el rol de Administrador.");
            }
            validarPrestador(rol, prestadorId);
            u.setNombre(nombre.trim());
            u.setRol(rol);
            u.setCargo(textoOpcional(cargo));
            u.setPrestadorId(rol.perfil() == Perfil.PRESTADOR ? prestadorId : null);
            usuarioDAO.actualizar(u);
            bitacora.registrar(admin.getId(), "SEG", "EDITAR_USUARIO", "usuario", id,
                    "Usuario " + u.getUsername() + " actualizado (rol " + rol.etiqueta() + ")");
        } catch (SQLException e) {
            throw errorBd("Error al editar el usuario", e);
        }
    }

    /** Asigna una clave nueva (RF-SEG-04). Los usuarios nunca se borran (RN-10). */
    public void restablecerClave(long id, String nuevaClave) {
        Usuario admin = exigirAdministrador();
        validarClave(nuevaClave);
        try {
            Usuario u = obtener(id);
            usuarioDAO.actualizarPassword(id, BCrypt.hashpw(nuevaClave, BCrypt.gensalt()));
            bitacora.registrar(admin.getId(), "SEG", "RESTABLECER_CLAVE", "usuario", id,
                    "Clave restablecida para " + u.getUsername());
        } catch (SQLException e) {
            throw errorBd("Error al restablecer la clave", e);
        }
    }

    /** Activa o desactiva una cuenta; nunca se elimina (RN-10). Un administrador no puede desactivarse a sí mismo. */
    public void cambiarEstado(long id, boolean activar) {
        Usuario admin = exigirAdministrador();
        try {
            Usuario u = obtener(id);
            if (!activar && admin.getId() == id) {
                throw new ServicioException("No puedes desactivar tu propia cuenta.");
            }
            if (activar && Usuario.ESTADO_BLOQUEADO.equals(u.getEstado())) {
                throw new ServicioException("La cuenta está bloqueada: usa la opción Desbloquear.");
            }
            String nuevo = activar ? Usuario.ESTADO_ACTIVO : Usuario.ESTADO_INACTIVO;
            if (nuevo.equals(u.getEstado())) {
                throw new ServicioException("La cuenta ya está " + (activar ? "activa." : "desactivada."));
            }
            usuarioDAO.cambiarEstado(id, nuevo);
            if (activar) {
                usuarioDAO.reiniciarIntentos(id);
            }
            bitacora.registrar(admin.getId(), "SEG", activar ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO", "usuario", id,
                    "Usuario " + u.getUsername() + (activar ? " activado" : " desactivado"));
        } catch (SQLException e) {
            throw errorBd("Error al cambiar el estado del usuario", e);
        }
    }

    /** Desbloquea una cuenta bloqueada por intentos fallidos y reinicia el contador (RF-SEG-06). */
    public void desbloquear(long id) {
        Usuario admin = exigirAdministrador();
        try {
            Usuario u = obtener(id);
            if (!Usuario.ESTADO_BLOQUEADO.equals(u.getEstado())) {
                throw new ServicioException("La cuenta no está bloqueada.");
            }
            usuarioDAO.cambiarEstado(id, Usuario.ESTADO_ACTIVO);
            usuarioDAO.reiniciarIntentos(id);
            bitacora.registrar(admin.getId(), "SEG", "DESBLOQUEAR_USUARIO", "usuario", id,
                    "Usuario " + u.getUsername() + " desbloqueado");
        } catch (SQLException e) {
            throw errorBd("Error al desbloquear la cuenta", e);
        }
    }

    // ------------------------------------------------------------------ utilidades

    private Usuario exigirAdministrador() {
        Usuario quien = actor.get();
        if (quien == null || quien.getRol() != Rol.ADMINISTRADOR) {
            throw new ServicioException("Solo un Administrador puede gestionar usuarios y roles.");
        }
        return quien;
    }

    private Usuario obtener(long id) throws SQLException {
        return usuarioDAO.buscarPorId(id).orElseThrow(() -> new ServicioException("El usuario ya no existe."));
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ServicioException("Escribe el nombre completo del usuario.");
        }
        if (nombre.trim().length() > 120) {
            throw new ServicioException("El nombre no puede pasar de 120 caracteres.");
        }
    }

    /** Mínimo {@value #CLAVE_MINIMA} caracteres con al menos una letra y un número. */
    private void validarClave(String clave) {
        boolean valida = clave != null && clave.length() >= CLAVE_MINIMA
                && clave.chars().anyMatch(Character::isLetter)
                && clave.chars().anyMatch(Character::isDigit);
        if (!valida) {
            throw new ServicioException("La contraseña debe tener al menos " + CLAVE_MINIMA
                    + " caracteres, con letras y números.");
        }
    }

    /** Perfil Prestador exige un prestador existente; el personal interno no lleva prestador. */
    private void validarPrestador(Rol rol, Long prestadorId) throws SQLException {
        if (rol.perfil() == Perfil.PRESTADOR) {
            if (prestadorId == null) {
                throw new ServicioException("Elige el prestador al que pertenece el usuario.");
            }
            boolean existe = usuarioDAO.listarPrestadores().stream().anyMatch(p -> p.id() == prestadorId);
            if (!existe) {
                throw new ServicioException("El prestador elegido no existe.");
            }
        } else if (prestadorId != null) {
            throw new ServicioException("El personal interno no se asocia a un prestador.");
        }
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private ServicioException errorBd(String detalle, SQLException e) {
        LOG.log(Level.SEVERE, detalle, e);
        return new ServicioException("No se pudo conectar con la base de datos. Verifica que MySQL esté activo.", e);
    }
}
