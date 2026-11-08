
package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Prestador;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de acceso a datos de los prestadores.
 */
public interface PrestadorDAO {

    Optional<Prestador> buscarPorRncCedula(String rncCedula)
            throws SQLException;

    long registrarPrestador(Prestador prestador)
            throws SQLException;

    void actualizarPrestador(Prestador prestador)
            throws SQLException;

    void cambiarEstadoContrato(Long id, String estadoContrato)
            throws SQLException;

    List<Prestador> listarPrestadores()
            throws SQLException;

    List<Prestador> buscarPorNombre(String nombre)
            throws SQLException;
}
