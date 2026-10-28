package edu.uasd.vitasalud.service;

import edu.uasd.vitasalud.dao.ReporteDAO;
import edu.uasd.vitasalud.model.Reporte;
import edu.uasd.vitasalud.model.ResumenPanel;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.TablaReporte;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-REP-01 a RF-REP-04 con un DAO falso en memoria (sin MySQL). */
class ReporteServiceTest {

    /** DAO falso: devuelve datos fijos, recuerda el último rango pedido y puede fallar. */
    private static class DaoFalso implements ReporteDAO {
        List<ConteoEstado> estados = new ArrayList<>();
        List<DevolucionPrestador> devoluciones = new ArrayList<>();
        List<SolicitudAntigua> antiguas = new ArrayList<>();
        long enAuditoria;
        long bloqueadas;
        boolean falla;
        LocalDate ultimoDesde;
        LocalDate ultimoHasta;

        private void revisar() throws SQLException {
            if (falla) {
                throw new SQLException("sin conexion");
            }
        }

        @Override
        public List<ConteoEstado> autorizacionesPorEstado(LocalDate desde, LocalDate hasta) throws SQLException {
            revisar();
            ultimoDesde = desde;
            ultimoHasta = hasta;
            return estados;
        }

        @Override
        public List<ReclamacionPrestador> reclamacionesPorPrestador(LocalDate d, LocalDate h) throws SQLException {
            revisar();
            return List.of();
        }

        @Override
        public List<DevolucionPrestador> devoluciones(LocalDate d, LocalDate h) throws SQLException {
            revisar();
            return devoluciones;
        }

        @Override
        public List<Pago> pagos(LocalDate d, LocalDate h) throws SQLException {
            revisar();
            return List.of();
        }

        @Override
        public List<ConteoPrecertificacion> precertificaciones(LocalDate d, LocalDate h) throws SQLException {
            revisar();
            return List.of();
        }

        @Override
        public List<ActividadUsuario> actividadUsuarios(LocalDate d, LocalDate h) throws SQLException {
            revisar();
            return List.of();
        }

        @Override
        public List<SolicitudAntigua> pendientesMasAntiguas(int limite) throws SQLException {
            revisar();
            return antiguas;
        }

        @Override
        public long reclamacionesEnAuditoria() throws SQLException {
            revisar();
            return enAuditoria;
        }

        @Override
        public long cuentasBloqueadas() throws SQLException {
            revisar();
            return bloqueadas;
        }
    }

    private final DaoFalso dao = new DaoFalso();
    private final ReporteService servicio = new ReporteService(dao);

    @Test
    void panelCuentaPorEstadoYMuestraCeroEnLosQueFaltan() {
        dao.estados.add(new ReporteDAO.ConteoEstado("PENDIENTE", 4, 0, 0));
        dao.estados.add(new ReporteDAO.ConteoEstado("APROBADA", 7, 7, 0));
        ResumenPanel p = servicio.cargarPanel(Rol.ANALISTA_AUTORIZACIONES);
        assertEquals(4, p.pendientes());
        assertEquals(0, p.enRevision());
        assertEquals(7, p.aprobadas());
        assertEquals(0, p.negadas());
        assertNull(p.aviso());
    }

    @Test
    void panelConBaseDeDatosCaidaDevuelveCerosYAviso() {
        dao.falla = true;
        ResumenPanel p = servicio.cargarPanel(Rol.ADMINISTRADOR);
        assertEquals(0, p.pendientes());
        assertNotNull(p.aviso());
        assertTrue(p.atencion().isEmpty());
    }

    @Test
    void cuentasBloqueadasSoloLasVeElAdministrador() {
        dao.bloqueadas = 2;
        dao.enAuditoria = 1;
        dao.antiguas.add(new ReporteDAO.SolicitudAntigua("AUT-000001", "Clinica Uno", LocalDateTime.of(2026, 3, 5, 9, 0)));
        List<String> admin = servicio.cargarPanel(Rol.ADMINISTRADOR).atencion();
        List<String> auditor = servicio.cargarPanel(Rol.AUDITOR_MEDICO).atencion();
        assertEquals(3, admin.size());
        assertTrue(admin.contains("2 cuentas bloqueadas"));
        assertEquals(2, auditor.size());
        assertTrue(admin.get(0).contains("05/03/2026"));
    }

    @Test
    void tiempoPromedioDeRespuestaSeCalculaSobreLasDecididas() {
        // 2 decididas que sumaron 5 h en total -> 2 h 30 min; una pendiente sin decision no cuenta.
        dao.estados.add(new ReporteDAO.ConteoEstado("APROBADA", 2, 2, 5 * 3600));
        dao.estados.add(new ReporteDAO.ConteoEstado("PENDIENTE", 1, 0, 0));
        TablaReporte t = servicio.generar(Reporte.AUTORIZACIONES, null, null, Rol.ADMINISTRADOR);
        assertEquals("Aprobada", t.filas().get(0).get(0));
        assertEquals("2 h 30 min", t.filas().get(0).get(4));
        assertEquals("-", t.filas().get(1).get(4));
        assertTrue(t.resumen().contains("Total: 3"));
        assertTrue(t.resumen().contains("2 h 30 min"));
    }

    @Test
    void duracionPromedioUsaMinutosHorasYDias() {
        assertEquals("-", ReporteService.duracionPromedio(100, 0));
        assertEquals("45 min", ReporteService.duracionPromedio(45 * 60, 1));
        assertEquals("1 d 3 h", ReporteService.duracionPromedio(27 * 3600, 1));
    }

