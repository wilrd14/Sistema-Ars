package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Acceso a la tabla {@code usuario}. Implementación JDBC en {@code dao.impl}. */
public interface UsuarioDAO {

    /** Prestador disponible para asociar a un usuario (consulta de solo lectura sobre {@code prestador}). */
    record PrestadorOpcion(long id, String nombre, String estadoContrato) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    Optional<Usuario> buscarPorUsername(String username) throws SQLException;

    Optional<Usuario> buscarPorId(long id) throws SQLException;

    /** Lista usuarios cuyo nombre, usuario o cargo contienen el filtro (null o vacío = todos). */
    List<Usuario> listar(String filtro) throws SQLException;

    /** Prestadores para el combo del formulario, ordenados por nombre. */
    List<PrestadorOpcion> listarPrestadores() throws SQLException;

    /** Inserta el usuario (con {@code passwordHash} ya cifrado) y devuelve el id generado. */
    long insertar(Usuario usuario) throws SQLException;

    /** Actualiza nombre, rol, cargo y prestador. No toca clave, estado ni perfil. */
    void actualizar(Usuario usuario) throws SQLException;

    void actualizarPassword(long usuarioId, String passwordHash) throws SQLException;

    /** Suma un intento fallido y devuelve el total acumulado. */
    int registrarIntentoFallido(long usuarioId) throws SQLException;

    void reiniciarIntentos(long usuarioId) throws SQLException;

    void cambiarEstado(long usuarioId, String estado) throws SQLException;
}
