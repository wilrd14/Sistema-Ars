package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato REC: cada operación escritora valida y cambia datos atómicamente.
 */
public interface ReclamacionDAO {
        String crear(Reclamacion registro) throws SQLException;

        List<Reclamacion> listar(Long prestadorId) throws SQLException;

        Optional<Reclamacion> buscar(long id, Long prestadorId) throws SQLException;

        boolean cambiarEstado(long id, EstadoReclamacion actual, EstadoReclamacion destino, String nota)
                        throws SQLException;

        /** Inserta glosas y recalcula montos dentro de la misma transacción (RN-11). */
        void agregarGlosa(long id, Glosa glosa) throws SQLException;

        List<Glosa> glosas(long reclamacionId) throws SQLException;

        boolean pagar(long id, LocalDate fecha, String referencia) throws SQLException;

        boolean corregirDevuelta(long id, long prestadorId, String factura, LocalDate fecha, java.math.BigDecimal monto)
                        throws SQLException;
}
