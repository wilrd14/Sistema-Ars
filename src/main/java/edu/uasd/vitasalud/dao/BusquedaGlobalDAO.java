package edu.uasd.vitasalud.dao;

import edu.uasd.vitasalud.model.ResultadoBusqueda;

import java.sql.SQLException;
import java.util.List;

/** Consultas de solo lectura de la búsqueda global (RF-SEG-08). {@code patron} ya viene con los comodines LIKE. */
public interface BusquedaGlobalDAO {

    /** Afiliados por documento, NSS, nombres, apellidos o número de afiliado. */
    List<ResultadoBusqueda> buscarAfiliados(String patron, int max) throws SQLException;

    /** Autorizaciones por número. */
    List<ResultadoBusqueda> buscarAutorizaciones(String patron, int max) throws SQLException;

    /** Prestadores por nombre o RNC/cédula. */
    List<ResultadoBusqueda> buscarPrestadores(String patron, int max) throws SQLException;
}
