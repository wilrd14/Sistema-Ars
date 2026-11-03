package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.BitacoraDAO;
import edu.uasd.vitasalud.dao.impl.BitacoraDAOJdbc;
import edu.uasd.vitasalud.model.Bitacora;
import edu.uasd.vitasalud.model.Rol;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bitácora de auditoría (RF-SEG-07). Cada inicio de sesión, creación, cambio de estado y anulación
 * guarda usuario, perfil, fecha/hora, módulo, acción e id del registro. Solo el Administrador la consulta.
 */
public class BitacoraService {

    private static final Logger LOG = Logger.getLogger(BitacoraService.class.getName());
    private static final int LIMITE_POR_DEFECTO = 200;

    private final BitacoraDAO bitacoraDAO;
    /** Con el constructor sin argumentos se exige rol Administrador; con DAO inyectado (pruebas) no. */
    private final boolean exigirAdministrador;

    public BitacoraService() {
        this.bitacoraDAO = new BitacoraDAOJdbc();
        this.exigirAdministrador = true;
    }

    public BitacoraService(BitacoraDAO bitacoraDAO) {
        this.bitacoraDAO = bitacoraDAO;
        this.exigirAdministrador = false;
    }

    /**
     * Guarda un evento de auditoría. Nunca lanza excepción: si falla, lo registra en el log para no
     * interrumpir la operación de negocio que se está auditando.
     *
     * @param usuarioId  usuario que actúa (null si no se conoce, p. ej. intento de login fallido)
     * @param modulo     módulo del sistema (SEG, AFI, AUT...)
     * @param accion     acción realizada (LOGIN, CREAR, CAMBIAR_ESTADO, ANULAR...)
     * @param tabla      tabla afectada (puede ser null)
     * @param registroId id del registro afectado (puede ser null)
     * @param detalle    texto libre breve (puede ser null)
     */
    public void registrar(Long usuarioId, String modulo, String accion, String tabla, Long registroId,
                          String detalle) {
        try {
            Bitacora b = new Bitacora();
            b.setUsuarioId(usuarioId);
            b.setModulo(recortar(modulo, 30));
            b.setAccion(recortar(accion, 60));
            b.setTabla(recortar(tabla, 40));
            b.setRegistroId(registroId);
            b.setDetalle(recortar(detalle, 500));
            bitacoraDAO.insertar(b);
        } catch (SQLException | RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo registrar en la bitácora: " + modulo + "/" + accion, e);
        }
    }

    /**
     * Consulta la bitácora filtrando por módulo y rango de fechas (solo Administrador).
     *
     * @param modulo módulo a filtrar (null o vacío = todos)
     * @param desde  fecha inicial inclusive (null = sin límite)
     * @param hasta  fecha final inclusive (null = sin límite)
     * @param limite máximo de filas (si es menor que 1 se usa 200)
     * @throws ServicioException si el usuario no es Administrador o falla la base de datos
     */
    public List<Bitacora> consultar(String modulo, LocalDate desde, LocalDate hasta, int limite) {
        verificarPermiso();
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ServicioException("La fecha inicial no puede ser posterior a la final.");
        }
        try {
            return bitacoraDAO.buscar(modulo, desde, hasta, limite < 1 ? LIMITE_POR_DEFECTO : limite);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error consultando la bitácora", e);
            throw new ServicioException("No se pudo consultar la bitácora.", e);
        }
    }

    /** Últimos eventos registrados (solo Administrador). */
    public List<Bitacora> listarRecientes(int limite) {
        return consultar(null, null, null, limite);
    }

    private void verificarPermiso() {
        if (exigirAdministrador && (!Sesion.activa() || Sesion.getRol() != Rol.ADMINISTRADOR)) {
            throw new ServicioException("Solo el Administrador puede consultar la bitácora.");
        }
    }

    private static String recortar(String texto, int max) {
        return texto != null && texto.length() > max ? texto.substring(0, max) : texto;
    }
}
