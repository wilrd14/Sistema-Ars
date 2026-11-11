package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Opcion;

import java.sql.SQLException;
import java.util.List;

/** Listas para llenar combos: provincias, municipios y planes activos. */
public interface CatalogoDAO {

    List<Opcion> listarProvincias() throws SQLException;

    List<Opcion> listarMunicipios(int provinciaId) throws SQLException;

    List<Opcion> listarPlanesActivos() throws SQLException;
}