package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.PrestadorDAO;
import edu.uasd.vitasalud.dao.impl.PrestadorDAOJdbc;
import edu.uasd.vitasalud.model.Prestador;
import edu.uasd.vitasalud.util.Validador;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrestadorService {

    private final PrestadorDAO prestadorDAO;
    /** Opcional (RF-SEG-07): null en pruebas que no quieren auditoría. */
    private final BitacoraService bitacora;

    public PrestadorService() {
        this(new PrestadorDAOJdbc(), new BitacoraService());
    }

    // Permite probar las reglas con un DAO falso, sin MySQL.
    public PrestadorService(PrestadorDAO prestadorDAO) {
        this(prestadorDAO, null);
    }

    public PrestadorService(PrestadorDAO prestadorDAO, BitacoraService bitacora) {
        this.prestadorDAO = prestadorDAO;
        this.bitacora = bitacora;
    }

    private void auditar(String accion, Long id, String detalle) {
        if (bitacora != null) {
            bitacora.registrarDeSesion("PSS", accion, "prestador", id, detalle);
        }
    }

    public long registrarPrestador(Prestador prestador) throws SQLException {

        validarCampos(prestador);

        // El RNC o la cédula no pueden estar registrados.
        if (prestadorDAO.buscarPorRncCedula(prestador.getRncCedula()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un prestador con ese RNC o cédula.");
        }

        // Estado inicial si no se especificó.
        if (prestador.getEstadoContrato() == null
                || prestador.getEstadoContrato().isBlank()) {
            prestador.setEstadoContrato("ACTIVO");
        }
        validarEstado(prestador.getEstadoContrato());

        long id = prestadorDAO.registrarPrestador(prestador);
        auditar("CREAR", id, "Prestador registrado");
        return id;
    }

    public void actualizarPrestador(Prestador prestador) throws SQLException {

        if (prestador.getId() == null) {
            throw new IllegalArgumentException(
                    "Selecciona el prestador que quieres editar.");
        }

        validarCampos(prestador);

        // La comprobación de duplicados distingue al propio prestador de otro.
        Optional<Prestador> existente =
                prestadorDAO.buscarPorRncCedula(prestador.getRncCedula());

        if (existente.isPresent()
                && !existente.get().getId().equals(prestador.getId())) {
            throw new IllegalArgumentException(
                    "Ese RNC o cédula pertenece a otro prestador.");
        }

        prestadorDAO.actualizarPrestador(prestador);
        auditar("ACTUALIZAR", prestador.getId(), "Datos del prestador actualizados");
    }

    public void cambiarEstadoContrato(Long id, String estado) throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException("Selecciona un prestador.");
        }

        validarEstado(estado);
        prestadorDAO.cambiarEstadoContrato(id, estado);
        auditar("CAMBIAR_ESTADO", id, "Estado de contrato: " + estado);
    }

    public List<Prestador> listarPrestadores() throws SQLException {
        return prestadorDAO.listarPrestadores();
    }

    public List<Prestador> buscarPorNombre(String nombre) throws SQLException {
        return prestadorDAO.buscarPorNombre(nombre);
    }

    // Filtro de la lista: texto (nombre o RNC/cédula), tipo y estado.
    // Un parámetro null o vacío significa "sin filtrar por ese dato".
    public List<Prestador> filtrar(String texto, String tipo, String estado)
            throws SQLException {

        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Prestador> resultado = new ArrayList<>();

        for (Prestador p : prestadorDAO.listarPrestadores()) {

            boolean coincideTexto = t.isEmpty()
                    || p.getNombre().toLowerCase().contains(t)
                    || p.getRncCedula().toLowerCase().contains(t);

            boolean coincideTipo = tipo == null || tipo.equals(p.getTipo());
            boolean coincideEstado = estado == null || estado.equals(p.getEstadoContrato());

            if (coincideTexto && coincideTipo && coincideEstado) {
                resultado.add(p);
            }
        }

        return resultado;
    }

    // Campos obligatorios y formatos, compartidos por alta y edición.
    private void validarCampos(Prestador prestador) {

        if (vacio(prestador.getNombre())) {
            throw new IllegalArgumentException("El nombre del prestador es obligatorio.");
        }

        String documento = prestador.getRncCedula();

        if (vacio(documento)) {
            throw new IllegalArgumentException("El RNC o la cédula es obligatorio.");
        }
        if (!Validador.esRnc(documento) && !Validador.esCedula(documento)) {
            throw new IllegalArgumentException(
                    "El documento debe tener formato de RNC de 9 dígitos "
                    + "o cédula 000-0000000-0.");
        }

        if (!esTipoValido(prestador.getTipo())) {
            throw new IllegalArgumentException("Selecciona el tipo de prestador.");
        }

        if (!vacio(prestador.getTelefono())
                && !Validador.esTelefono(prestador.getTelefono())) {
            throw new IllegalArgumentException(
                    "El teléfono debe tener el formato (000) 000-0000.");
        }

        if (!vacio(prestador.getCorreo())
                && !Validador.esCorreo(prestador.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido.");
        }
    }

    private boolean esTipoValido(String tipo) {
        return "CLINICA".equals(tipo)
                || "HOSPITAL".equals(tipo)
                || "LABORATORIO".equals(tipo)
                || "CENTRO_DIAGNOSTICO".equals(tipo)
                || "MEDICO_INDEPENDIENTE".equals(tipo)
                || "FARMACIA".equals(tipo);
    }

    private void validarEstado(String estado) {
        if (!"ACTIVO".equals(estado)
                && !"SUSPENDIDO".equals(estado)
                && !"TERMINADO".equals(estado)) {
            throw new IllegalArgumentException("El estado del contrato no es válido.");
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}