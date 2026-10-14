package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Cobertura;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public interface CoberturaDAO {

    Optional<Cobertura> buscarPorId(Long id) throws SQLException;

    List<Cobertura> listarTodo() throws SQLException;

    void registrar(Cobertura cobertura) throws SQLException;

    void actualizar(Cobertura cobertura) throws SQLException;

    void eliminar(Long id) throws SQLException;
}