
package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.ComunicadoDAO;
import edu.uasd.vitasalud.model.Comunicado;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ComunicadoService {

    private final ComunicadoDAO comunicadoDAO;

    public ComunicadoService(ComunicadoDAO comunicadoDAO) {
        this.comunicadoDAO = comunicadoDAO;
    }

    public Optional<Comunicado> buscarPorId(Integer id) throws SQLException {
        return comunicadoDAO.buscarPorId(id);
    }

    public List<Comunicado> listarTodo() throws SQLException {
        return comunicadoDAO.listarTodo();
    }

    public void registrar(Comunicado comunicado) throws SQLException {
        validar(comunicado);
        comunicadoDAO.registrar(comunicado);
    }

    public void actualizar(Comunicado comunicado) throws SQLException {
        validar(comunicado);
        if (comunicado.getId() == null) {
            throw new IllegalArgumentException("El ID del comunicado es obligatorio.");
        }
        comunicadoDAO.actualizar(comunicado);
    }

    public void eliminar(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido.");
        }
        comunicadoDAO.eliminar(id);
    }

    private void validar(Comunicado comunicado) {
        if (comunicado == null) {
            throw new IllegalArgumentException("El comunicado es obligatorio.");
        }
        if (comunicado.getTitulo() == null || comunicado.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (comunicado.getTitulo().length() > 150) {
            throw new IllegalArgumentException("El título no puede superar 150 caracteres.");
        }
        if (comunicado.getMensaje() == null || comunicado.getMensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje es obligatorio.");
        }
        if (comunicado.getVigente() == null) {
            throw new IllegalArgumentException("Debes indicar si está vigente.");
        }
    }
}
