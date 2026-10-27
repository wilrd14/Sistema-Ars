package edu.uasd.vitasalud.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.uasd.vitasalud.dao.AfiliadoDAO;
import edu.uasd.vitasalud.model.Afiliado;
import edu.uasd.vitasalud.model.EstadoAfiliado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AfiliadoServiceTest {

    private DaoFalso dao;
    private AfiliadoService service;

    @BeforeEach
    void preparar() {
        dao = new DaoFalso();
        service = new AfiliadoService(dao);
    }

    // ---------- RN-01: documento y NSS únicos ----------

    @Test
    void titularConDatosNuevosSeRegistra() {
        assertDoesNotThrow(() -> service.registrarAfiliado(titular("P-1001", "NSS-1")));
    }

    @Test
    void documentoRepetidoSeRechaza() throws Exception {
        service.registrarAfiliado(titular("P-1001", "NSS-1"));

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarAfiliado(titular("P-1001", "NSS-2")));
    }

    @Test
    void nssRepetidoSeRechaza() throws Exception {
        service.registrarAfiliado(titular("P-1001", "NSS-1"));

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarAfiliado(titular("P-2002", "NSS-1")));
    }

    // ---------- RN-02: dependientes ----------

    @Test
    void hijoMenorConActaSeRegistra() {
        Afiliado hijo = dependiente("ACTA_NACIMIENTO", "ACTA-1", "NSS-10", "HIJO", 10);

        assertDoesNotThrow(() -> service.registrarAfiliado(hijo));
    }

    @Test
    void hijoMayorDe21SeRechaza() {
        Afiliado hijo = dependiente("PASAPORTE", "P-30", "NSS-11", "HIJO", 25);
        hijo.setEsEstudiante(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarAfiliado(hijo));
    }

    @Test
    void hijoDe20EstudianteSeAcepta() {
        Afiliado hijo = dependiente("PASAPORTE", "P-31", "NSS-12", "HIJO", 20);
        hijo.setEsEstudiante(true);

        assertDoesNotThrow(() -> service.registrarAfiliado(hijo));
    }

    @Test
    void hijoDe20SinEstudiarSeRechaza() {
        Afiliado hijo = dependiente("PASAPORTE", "P-32", "NSS-13", "HIJO", 20);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarAfiliado(hijo));
    }

    @Test
    void primerConyugeSeAcepta() {
        Afiliado conyuge = dependiente("PASAPORTE", "P-40", "NSS-14", "CONYUGE", 35);

        assertDoesNotThrow(() -> service.registrarAfiliado(conyuge));
    }

    @Test
    void segundoConyugeSeRechaza() {
        dao.tieneConyuge = true;
        Afiliado conyuge = dependiente("PASAPORTE", "P-41", "NSS-15", "CONYUGE", 35);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarAfiliado(conyuge));
    }

    @Test
    void menorSinActaDeNacimientoSeRechaza() {
        Afiliado hijo = dependiente("PASAPORTE", "P-50", "NSS-16", "HIJO", 8);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarAfiliado(hijo));
    }

    @Test
    void dependienteSinTitularSeRechaza() {
        Afiliado hijo = dependiente("ACTA_NACIMIENTO", "ACTA-9", "NSS-17", "HIJO", 5);
        hijo.setTitularId(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarAfiliado(hijo));
    }

    // ---------- Datos de apoyo ----------

    private Afiliado titular(String documento, String nss) {
        Afiliado a = new Afiliado();
        a.setTipoDocumento("PASAPORTE");
        a.setNumeroDocumento(documento);
        a.setNss(nss);
        a.setNombres("Persona");
        a.setApellidos("De Prueba");
        a.setFechaNacimiento(LocalDate.now().minusYears(40));
        a.setSexo('M');
        a.setPlanId(1);
        a.setTipoAfiliado("TITULAR");
        a.setInicioCobertura(LocalDate.now());
        return a;
    }

    private Afiliado dependiente(String tipoDoc, String documento, String nss,
                                 String parentesco, int edad) {
        Afiliado a = titular(documento, nss);
        a.setTipoDocumento(tipoDoc);
        a.setFechaNacimiento(LocalDate.now().minusYears(edad));
        a.setTipoAfiliado("DEPENDIENTE");
        a.setTitularId(1L);
        a.setParentesco(parentesco);
        return a;
    }

    /** DAO en memoria: permite probar las reglas sin MySQL. */
    private static class DaoFalso implements AfiliadoDAO {

        private final List<Afiliado> guardados = new ArrayList<>();
        private boolean tieneConyuge = false;

        @Override
        public Optional<Afiliado> buscarPorNumeroDocumento(String tipo, String numero) {
            return guardados.stream()
                    .filter(a -> a.getTipoDocumento().equals(tipo)
                            && a.getNumeroDocumento().equals(numero))
                    .findFirst();
        }

        @Override
        public Optional<Afiliado> buscarPorNss(String nss) {
            return guardados.stream().filter(a -> a.getNss().equals(nss)).findFirst();
        }

        @Override
        public long registrarAfiliado(Afiliado afiliado) {
            guardados.add(afiliado);
            return guardados.size();
        }

        @Override
        public boolean existeConyugePorTitular(long titularId) {
            return tieneConyuge;
        }

        @Override
        public void actualizarAfiliado(Afiliado afiliado) { }

        @Override
        public void cambiarEstado(long id, EstadoAfiliado estado) { }

        @Override
        public List<Afiliado> listarAfiliados() {
            return guardados;
        }

        @Override
        public List<Afiliado> buscarPorNombre(String nombres) {
            return guardados;
        }

        @Override
        public Optional<Afiliado> buscarPorId(long id) {
            return Optional.empty();
        }

        @Override
        public List<Afiliado> listarDependientes(long titularId) {
            return new ArrayList<>();
        }

        @Override
        public List<Afiliado> buscar(String criterio) {
            return guardados;
        }
    }
}