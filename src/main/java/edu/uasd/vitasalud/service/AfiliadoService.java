
package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.AfiliadoDAO;
import edu.uasd.vitasalud.dao.impl.AfiliadoDAOJdbc;
import edu.uasd.vitasalud.model.Afiliado;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

public class AfiliadoService {

    private final AfiliadoDAO afiliadoDAO;

    public AfiliadoService() {
        this.afiliadoDAO = new AfiliadoDAOJdbc();
    }

    public void registrarAfiliado(Afiliado afiliado) throws SQLException {

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

        // Registrar el afiliado después de validar.
        afiliadoDAO.registrarAfiliado(afiliado);
    }

    // RN-02: validar las condiciones de los dependientes.
    private void validarDependiente(Afiliado afiliado) throws SQLException {

        // Estas reglas solo aplican a los dependientes.
        if (!"DEPENDIENTE".equalsIgnoreCase(
                afiliado.getTipoAfiliado())) {
            return;
        }

        // Verificar que tenga un titular asociado.
        if (afiliado.getTitularId() == null) {
            throw new IllegalArgumentException(
                    "El dependiente debe estar asociado a un titular.");
        }

        // Permitir un solo cónyuge por titular.
        if ("CONYUGE".equalsIgnoreCase(afiliado.getParentesco())
                && afiliadoDAO.existeConyugePorTitular(
                        afiliado.getTitularId())) {

            throw new IllegalArgumentException(
                    "Este titular ya tiene un cónyuge registrado.");
        }

        // Calcular la edad del dependiente.
        int edad = Period.between(
                afiliado.getFechaNacimiento(),
                LocalDate.now()).getYears();

        // Los hijos pueden ser dependientes hasta los 18 años,
        // o hasta los 21 si son estudiantes.
        if ("HIJO".equalsIgnoreCase(afiliado.getParentesco())) {

            if (edad > 18
                    && !(edad <= 21 && afiliado.isEsEstudiante())) {

                throw new IllegalArgumentException(
                        "Los hijos dependientes deben tener hasta 18 años, "
                        + "o hasta 21 si son estudiantes.");
            }
        }

       
        // Los menores sin cédula deben registrarse con acta de nacimiento.
        if (edad < 18
                && !"ACTA_NACIMIENTO".equalsIgnoreCase(
                        afiliado.getTipoDocumento())) {

            throw new IllegalArgumentException(
                    "Los menores sin cédula deben registrarse "
                    + "con acta de nacimiento.");
        }

    }
}
