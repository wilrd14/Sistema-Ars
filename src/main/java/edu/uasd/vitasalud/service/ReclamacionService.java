package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.ReclamacionDAO;
import edu.uasd.vitasalud.dao.impl.ReclamacionDAOJdbc;
import edu.uasd.vitasalud.model.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Reglas REC: una reclamación por autorización, aislamiento, glosas y cierre de
 * pago.
 */
public class ReclamacionService {
    private final ReclamacionDAO dao;

    public ReclamacionService() {
        this(new ReclamacionDAOJdbc());
    }

    public ReclamacionService(ReclamacionDAO dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    private long prestador() {
        if (Sesion.getRol() != Rol.PRESTADOR || Sesion.getPrestadorId() == null)
            throw new ServicioException("Operación exclusiva de prestadores");
        return Sesion.getPrestadorId();
    }

    private void auditor() {
        if (Sesion.getRol() != Rol.ADMINISTRADOR && Sesion.getRol() != Rol.AUDITOR_MEDICO)
            throw new ServicioException("Solo auditor médico o administrador");
    }

    private Long filtro() {
        return Sesion.getRol() == Rol.PRESTADOR ? prestador() : null;
    }

    private void auditar(String accion, Long id, String detalle) {
        new BitacoraService().registrar(Sesion.getUsuario().getId(), "REC", accion, "reclamacion", id, detalle);
    }

    private static ServicioException error(SQLException e) {
        return new ServicioException("Error de base de datos: " + e.getMessage(), e);
    }

    public String crear(long idAut, String factura, LocalDate fecha, BigDecimal monto) {
        long pid = prestador();
        if (idAut <= 0 || factura == null || factura.isBlank() || fecha == null || monto == null || monto.signum() <= 0)
            throw new ServicioException("Indique autorización, factura, fecha y monto positivo");
        if (fecha.isAfter(LocalDate.now()))
            throw new ServicioException("La fecha de servicio no puede ser futura");
        Reclamacion r = new Reclamacion(null, null, idAut, pid, null, null, factura, fecha, monto, BigDecimal.ZERO,
                monto,
                EstadoReclamacion.RECIBIDA, null, null, null);
        try {
            String numero = dao.crear(r);
            auditar("CREAR", null, numero);
            return numero;
        } catch (SQLException e) {
            throw error(e);
        }
    }

    public List<Reclamacion> listar() {
        try {
            return dao.listar(filtro());
        } catch (SQLException e) {
            throw error(e);
        }
    }

    public Reclamacion buscar(long id) {
        try {
            return dao.buscar(id, filtro()).orElseThrow(() -> new ServicioException("Reclamación no disponible"));
        } catch (SQLException e) {
            throw error(e);
        }
    }

    public void cambiar(long id, EstadoReclamacion siguiente, String nota) {
        auditor();
        Reclamacion r = buscar(id);
        if (!transicionPermitida(r.estado(), siguiente))
            throw new ServicioException("Transición de estados inválida");
        if ((siguiente == EstadoReclamacion.DEVUELTA || siguiente == EstadoReclamacion.RECHAZADA)
                && (nota == null || nota.isBlank()))
            throw new ServicioException("Debe indicar un motivo");
        try {
            if (!dao.cambiarEstado(id, r.estado(), siguiente, nota))
                throw new ServicioException("El estado fue cambiado por otro usuario");
            auditar(siguiente.name(), id, nota);
        } catch (SQLException e) {
            throw error(e);
        }
    }

    /** Tabla de transiciones explícita (RF-REC-03), útil también para pruebas. */
    public static boolean transicionPermitida(EstadoReclamacion antes, EstadoReclamacion despues) {
        return switch (antes) {
            case RECIBIDA -> despues == EstadoReclamacion.EN_AUDITORIA;
            case EN_AUDITORIA -> despues == EstadoReclamacion.APROBADA || despues == EstadoReclamacion.DEVUELTA
                    || despues == EstadoReclamacion.RECHAZADA;
            default -> false; // GLOSADA y APROBADA pasan a PAGADA por el método pagar.
        };
    }

    public void glosar(long id, String motivo, BigDecimal monto, String observacion) {
        auditor();
        if (!Set.of("SERVICIO_NO_AUTORIZADO", "MONTO_SUPERIOR_TARIFA", "DOCUMENTACION_INCOMPLETA", "OTRO")
                .contains(motivo))
            throw new ServicioException("Motivo de glosa no permitido");
        if (monto == null || monto.signum() <= 0)
            throw new ServicioException("Monto de glosa debe ser mayor a cero");
        buscar(id); // comprueba que existe; el DAO valida de nuevo dentro de la transacción.
        try {
            dao.agregarGlosa(id, new Glosa(null, id, motivo, monto, observacion, Sesion.getUsuario().getId()));
            auditar("GLOSA", id, motivo + ": " + monto);
        } catch (SQLException e) {
            throw error(e);
        }
    }

    public List<Glosa> glosas(long id) {
        buscar(id);
        try {
            return dao.glosas(id);
        } catch (SQLException e) {
            throw error(e);
        }
    }

    /**
     * Una DEVUELTA puede corregirse y regresar a RECIBIDA. Se mantiene el mismo
     * número REC.
     */
    public void corregir(long id, String factura, LocalDate fecha, BigDecimal monto) {
        long pid = prestador();
        if (factura == null || factura.isBlank() || fecha == null || fecha.isAfter(LocalDate.now()) || monto == null
                || monto.signum() <= 0)
            throw new ServicioException("Factura, fecha y monto válidos son obligatorios");
        try {
            if (!dao.corregirDevuelta(id, pid, factura, fecha, monto))
                throw new ServicioException("Reclamación no devuelta o monto superior a lo autorizado");
            auditar("CORREGIR", id, "Reclamación reenviada");
        } catch (SQLException e) {
            throw error(e);
        }
    }

    public void pagar(long id, LocalDate fecha, String referencia) {
        auditor();
        if (fecha == null || referencia == null || referencia.isBlank())
            throw new ServicioException("Fecha y referencia obligatorias");
        if (fecha.isAfter(LocalDate.now()))
            throw new ServicioException("Fecha de pago no puede ser futura");
        try {
            if (!dao.pagar(id, fecha, referencia))
                throw new ServicioException("Solo se paga una reclamada APROBADA o GLOSADA no pagada");
            auditar("PAGAR", id, referencia);
        } catch (SQLException e) {
            throw error(e);
        }
    }
}
