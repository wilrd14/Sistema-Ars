package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.BusquedaGlobalDAO;
import edu.uasd.vitasalud.dao.impl.BusquedaGlobalDAOJdbc;
import edu.uasd.vitasalud.model.ResultadoBusqueda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Búsqueda global del encabezado de la Consola interna (RF-SEG-08). Solo lectura. */
public class BusquedaGlobalService {

    public static final int MIN_CARACTERES = 3;
    public static final int MAX_POR_TIPO = 8;

    private static final Logger LOG = Logger.getLogger(BusquedaGlobalService.class.getName());

    private final BusquedaGlobalDAO dao;

    public BusquedaGlobalService() {
        this(new BusquedaGlobalDAOJdbc());
    }

    public BusquedaGlobalService(BusquedaGlobalDAO dao) {
        this.dao = dao;
    }

    /**
     * Busca afiliados, autorizaciones y prestadores que contengan el término.
     *
     * @return hasta {@link #MAX_POR_TIPO} resultados por tipo (afiliados, autorizaciones, prestadores);
     *         lista vacía si el término tiene menos de {@link #MIN_CARACTERES} caracteres
     * @throws ServicioException si falla la base de datos
     */
    public List<ResultadoBusqueda> buscar(String termino) {
        String limpio = termino == null ? "" : termino.trim();
        if (limpio.length() < MIN_CARACTERES) {
            return List.of();
        }
        String patron = "%" + escaparLike(limpio) + "%";
        try {
            List<ResultadoBusqueda> todos = new ArrayList<>();
            todos.addAll(dao.buscarAfiliados(patron, MAX_POR_TIPO));
            todos.addAll(dao.buscarAutorizaciones(patron, MAX_POR_TIPO));
            todos.addAll(dao.buscarPrestadores(patron, MAX_POR_TIPO));
            return todos;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos en la búsqueda global", e);
            throw new ServicioException("No se pudo realizar la búsqueda. Verifica la conexión con la base de datos.", e);
        }
    }

    /** Evita que % y _ escritos por el usuario actúen como comodines. */
    static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
