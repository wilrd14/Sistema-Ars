
package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Comunicado;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ComunicadoDAO {

    Optional<Comunicado> buscarPorId(Integer id) throws SQLException;

    List<Comunicado> listarTodo() throws SQLException;

    void registrar(Comunicado comunicado) throws SQLException;

    void actualizar(Comunicado comunicado) throws SQLException;

    void eliminar(Integer id) throws SQLException;
}
