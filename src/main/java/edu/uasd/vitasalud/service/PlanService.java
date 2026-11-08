
package edu.uasd.vitasalud.service;

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
    /** Opcional (RF-SEG-07): null en pruebas que no quieren auditoría. */
    private final BitacoraService bitacora;

    public PlanService(PlanDAO planDAO, CoberturaDAO coberturaDAO) {
        this(planDAO, coberturaDAO, null);
    }

    public PlanService(PlanDAO planDAO, CoberturaDAO coberturaDAO, BitacoraService bitacora) {
        this.planDAO = planDAO;
        this.coberturaDAO = coberturaDAO;
        this.bitacora = bitacora;
    }

    private void auditar(String accion, Long id, String detalle) {
        if (bitacora != null) {
            bitacora.registrarDeSesion("PLA", accion, "plan", id, detalle);
        }
    }

    public Optional<Plan> buscarPorId(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del plan es obligatorio.");
        }
        return planDAO.buscarPorId(id);
    }

    public List<Plan> listarPlanes() throws SQLException {
        return planDAO.listarTodo();
    }

    public void registrar(Plan plan) throws SQLException {
        validar(plan);
        planDAO.registrar(plan);
        auditar("CREAR", plan.getId() == null ? null : plan.getId().longValue(), "Plan registrado");
    }

    public void actualizar(Plan plan) throws SQLException {
        validar(plan);
        if (plan.getId() == null) {
            throw new IllegalArgumentException("El ID del plan es obligatorio.");
        }
        planDAO.actualizar(plan);
        auditar("ACTUALIZAR", plan.getId().longValue(), "Plan actualizado");
    }

    public void eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido.");
        }

        boolean tieneCoberturas = coberturaDAO.listarTodo().stream()
                .anyMatch(c -> c.getPlan_id() != null && c.getPlan_id() == id);

        if (tieneCoberturas) {
            throw new IllegalStateException(
                    "No se puede eliminar el plan porque tiene coberturas asociadas.");
        }

        planDAO.eliminar(id);
        auditar("ELIMINAR", (long) id, "Plan eliminado");
    }

    public List<Cobertura> consultarCoberturaPorCategoria(String categoria)
            throws SQLException {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }

        return coberturaDAO.listarTodo().stream()
                .filter(c -> c.getCategoria() != null
                        && c.getCategoria().equalsIgnoreCase(categoria.trim()))
                .collect(Collectors.toList());
    }

    private void validar(Plan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("El plan es obligatorio.");
        }
        if (plan.getCodigo() == null || plan.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        if (plan.getNombre() == null || plan.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (plan.getPrima_mensual() == null
                || plan.getPrima_mensual().signum() < 0) {
            throw new IllegalArgumentException(
                    "La prima mensual no puede ser negativa.");
        }
        if (plan.getEstado() == null || plan.getEstado().isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
    }
}
