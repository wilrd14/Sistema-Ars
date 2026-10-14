package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Tarifa;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public interface TarifaDAO {
    Optional<Tarifa> buscarPorId(Long id) throws SQLException;
    List<Tarifa> listarTodo() throws SQLException;
    void registrar(Tarifa tarifa) throws SQLException;
    void actualizar(Tarifa tarifa) throws SQLException;
    void eliminar(Long id) throws SQLException;
}