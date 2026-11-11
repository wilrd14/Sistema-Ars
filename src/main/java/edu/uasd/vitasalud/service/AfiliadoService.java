package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.AfiliadoDAO;
import edu.uasd.vitasalud.dao.impl.AfiliadoDAOJdbc;
import edu.uasd.vitasalud.model.Afiliado;
import edu.uasd.vitasalud.util.Validador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

public class AfiliadoService {

    private final AfiliadoDAO afiliadoDAO;
    /** Opcional (RF-SEG-07): null en pruebas que no quieren auditoría. */
    private final BitacoraService bitacora;

    public AfiliadoService() {
        this(new AfiliadoDAOJdbc(), new BitacoraService());
    }

    // Permite probar las reglas con un DAO falso, sin MySQL.
    public AfiliadoService(AfiliadoDAO afiliadoDAO) {
        this(afiliadoDAO, null);
    }

    public AfiliadoService(AfiliadoDAO afiliadoDAO, BitacoraService bitacora) {
        this.afiliadoDAO = afiliadoDAO;
        this.bitacora = bitacora;
    }

    public long registrarAfiliado(Afiliado afiliado) throws SQLException {

        // Datos obligatorios y formatos (RF-AFI-01 a 03).
        validarDatos(afiliado);

        // RN-02: validar las condiciones del dependiente.
        validarDependiente(afiliado);

        // RN-01: verificar que el documento no esté registrado.
        if (afiliadoDAO.buscarPorNumeroDocumento(
                afiliado.getTipoDocumento(),
                afiliado.getNumeroDocumento()).isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un afiliado con ese tipo y número de documento.");
        }

        // RN-01: verificar que el NSS no esté registrado.
        if (afiliadoDAO.buscarPorNss(afiliado.getNss()).isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un afiliado con ese NSS.");
        }

        long id = afiliadoDAO.registrarAfiliado(afiliado);
        if (bitacora != null) {
            bitacora.registrarDeSesion("AFI", "CREAR", "afiliado", id, "Afiliado registrado");
        }
        return id;
    }

    // Campos obligatorios y formatos de los datos del afiliado.
    private void validarDatos(Afiliado a) {

        if (vacio(a.getTipoDocumento())) {
            throw new IllegalArgumentException("Selecciona el tipo de documento.");
        }
        if (vacio(a.getNumeroDocumento())) {
            throw new IllegalArgumentException("El número de documento es obligatorio.");
        }
        if ("CEDULA".equalsIgnoreCase(a.getTipoDocumento())
                && !Validador.esCedula(a.getNumeroDocumento())) {
            throw new IllegalArgumentException(
                    "La cédula debe tener el formato 000-0000000-0.");
        }
        if (vacio(a.getNss())) {
            throw new IllegalArgumentException("El NSS es obligatorio.");
        }
        if (vacio(a.getNombres())) {
            throw new IllegalArgumentException("Los nombres son obligatorios.");
        }
        if (vacio(a.getApellidos())) {
            throw new IllegalArgumentException("Los apellidos son obligatorios.");
        }
        if (a.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (a.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        if (a.getSexo() != 'M' && a.getSexo() != 'F') {
            throw new IllegalArgumentException("Selecciona el sexo.");
        }
        if (!vacio(a.getTelefono()) && !Validador.esTelefono(a.getTelefono())) {
            throw new IllegalArgumentException(
                    "El teléfono debe tener el formato (000) 000-0000.");
        }
        if (!vacio(a.getCorreo()) && !Validador.esCorreo(a.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido.");
        }
        if (a.getPlanId() <= 0) {
            throw new IllegalArgumentException("Selecciona un plan.");
        }
        if (!"TITULAR".equalsIgnoreCase(a.getTipoAfiliado())
                && !"DEPENDIENTE".equalsIgnoreCase(a.getTipoAfiliado())) {
            throw new IllegalArgumentException("Selecciona el tipo de afiliado.");
        }
        if (a.getInicioCobertura() == null) {
            throw new IllegalArgumentException("El inicio de cobertura es obligatorio.");
        }
        if (!vacio(a.getEmpleadorRnc()) && !Validador.esRnc(a.getEmpleadorRnc())) {
            throw new IllegalArgumentException("El RNC del empleador debe tener 9 dígitos.");
        }
    }

    // RN-02: validar las condiciones de los dependientes.
    private void validarDependiente(Afiliado afiliado) throws SQLException {

        // Estas reglas solo aplican a los dependientes.
        if (!"DEPENDIENTE".equalsIgnoreCase(afiliado.getTipoAfiliado())) {
            return;
        }

        if (afiliado.getTitularId() == null) {
            throw new IllegalArgumentException(
                    "El dependiente debe estar asociado a un titular.");
        }

        // Un solo cónyuge por titular.
        if ("CONYUGE".equalsIgnoreCase(afiliado.getParentesco())
                && afiliadoDAO.existeConyugePorTitular(afiliado.getTitularId())) {

            throw new IllegalArgumentException(
                    "Este titular ya tiene un cónyuge registrado.");
        }

        int edad = Period.between(
                afiliado.getFechaNacimiento(), LocalDate.now()).getYears();

        // Hijos: hasta 18 años, o hasta 21 si son estudiantes.
        if ("HIJO".equalsIgnoreCase(afiliado.getParentesco())) {
            if (edad > 18 && !(edad <= 21 && afiliado.isEsEstudiante())) {
                throw new IllegalArgumentException(
                        "Los hijos dependientes deben tener hasta 18 años, "
                        + "o hasta 21 si son estudiantes.");
            }
        }

        // Los menores sin cédula deben registrarse con acta de nacimiento.
        if (edad < 18
                && !"ACTA_NACIMIENTO".equalsIgnoreCase(afiliado.getTipoDocumento())) {

            throw new IllegalArgumentException(
                    "Los menores sin cédula deben registrarse con acta de nacimiento.");
        }
    }

    // Busca un titular por cédula, para registrar a sus dependientes.
    public Afiliado buscarTitularPorCedula(String cedula) throws SQLException {

        if (vacio(cedula)) {
            throw new IllegalArgumentException("Escribe la cédula del titular.");
        }

        Afiliado titular = afiliadoDAO
                .buscarPorNumeroDocumento("CEDULA", cedula.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un afiliado con esa cédula."));

        if (!"TITULAR".equalsIgnoreCase(titular.getTipoAfiliado())) {
            throw new IllegalArgumentException("Esa persona no es titular.");
        }

        return titular;
    }

    // Busca por documento, NSS, número de afiliado o nombre.
    public List<Afiliado> buscar(String criterio) throws SQLException {
        return afiliadoDAO.buscar(criterio);
    }

    public Optional<Afiliado> buscarPorId(long id) throws SQLException {
        return afiliadoDAO.buscarPorId(id);
    }

    public List<Afiliado> listarDependientes(long titularId) throws SQLException {
        return afiliadoDAO.listarDependientes(titularId);
    }

    public List<Afiliado> listarAfiliados() throws SQLException {
        return afiliadoDAO.listarAfiliados();
    }

    public List<Afiliado> buscarPorNombre(String criterio) throws SQLException {
        return afiliadoDAO.buscarPorNombre(criterio);
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}