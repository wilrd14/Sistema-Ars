package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.BusquedaGlobalDAO;
import edu.uasd.vitasalud.model.ResultadoBusqueda;
import edu.uasd.vitasalud.model.ResultadoBusqueda.Tipo;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-SEG-08 con un DAO falso (no necesita MySQL). */
class BusquedaGlobalServiceTest {

    private static class DaoFalso implements BusquedaGlobalDAO {
        final List<String> patrones = new ArrayList<>();
        final List<Integer> maximos = new ArrayList<>();
        boolean fallar;

        private List<ResultadoBusqueda> responder(Tipo tipo, String patron, int max) throws SQLException {
            if (fallar) {
                throw new SQLException("sin conexión");
            }
            patrones.add(patron);
            maximos.add(max);
            return List.of(new ResultadoBusqueda(tipo, "titulo " + tipo, "detalle", "clave"));
        }

        @Override
        public List<ResultadoBusqueda> buscarAfiliados(String patron, int max) throws SQLException {
            return responder(Tipo.AFILIADO, patron, max);
        }

        @Override
        public List<ResultadoBusqueda> buscarAutorizaciones(String patron, int max) throws SQLException {
            return responder(Tipo.AUTORIZACION, patron, max);
        }

        @Override
        public List<ResultadoBusqueda> buscarPrestadores(String patron, int max) throws SQLException {
            return responder(Tipo.PRESTADOR, patron, max);
        }
    }

    @Test
    void conMenosDeTresCaracteresNoConsultaNada() {
        DaoFalso dao = new DaoFalso();
        BusquedaGlobalService service = new BusquedaGlobalService(dao);
        assertTrue(service.buscar("ab").isEmpty());
        assertTrue(service.buscar("  a ").isEmpty());
        assertTrue(service.buscar(null).isEmpty());
        assertTrue(dao.patrones.isEmpty());
    }

    @Test
    void consultaLosTresTiposConPatronYMaximo() {
        DaoFalso dao = new DaoFalso();
        List<ResultadoBusqueda> r = new BusquedaGlobalService(dao).buscar("  Perez ");
        assertEquals(List.of(Tipo.AFILIADO, Tipo.AUTORIZACION, Tipo.PRESTADOR),
                r.stream().map(ResultadoBusqueda::tipo).toList());
        assertEquals(List.of("%Perez%", "%Perez%", "%Perez%"), dao.patrones);
        assertEquals(List.of(8, 8, 8), dao.maximos);
    }

    @Test
    void escapaLosComodinesDelUsuario() {
        DaoFalso dao = new DaoFalso();
        new BusquedaGlobalService(dao).buscar("10%_x");
        assertEquals("%10\\%\\_x%", dao.patrones.get(0));
    }

    @Test
    void errorDeBaseDeDatosSeConvierteEnMensajeComprensible() {
        DaoFalso dao = new DaoFalso();
        dao.fallar = true;
        ServicioException e = assertThrows(ServicioException.class,
                () -> new BusquedaGlobalService(dao).buscar("Perez"));
        assertTrue(e.getMessage().contains("No se pudo realizar la búsqueda"));
    }
}
