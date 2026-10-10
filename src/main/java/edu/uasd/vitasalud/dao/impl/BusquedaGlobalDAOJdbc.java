package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD;
import edu.uasd.vitasalud.dao.BusquedaGlobalDAO;
import edu.uasd.vitasalud.model.ResultadoBusqueda;
import edu.uasd.vitasalud.model.ResultadoBusqueda.Tipo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO JDBC de la búsqueda global: solo SELECT, siempre con PreparedStatement. */
public class BusquedaGlobalDAOJdbc implements BusquedaGlobalDAO {

    private static final String SQL_AFILIADOS = """
            SELECT numero_afiliado, nombres, apellidos, numero_documento, estado
              FROM afiliado
             WHERE es_borrador = FALSE
               AND (numero_documento LIKE ? OR nss LIKE ? OR nombres LIKE ? OR apellidos LIKE ?
                    OR numero_afiliado LIKE ? OR CONCAT(nombres, ' ', apellidos) LIKE ?)
             ORDER BY apellidos, nombres
             LIMIT ?
            """;

    private static final String SQL_AUTORIZACIONES = """
            SELECT a.numero, a.estado, CONCAT(f.nombres, ' ', f.apellidos) AS afiliado
              FROM autorizacion a
              JOIN afiliado f ON f.id = a.afiliado_id
             WHERE a.numero LIKE ?
             ORDER BY a.fecha_solicitud DESC
             LIMIT ?
            """;

    private static final String SQL_PRESTADORES = """
            SELECT rnc_cedula, nombre, tipo
              FROM prestador
             WHERE nombre LIKE ? OR rnc_cedula LIKE ?
             ORDER BY nombre
             LIMIT ?
            """;

    @Override
    public List<ResultadoBusqueda> buscarAfiliados(String patron, int max) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_AFILIADOS)) {
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, patron);
            }
            ps.setInt(7, max);
            List<ResultadoBusqueda> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResultadoBusqueda(Tipo.AFILIADO,
                            rs.getString("nombres") + " " + rs.getString("apellidos"),
                            rs.getString("numero_afiliado") + " · " + rs.getString("numero_documento")
                                    + " · " + rs.getString("estado"),
                            rs.getString("numero_afiliado")));
                }
            }
            return lista;
        }
    }

    @Override
    public List<ResultadoBusqueda> buscarAutorizaciones(String patron, int max) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_AUTORIZACIONES)) {
            ps.setString(1, patron);
            ps.setInt(2, max);
            List<ResultadoBusqueda> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResultadoBusqueda(Tipo.AUTORIZACION,
                            rs.getString("numero"),
                            rs.getString("afiliado") + " · " + rs.getString("estado"),
                            rs.getString("numero")));
                }
            }
            return lista;
        }
    }

    @Override
    public List<ResultadoBusqueda> buscarPrestadores(String patron, int max) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_PRESTADORES)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setInt(3, max);
            List<ResultadoBusqueda> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResultadoBusqueda(Tipo.PRESTADOR,
                            rs.getString("nombre"),
                            rs.getString("rnc_cedula") + " · " + rs.getString("tipo"),
                            rs.getString("rnc_cedula")));
                }
            }
            return lista;
        }
    }
}
