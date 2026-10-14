
package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Plan;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public interface PlanDAO {

    Optional<Plan> buscarPorId(String id) throws SQLException;

    List<Plan> listarTodo() throws SQLException;

    void registrar(Plan plan) throws SQLException;

    void actualizar(Plan plan) throws SQLException;

    void eliminar(int id) throws SQLException;
}

