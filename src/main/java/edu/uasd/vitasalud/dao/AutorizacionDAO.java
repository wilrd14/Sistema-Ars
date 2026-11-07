package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia AUT. No presenta ventanas ni calcula reglas
 * clínicas.
 */
public interface AutorizacionDAO {
    /** Inserta cabecera + líneas en UNA transacción; devuelve número AUT-######. */
    String crear(Autorizacion a, List<DetalleAutorizacion> detalles) throws SQLException;

    /** prestadorId != null: el filtro está en SQL (RN-07). */
    List<Autorizacion> listar(Long prestadorId) throws SQLException;

    Optional<Autorizacion> buscar(long id, Long prestadorId) throws SQLException;

    List<DetalleAutorizacion> detalles(long id, Long prestadorId) throws SQLException;

    List<AfiliadoBusqueda> buscarAfiliados(String texto) throws SQLException;

    List<ProcedimientoBusqueda> procedimientos() throws SQLException;

    /**
     * La función evalúa reglas dentro de una transacción y un bloqueo por afiliado.
     */
    void aprobar(long id, long usuario, Evaluador evaluador) throws SQLException;

    /** Pasa a EN_REVISION / NEGADA / ANULADA, usando transición atómica. */
    boolean cambiarEstado(long id, long usuario, EstadoAutorizacion destino, String nota) throws SQLException;

    int vencerSinReclamacion() throws SQLException;

    void registrarIngreso(long autorizacionId, long prestadorId, LocalDateTime ingreso, String observacion)
            throws SQLException;

    void registrarEgreso(long autorizacionId, long prestadorId, LocalDateTime egreso) throws SQLException;

    List<Hospitalizacion> hospitalizaciones(long prestadorId) throws SQLException;

    /**
     * Datos para calcular RN-04..RN-06. Se consultan dentro de la transacción de
     * aprobación.
     */
    record FilaCobertura(long detalleId, String categoria, int cantidad, BigDecimal solicitado,
            BigDecimal tarifa, BigDecimal porcentaje, BigDecimal copagoFijo, BigDecimal topeAnual,
            int diasCarencia, BigDecimal consumidoAnual) {
    }

    record Contexto(String estadoAfiliado, String estadoPrestador,
            java.time.LocalDate inicioCobertura, List<FilaCobertura> filas) {
    }

    record LineaCalculada(long detalleId, BigDecimal cubierto) {
    }

    record Resultado(BigDecimal cubiertoTotal, BigDecimal copagoTotal, List<LineaCalculada> lineas) {
    }

    @FunctionalInterface
    interface Evaluador {
        Resultado calcular(Contexto contexto);
    }
}
