
package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.app.Sesion;
import edu.uasd.vitasalud.dao.PlanDAO;
import edu.uasd.vitasalud.dao.CoberturaDAO;
import edu.uasd.vitasalud.model.Plan;
import edu.uasd.vitasalud.model.Cobertura;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlanService {

    private final PlanDAO planDAO;
    private final CoberturaDAO coberturaDAO;
    private final BitacoraService bitacoraService;

    public PlanService(PlanDAO planDAO, CoberturaDAO coberturaDAO) {
        this(planDAO, coberturaDAO, new BitacoraService());
    }

    public PlanService(
            PlanDAO planDAO,
            CoberturaDAO coberturaDAO,
            BitacoraService bitacoraService) {

        this.planDAO = planDAO;
        this.coberturaDAO = coberturaDAO;
        this.bitacoraService = bitacoraService;
    }

    private void auditar(String accion, Long id, String detalle) {
        if (bitacoraService != null) {
            bitacoraService.registrar(
                    obtenerUsuarioId(),
                    "PLA",
                    accion,
                    "plan",
                    id,
                    detalle
            );
        }
    }

    public Optional<Plan> buscarPorId(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El ID del plan es obligatorio.");
        }

        return planDAO.buscarPorId(id);
    }

    public List<Plan> listarPlanes() throws SQLException {
        return planDAO.listarTodo();
    }

    public void registrar(Plan plan) throws SQLException {
        validar(plan);

        planDAO.registrar(plan);

        Long registroId = plan.getId() == null
                ? null
                : plan.getId().longValue();

        auditar(
                "CREAR",
                registroId,
                "Creación de plan"
        );
    }

    public void actualizar(Plan plan) throws SQLException {
        validar(plan);

        if (plan.getId() == null) {
            throw new IllegalArgumentException(
                    "El ID del plan es obligatorio.");
        }

        planDAO.actualizar(plan);

        auditar(
                "ACTUALIZAR",
                plan.getId().longValue(),
                "Actualización de plan"
        );
    }

    public void eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID debe ser válido.");
        }

        Optional<Plan> resultado =
                planDAO.buscarPorId(String.valueOf(id));

        if (resultado.isEmpty()) {
            throw new IllegalArgumentException(
                    "El plan no existe.");
        }

        // No permitir desactivar planes que tengan afiliados.
        if (planDAO.tieneAfiliados(id)) {
            throw new IllegalStateException(
                    "No se puede desactivar un plan que tiene afiliados.");
        }

        // No permitir desactivar planes que tengan coberturas.
        boolean tieneCoberturas = coberturaDAO.listarTodo()
                .stream()
                .anyMatch(cobertura ->
                        Integer.valueOf(id).equals(
                                cobertura.getPlan_id()
                        )
                );

        if (tieneCoberturas) {
            throw new IllegalStateException(
                    "No se puede desactivar un plan que tiene coberturas asociadas.");
        }

        // Desactivación lógica.
        planDAO.eliminar(id);

        // Registrar la operación en bitácora.
        auditar(
                "ELIMINAR",
                (long) id,
                "Desactivación de plan"
        );
    }

    public List<Cobertura> consultarCoberturaPorCategoria(
            String categoria) throws SQLException {

        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria.");
        }

        String categoriaBuscada = categoria.trim();

        return coberturaDAO.listarTodo().stream()
                .filter(c -> c.getCategoria() != null
                        && c.getCategoria().trim().equalsIgnoreCase(
                                categoriaBuscada))
                .collect(Collectors.toList());
    }

    private Long obtenerUsuarioId() {
        if (!Sesion.activa()) {
            return null;
        }

        return Sesion.getUsuario().getId();
    }

    private void validar(Plan plan) {
        if (plan == null) {
            throw new IllegalArgumentException(
                    "El plan es obligatorio.");
        }

        if (plan.getCodigo() == null
                || plan.getCodigo().isBlank()) {
            throw new IllegalArgumentException(
                    "El código es obligatorio.");
        }

        if (plan.getNombre() == null
                || plan.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio.");
        }

        if (plan.getPrima_mensual() == null
                || plan.getPrima_mensual().signum() < 0) {
            throw new IllegalArgumentException(
                    "La prima mensual no puede ser negativa.");
        }

        if (plan.getEstado() == null
                || plan.getEstado().isBlank()) {
            throw new IllegalArgumentException(
                    "El estado es obligatorio.");
        }
    }
}