    @Test
    void rangoInvertidoSeRechazaYElRangoValidoLlegaAlDao() {
        LocalDate d = LocalDate.of(2026, 2, 1);
        LocalDate h = LocalDate.of(2026, 1, 1);
        assertThrows(ServicioException.class, () -> servicio.generar(Reporte.AUTORIZACIONES, d, h, Rol.ADMINISTRADOR));
        servicio.generar(Reporte.AUTORIZACIONES, h, d, Rol.ADMINISTRADOR);
        assertEquals(h, dao.ultimoDesde);
        assertEquals(d, dao.ultimoHasta);
    }

    @Test
    void cadaRolSoloVeSusReportes() {
        assertEquals(6, servicio.reportesPara(Rol.ADMINISTRADOR).size());
        assertFalse(servicio.reportesPara(Rol.ANALISTA_AUTORIZACIONES).contains(Reporte.PAGOS));
        assertFalse(servicio.reportesPara(Rol.AUDITOR_MEDICO).contains(Reporte.ACTIVIDAD));
        assertTrue(servicio.reportesPara(Rol.AGENTE_AFILIACION).isEmpty());
        assertThrows(ServicioException.class,
                () -> servicio.generar(Reporte.ACTIVIDAD, null, null, Rol.AUDITOR_MEDICO));
    }

    @Test
    void falloDeBaseDeDatosEnReporteDaMensajeComprensible() {
        dao.falla = true;
        ServicioException e = assertThrows(ServicioException.class,
                () -> servicio.generar(Reporte.DEVOLUCIONES, null, null, Rol.ADMINISTRADOR));
        assertFalse(e.getMessage().contains("SQL"));
    }

    @Test
    void devolucionesMarcanReincidenciaDesdeDosDevoluciones() {
        dao.devoluciones.add(new ReporteDAO.DevolucionPrestador("Clinica Uno", "Falta factura", 1));
        dao.devoluciones.add(new ReporteDAO.DevolucionPrestador("Clinica Uno", "Monto errado", 1));
        dao.devoluciones.add(new ReporteDAO.DevolucionPrestador("Lab Dos", "Falta factura", 1));
        TablaReporte t = servicio.generar(Reporte.DEVOLUCIONES, null, null, Rol.AUDITOR_MEDICO);
        assertEquals("Sí", t.filas().get(0).get(4));
        assertEquals("2", t.filas().get(0).get(3));
        assertEquals("No", t.filas().get(2).get(4));
        assertTrue(t.resumen().startsWith("1 prestador"));
    }

    @Test
    void csvLlevaBomSeparadorPuntoYComaYEscapaCorrectamente() {
        TablaReporte t = new TablaReporte(Reporte.PAGOS, List.of("Prestador", "Nota"),
                List.of(List.of("Clínica; \"Vida\"", "linea1\nlinea2"), List.of("Normal", "")), null);
        String csv = servicio.aCsv(t);
        assertTrue(csv.startsWith("﻿Prestador;Nota\r\n"));
        assertTrue(csv.contains("\"Clínica; \"\"Vida\"\"\";\"linea1\nlinea2\"\r\n"));
        assertTrue(csv.endsWith("Normal;\r\n"));
    }

    @Test
    void exportarCsvEscribeUtf8ConBom() throws Exception {
        Path archivo = Files.createTempFile("reporte", ".csv");
        try {
            TablaReporte t = new TablaReporte(Reporte.PAGOS, List.of("Año"), List.of(List.of("ñandú")), null);
            servicio.exportarCsv(t, archivo);
            byte[] bytes = Files.readAllBytes(archivo);
            assertEquals((byte) 0xEF, bytes[0]);
            assertEquals((byte) 0xBB, bytes[1]);
            assertEquals((byte) 0xBF, bytes[2]);
            assertTrue(new String(bytes, StandardCharsets.UTF_8).contains("ñandú"));
        } finally {
            Files.deleteIfExists(archivo);
        }
    }

    @Test
    void pdfEmpiezaConCabeceraYNoEstaVacio() {
        TablaReporte t = new TablaReporte(Reporte.PAGOS, List.of("Prestador", "Monto"),
                List.of(List.of("Clínica Año Nuevo, ñandú", "RD$ 1,000.00")), "Resumen: total pagado");
        byte[] pdf = servicio.aPdf(t, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        assertTrue(pdf.length > 500);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void pdfSinFilasNiFechasTampocoFalla() {
        TablaReporte t = new TablaReporte(Reporte.AUTORIZACIONES, List.of("Estado", "Cantidad"), List.of(), null);
        byte[] pdf = servicio.aPdf(t, null, null);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void exportarPdfEscribeElArchivo() throws Exception {
        Path archivo = Files.createTempFile("reporte", ".pdf");
        try {
            TablaReporte t = new TablaReporte(Reporte.PAGOS, List.of("Año"), List.of(List.of("ñandú áéíóú")), null);
            servicio.exportarPdf(t, null, null, archivo);
            assertTrue(Files.size(archivo) > 0);
            assertEquals("%PDF", new String(Files.readAllBytes(archivo), 0, 4, StandardCharsets.US_ASCII));
        } finally {
            Files.deleteIfExists(archivo);
        }
    }

    @Test
    void nombreArchivoPdfTerminaEnPdf() {
        assertTrue(ReporteService.nombreArchivoPdf(Reporte.PAGOS).endsWith(".pdf"));
    }
}
