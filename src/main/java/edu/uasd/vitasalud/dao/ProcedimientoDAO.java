
package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Procedimiento;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public interface ProcedimientoDAO {
    Optional<Procedimiento> buscarPorId(Long id) throws SQLException;
    List<Procedimiento> listarTodo() throws SQLException;
    void guardar(Procedimiento procedimiento) throws SQLException;
    void modificar(Procedimiento procedimiento) throws SQLException;
    void eliminar(Long id) throws SQLException;
}

