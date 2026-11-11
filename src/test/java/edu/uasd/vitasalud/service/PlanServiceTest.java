
package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.PlanDAO;
import edu.uasd.vitasalud.dao.CoberturaDAO;
import edu.uasd.vitasalud.model.Plan;
import edu.uasd.vitasalud.model.Cobertura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlanServiceTest {

    private PlanDAOFalso planDAO;
    private CoberturaDAOFalso coberturaDAO;
    private PlanService planService;

    @BeforeEach
    void configurar() {
        planDAO = new PlanDAOFalso();
        coberturaDAO = new CoberturaDAOFalso();

        planService = new PlanService(planDAO, coberturaDAO);

        planDAO.planes.add(
            crearPlan(1, "PLAN-001", "Plan Familiar", "ACTIVO")
        );

        planDAO.planes.add(
            crearPlan(2, "PLAN-002", "Plan Individual", "ACTIVO")
        );

        coberturaDAO.coberturas.add(
            crearCobertura(1L, 1, "CONSULTA")
        );

        coberturaDAO.coberturas.add(
            crearCobertura(2L, 1, "CONSULTA")
        );

        coberturaDAO.coberturas.add(
            crearCobertura(3L, 2, "HOSPITALIZACION")
        );

        coberturaDAO.coberturas.add(
            crearCobertura(4L, 2, null)
        );
    }

    @Test
    void noDebeDesactivarPlanQueTieneAfiliados()
            throws SQLException {

        planDAO.tieneAfiliados = true;

        IllegalStateException excepcion = assertThrows(
            IllegalStateException.class,
            () -> planService.eliminar(1)
        );

        assertEquals(
            "No se puede desactivar un plan que tiene afiliados.",
            excepcion.getMessage()
        );

        assertEquals(0, planDAO.vecesEliminado);

        assertEquals(
            "ACTIVO",
            planDAO.planes.get(0).getEstado()
        );
    }

    @Test
    void noDebeDesactivarPlanQueTieneCoberturas()
            throws SQLException {

        planDAO.tieneAfiliados = false;

        IllegalStateException excepcion = assertThrows(
            IllegalStateException.class,
            () -> planService.eliminar(1)
        );

        assertEquals(
            "No se puede desactivar un plan que tiene coberturas asociadas.",
            excepcion.getMessage()
        );

        assertEquals(0, planDAO.vecesEliminado);

        assertEquals(
            "ACTIVO",
            planDAO.planes.get(0).getEstado()
        );
    }

    @Test
    void debeDesactivarPlanCuandoNoTieneAfiliadosNiCoberturas()
            throws SQLException {

        planDAO.tieneAfiliados = false;

        coberturaDAO.coberturas.removeIf(
            cobertura ->
                Integer.valueOf(1).equals(
                    cobertura.getPlan_id()
                )
        );

        assertDoesNotThrow(
            () -> planService.eliminar(1)
        );

        assertEquals(1, planDAO.vecesEliminado);

        assertEquals(
            "INACTIVO",
            planDAO.planes.get(0).getEstado()
        );
    }

    @Test
    void noDebeDesactivarPlanInexistente() {

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.eliminar(99)
        );

        assertEquals(0, planDAO.vecesEliminado);
    }

    @Test
    void debeRechazarIdDePlanInvalido() {

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.eliminar(0)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.eliminar(-1)
        );
    }

    @Test
    void debeBuscarPlanPorId() throws SQLException {

        Optional<Plan> resultado =
            planService.buscarPorId("1");

        assertTrue(resultado.isPresent());

        assertEquals(
            "PLAN-001",
            resultado.get().getCodigo()
        );
    }

    @Test
    void debeRechazarIdVacio() {

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.buscarPorId("")
        );
    }

    @Test
    void debeRechazarIdNulo() {

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.buscarPorId(null)
        );
    }

    @Test
    void debeListarPlanes() throws SQLException {

        List<Plan> resultado =
            planService.listarPlanes();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void debeConsultarCoberturasPorCategoria()
            throws SQLException {

        List<Cobertura> resultado =
            planService.consultarCoberturaPorCategoria(
                "CONSULTA"
            );

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        for (Cobertura cobertura : resultado) {
            assertEquals(
                "CONSULTA",
                cobertura.getCategoria()
            );
        }
    }

    @Test
    void debeBuscarCategoriaSinDistinguirMayusculas()
            throws SQLException {

        List<Cobertura> resultado =
            planService.consultarCoberturaPorCategoria(
                "consulta"
            );

        assertEquals(2, resultado.size());
    }

    @Test
    void debeAceptarEspaciosAlrededorDeLaCategoria()
            throws SQLException {

        List<Cobertura> resultado =
            planService.consultarCoberturaPorCategoria(
                "  CONSULTA  "
            );

        assertEquals(2, resultado.size());
    }

    @Test
    void noDebeIncluirCoberturasDeOtraCategoria()
            throws SQLException {

        List<Cobertura> resultado =
            planService.consultarCoberturaPorCategoria(
                "CONSULTA"
            );

        for (Cobertura cobertura : resultado) {
            assertNotEquals(
                "HOSPITALIZACION",
                cobertura.getCategoria()
            );
        }
    }

    @Test
    void debeRetornarListaVaciaCuandoNoHayCoincidencias()
            throws SQLException {

        List<Cobertura> resultado =
            planService.consultarCoberturaPorCategoria(
                "ODONTOLOGIA"
            );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRechazarCategoriaNula() {

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.consultarCoberturaPorCategoria(
                null
            )
        );
    }

    @Test
    void debeRechazarCategoriaVacia() {

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.consultarCoberturaPorCategoria(
                ""
            )
        );
    }

    @Test
    void debeRechazarCategoriaConEspacios() {

        assertThrows(
            IllegalArgumentException.class,
            () -> planService.consultarCoberturaPorCategoria(
                "   "
            )
        );
    }

    private Plan crearPlan(
            Integer id,
            String codigo,
            String nombre,
            String estado) {

        return new Plan(
            id,
            codigo,
            nombre,
            "Descripción de prueba",
            new BigDecimal("1500.00"),
            estado
        );
    }

    private Cobertura crearCobertura(
            Long id,
            Integer planId,
            String categoria) {

        return new Cobertura(
            id,
            planId,
            categoria,
            new BigDecimal("80.00"),
            new BigDecimal("100.00"),
            new BigDecimal("50000.00"),
            30,
            false
        );
    }

    private static class PlanDAOFalso implements PlanDAO {

        private final List<Plan> planes =
            new ArrayList<>();

        private boolean tieneAfiliados = false;
        private int vecesEliminado = 0;

        @Override
        public Optional<Plan> buscarPorId(String id) {

            return planes.stream()
                .filter(plan ->
                    plan.getId() != null
                    && plan.getId().toString().equals(id)
                )
                .findFirst();
        }

        @Override
        public List<Plan> listarTodo() {
            return new ArrayList<>(planes);
        }

        @Override
        public void registrar(Plan plan) {
            planes.add(plan);
        }

        @Override
        public void actualizar(Plan plan) {
            for (int i = 0; i < planes.size(); i++) {
                if (planes.get(i).getId()
                        .equals(plan.getId())) {
                    planes.set(i, plan);
                    return;
                }
            }
        }

        @Override
        public boolean tieneAfiliados(int id) {
            return tieneAfiliados;
        }

        @Override
        public void eliminar(int id) {
            vecesEliminado++;

            buscarPorId(String.valueOf(id))
                .ifPresent(plan ->
                    plan.setEstado("INACTIVO")
                );
        }
    }

    private static class CoberturaDAOFalso
            implements CoberturaDAO {

        private final List<Cobertura> coberturas =
            new ArrayList<>();

        @Override
        public Optional<Cobertura> buscarPorId(Long id) {

            return coberturas.stream()
                .filter(cobertura ->
                    cobertura.getId() != null
                    && cobertura.getId().equals(id)
                )
                .findFirst();
        }

        @Override
        public List<Cobertura> listarTodo() {
            return new ArrayList<>(coberturas);
        }

        @Override
        public void registrar(Cobertura cobertura) {
            coberturas.add(cobertura);
        }

        @Override
        public void actualizar(Cobertura cobertura) {
            for (int i = 0; i < coberturas.size(); i++) {
                if (coberturas.get(i).getId()
                        .equals(cobertura.getId())) {
                    coberturas.set(i, cobertura);
                    return;
                }
            }
        }

        @Override
        public void eliminar(Long id) {
            coberturas.removeIf(cobertura ->
                cobertura.getId() != null
                && cobertura.getId().equals(id)
            );
        }
    }
}
