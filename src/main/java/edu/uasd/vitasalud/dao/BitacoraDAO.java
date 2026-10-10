package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Bitacora;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Acceso a la tabla {@code bitacora}. Implementación JDBC en {@code dao.impl}. */
public interface BitacoraDAO {

    /**
     * Inserta un registro. La fecha/hora la pone la base de datos y el perfil se toma del usuario
     * indicado en {@code b.getUsuarioId()} (null si no hay usuario).
     */
    void insertar(Bitacora b) throws SQLException;

    /**
     * Busca registros, del más reciente al más antiguo.
     *
     * @param modulo filtro por módulo (null o vacío = todos)
     * @param desde  fecha inicial inclusive (null = sin límite)
     * @param hasta  fecha final inclusive (null = sin límite)
     * @param limite máximo de filas a devolver
     */
    List<Bitacora> buscar(String modulo, LocalDate desde, LocalDate hasta, int limite) throws SQLException;
}
