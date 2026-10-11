package edu.uasd.vitasalud.service;


import edu.uasd.vitasalud.dao.PrestadorDAO;
import edu.uasd.vitasalud.dao.impl.PrestadorDAOJdbc;
import edu.uasd.vitasalud.model.Prestador;
import edu.uasd.vitasalud.util.Validador;

import java.sql.SQLException;
import java.util.List;

public class PrestadorService {

    private final PrestadorDAO prestadorDAO;

    public PrestadorService() {

        this.prestadorDAO = new PrestadorDAOJdbc();

    }

    public long registrarPrestador(Prestador prestador)throws SQLException{
        // Verificar que el RNC o la cédula no estén registrados.
        if (prestadorDAO.buscarPorRncCedula(
                prestador.getRncCedula()).isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe un prestador con ese RNC o cédula.");
    }

    
        // Validar el formato del RNC o de la cédula.
        String documento = prestador.getRncCedula();

        if (!Validador.esRnc(documento)
                && !Validador.esCedula(documento)) {

            throw new IllegalArgumentException(
                    "El documento debe tener formato de RNC "
                    + "de 9 dígitos o cédula 000-0000000-0.");
        }

        // Validar el formato del teléfono si fue proporcionado.
        if (prestador.getTelefono() != null
                && !prestador.getTelefono().isBlank()
                && !Validador.esTelefono(prestador.getTelefono())) {

            throw new IllegalArgumentException(
                    "El teléfono debe tener el formato (000) 000-0000.");
        }

        // Validar el correo si fue proporcionado.
        if (prestador.getCorreo() != null
                && !prestador.getCorreo().isBlank()
                && !Validador.esCorreo(prestador.getCorreo())) {

            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido.");
        }


    // Validar que el tipo de prestador sea uno de los permitidos.
        if (!esTipoValido(prestador.getTipo())) {
            throw new IllegalArgumentException(
                    "El tipo de prestador no es válido.");
        }

        // Asignar el estado inicial si no se especificó.
        if (prestador.getEstadoContrato() == null
                || prestador.getEstadoContrato().isBlank()) {
            prestador.setEstadoContrato("ACTIVO");
        }

        validarEstado(prestador.getEstadoContrato());

        return prestadorDAO.registrarPrestador(prestador);
    }

    public void actualizarPrestador(Prestador prestador)
            throws SQLException {

        if (prestadorDAO.buscarPorRncCedula(
                prestador.getRncCedula()).isPresent()) {

            // La comprobación de duplicados debe distinguir
            // el propio prestador de otro registro.
            Prestador existente = prestadorDAO
                    .buscarPorRncCedula(prestador.getRncCedula())
                    .get();

            if (!existente.getId().equals(prestador.getId())) {
                throw new IllegalArgumentException(
                        "Ese RNC o cédula pertenece a otro prestador.");
            }
        }

        if (!esTipoValido(prestador.getTipo())) {
            throw new IllegalArgumentException(
                    "El tipo de prestador no es válido.");
        }

        prestadorDAO.actualizarPrestador(prestador);
    }

    public void cambiarEstadoContrato(Long id, String estado)
            throws SQLException {

        validarEstado(estado);
        prestadorDAO.cambiarEstadoContrato(id, estado);
    }

    public List<Prestador> listarPrestadores() throws SQLException {
        return prestadorDAO.listarPrestadores();
    }

    public List<Prestador> buscarPorNombre(String nombre)
            throws SQLException {
        return prestadorDAO.buscarPorNombre(nombre);
    }

    private boolean esTipoValido(String tipo) {

        if (tipo == null) {
            return false;
        }

        return tipo.equals("CLINICA")
                || tipo.equals("HOSPITAL")
                || tipo.equals("LABORATORIO")
                || tipo.equals("CENTRO_DIAGNOSTICO")
                || tipo.equals("MEDICO_INDEPENDIENTE")
                || tipo.equals("FARMACIA");
    }

    private void validarEstado(String estado) {

        if (estado == null
                || !(estado.equals("ACTIVO")
                || estado.equals("SUSPENDIDO")
                || estado.equals("TERMINADO"))) {

            throw new IllegalArgumentException(
                    "El estado del contrato no es válido.");
        }
    }
}
