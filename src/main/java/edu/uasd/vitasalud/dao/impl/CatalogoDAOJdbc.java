package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.CatalogoDAO;
import edu.uasd.vitasalud.model.Opcion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Implementa CatalogoDAO con JDBC. */
public class CatalogoDAOJdbc implements CatalogoDAO {

    @Override
    public List<Opcion> listarProvincias() throws SQLException {
        return consultar("SELECT id, nombre FROM provincia ORDER BY nombre", null);
    }

    @Override
    public List<Opcion> listarMunicipios(int provinciaId) throws SQLException {
        return consultar("SELECT id, nombre FROM municipio "
                + "WHERE provincia_id = ? ORDER BY nombre", provinciaId);
    }

    @Override
    public List<Opcion> listarPlanesActivos() throws SQLException {
        return consultar("SELECT id, nombre FROM plan "
                + "WHERE estado = 'ACTIVO' ORDER BY nombre", null);
    }

    private List<Opcion> consultar(String sql, Integer parametro) throws SQLException {
        List<Opcion> lista = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parametro != null) {
                ps.setInt(1, parametro);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Opcion(rs.getInt("id"), rs.getString("nombre")));
                }
            }
        }

        return lista;
    }
}