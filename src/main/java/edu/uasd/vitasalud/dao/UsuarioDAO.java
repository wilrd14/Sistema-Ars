package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Usuario;

import java.sql.SQLException;
import java.util.Optional;

/** Acceso a la tabla {@code usuario}. Implementación JDBC en {@code dao.impl}. */
public interface UsuarioDAO {

    Optional<Usuario> buscarPorUsername(String username) throws SQLException;

    /** Suma un intento fallido y devuelve el total acumulado. */
    int registrarIntentoFallido(long usuarioId) throws SQLException;

    void reiniciarIntentos(long usuarioId) throws SQLException;

    void cambiarEstado(long usuarioId, String estado) throws SQLException;
}
