package edu.uasd.vitasalud.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import edu.uasd.vitasalud.dao.ReporteDAO;
import edu.uasd.vitasalud.dao.ReporteDAO.ConteoEstado;
import edu.uasd.vitasalud.dao.impl.ReporteDAOJdbc;
import edu.uasd.vitasalud.model.EstadoAutorizacion;
import edu.uasd.vitasalud.model.Reporte;
import edu.uasd.vitasalud.model.ResumenPanel;
import edu.uasd.vitasalud.model.Rol;
import edu.uasd.vitasalud.model.TablaReporte;
import edu.uasd.vitasalud.util.Formatos;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Panel (RF-REP-01), reportes (RF-REP-02/03) y exportación a CSV y PDF (RF-REP-04). */
public class ReporteService {

    public static final char SEPARADOR_CSV = ';';
    private static final String BOM = "﻿";
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Logger LOG = Logger.getLogger(ReporteService.class.getName());
    private static final int MAX_ATENCION = 3;

    private final ReporteDAO dao;

    public ReporteService() {
        this(new ReporteDAOJdbc());
    }

    public ReporteService(ReporteDAO dao) {
        this.dao = dao;
    }

    // ---------------------------------------------------------------- Panel

    /** Nunca lanza: si la base de datos falla devuelve ceros con un aviso comprensible. */
    public ResumenPanel cargarPanel(Rol rol) {
        try {
            Map<String, Long> porEstado = new LinkedHashMap<>();
            for (ConteoEstado c : dao.autorizacionesPorEstado(null, null)) {
                porEstado.put(c.estado(), c.total());
            }
            List<String> atencion = new ArrayList<>();
            for (ReporteDAO.SolicitudAntigua s : dao.pendientesMasAntiguas(MAX_ATENCION)) {
                atencion.add("Solicitud " + s.numero() + " de " + s.prestador() + " pendiente desde "
                        + Formatos.fecha(s.fechaSolicitud().toLocalDate()));
            }
            long enAuditoria = dao.reclamacionesEnAuditoria();
            if (enAuditoria > 0) {
                atencion.add(enAuditoria + (enAuditoria == 1 ? " reclamación en auditoría" : " reclamaciones en auditoría"));
            }
            if (rol == Rol.ADMINISTRADOR) {
                long bloqueadas = dao.cuentasBloqueadas();
                if (bloqueadas > 0) {
                    atencion.add(bloqueadas + (bloqueadas == 1 ? " cuenta bloqueada" : " cuentas bloqueadas"));
                }
            }
            return new ResumenPanel(valor(porEstado, EstadoAutorizacion.PENDIENTE),
                    valor(porEstado, EstadoAutorizacion.EN_REVISION),
                    valor(porEstado, EstadoAutorizacion.APROBADA), valor(porEstado, EstadoAutorizacion.NEGADA),
                    atencion, null);
        } catch (SQLException | RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo cargar el panel", e);
            return ResumenPanel.vacio("No se pudo consultar la base de datos. Los valores se muestran en 0; "
                    + "verifica la conexión y pulsa Actualizar.");
        }
    }

    private static long valor(Map<String, Long> mapa, EstadoAutorizacion estado) {
        return mapa.getOrDefault(estado.name(), 0L);
    }

    // -------------------------------------------------------------- Reportes

    /** Reportes que el rol puede ver, en el orden de {@link Reporte}. */
    public List<Reporte> reportesPara(Rol rol) {
        List<Reporte> lista = new ArrayList<>();
        for (Reporte r : Reporte.values()) {
            if (r.permitidoPara(rol)) {
                lista.add(r);
            }
        }
        return lista;
    }

    /**
     * @throws ServicioException si el rango es inválido, el rol no puede ver el reporte o falla la base de datos
     */
    public TablaReporte generar(Reporte reporte, LocalDate desde, LocalDate hasta, Rol rol) {
        if (!reporte.permitidoPara(rol)) {
            throw new ServicioException("Tu rol no tiene acceso al reporte \"" + reporte.titulo() + "\".");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ServicioException("La fecha \"Desde\" no puede ser posterior a la fecha \"Hasta\".");
        }
        try {
            return switch (reporte) {
                case AUTORIZACIONES -> autorizaciones(dao.autorizacionesPorEstado(desde, hasta));
                case RECLAMACIONES -> reclamaciones(dao.reclamacionesPorPrestador(desde, hasta));
                case DEVOLUCIONES -> devoluciones(dao.devoluciones(desde, hasta));
                case PAGOS -> pagos(dao.pagos(desde, hasta));
                case PRECERTIFICACIONES -> precertificaciones(dao.precertificaciones(desde, hasta));
                case ACTIVIDAD -> actividad(dao.actividadUsuarios(desde, hasta));
            };
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error al generar el reporte " + reporte, e);
            throw new ServicioException("No se pudo generar el reporte. Verifica la conexión con la base de datos.", e);
        }
    }

    private TablaReporte autorizaciones(List<ConteoEstado> datos) {
        long total = 0;
        long decididas = 0;
        long segundos = 0;
        for (ConteoEstado c : datos) {
            total += c.total();
            decididas += c.decididas();
            segundos += c.segundosRespuesta();
        }
        List<List<String>> filas = new ArrayList<>();
        for (ConteoEstado c : datos) {
            filas.add(List.of(etiquetaEstado(c.estado()), String.valueOf(c.total()), porcentaje(c.total(), total),
                    String.valueOf(c.decididas()), duracionPromedio(c.segundosRespuesta(), c.decididas())));
        }
        String resumen = "Total: " + total + " autorizaciones. Tiempo promedio de respuesta: "
                + duracionPromedio(segundos, decididas) + ".";
        return new TablaReporte(Reporte.AUTORIZACIONES,
                List.of("Estado", "Cantidad", "% del total", "Con decisión", "Tiempo promedio de respuesta"),
                filas, resumen);
    }

    private TablaReporte reclamaciones(List<ReporteDAO.ReclamacionPrestador> datos) {
        List<List<String>> filas = new ArrayList<>();
        for (ReporteDAO.ReclamacionPrestador r : datos) {
            filas.add(List.of(r.prestador(), String.valueOf(r.total()), Formatos.moneda(nz(r.reclamado())),
                    Formatos.moneda(nz(r.glosado())), Formatos.moneda(nz(r.aPagar()))));
        }
        return new TablaReporte(Reporte.RECLAMACIONES,
                List.of("Prestador", "Reclamaciones", "Reclamado", "Glosado", "A pagar"), filas, null);
    }

    private TablaReporte devoluciones(List<ReporteDAO.DevolucionPrestador> datos) {
        Map<String, Long> porPrestador = new LinkedHashMap<>();
        for (ReporteDAO.DevolucionPrestador d : datos) {
            porPrestador.merge(d.prestador(), d.cantidad(), Long::sum);
        }
        List<List<String>> filas = new ArrayList<>();
        for (ReporteDAO.DevolucionPrestador d : datos) {
            long totalPrestador = porPrestador.get(d.prestador());
            filas.add(List.of(d.prestador(), d.motivo(), String.valueOf(d.cantidad()),
                    String.valueOf(totalPrestador), totalPrestador >= 2 ? "Sí" : "No"));
        }
        long reincidentes = porPrestador.values().stream().filter(n -> n >= 2).count();
        return new TablaReporte(Reporte.DEVOLUCIONES,
                List.of("Prestador", "Motivo", "Cantidad", "Total del prestador", "Reincidente"), filas,
                reincidentes + " prestador(es) con 2 o más devoluciones.");
    }

    private TablaReporte pagos(List<ReporteDAO.Pago> datos) {
        List<List<String>> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ReporteDAO.Pago p : datos) {
            filas.add(List.of(nvl(p.referencia()), nvl(p.reclamacion()), p.prestador(), Formatos.fecha(p.fechaPago()),
                    Formatos.moneda(nz(p.monto()))));
            total = total.add(nz(p.monto()));
        }
        return new TablaReporte(Reporte.PAGOS,
                List.of("Referencia / cheque", "Reclamación", "Prestador", "Fecha de pago", "Monto"), filas,
                "Total pagado: " + Formatos.moneda(total));
    }

    private TablaReporte precertificaciones(List<ReporteDAO.ConteoPrecertificacion> datos) {
        List<List<String>> filas = new ArrayList<>();
        long total = 0;
        for (ReporteDAO.ConteoPrecertificacion c : datos) {
            filas.add(List.of(c.estado(), String.valueOf(c.cantidad())));
            total += c.cantidad();
        }
        return new TablaReporte(Reporte.PRECERTIFICACIONES, List.of("Estado", "Cantidad"), filas,
                "Total: " + total + " precertificaciones.");
    }

    private TablaReporte actividad(List<ReporteDAO.ActividadUsuario> datos) {
        List<List<String>> filas = new ArrayList<>();
        for (ReporteDAO.ActividadUsuario a : datos) {
            filas.add(List.of(a.usuario(), a.modulo(), String.valueOf(a.acciones()),
                    a.ultima() == null ? "" : FECHA_HORA.format(a.ultima())));
        }
        return new TablaReporte(Reporte.ACTIVIDAD, List.of("Usuario", "Módulo", "Acciones", "Última actividad"),
                filas, null);
    }

    // ------------------------------------------------------------- Utilidades

    /** Promedio legible ("2 h 15 min", "1 d 3 h", "45 min") o "-" si no hay decisiones. */
    public static String duracionPromedio(long segundosTotales, long cantidad) {
        if (cantidad <= 0) {
            return "-";
        }
        long minutos = Math.round(segundosTotales / (double) cantidad / 60.0);
        if (minutos < 60) {
            return minutos + " min";
        }
        long horas = minutos / 60;
        if (horas < 24) {
            return horas + " h " + (minutos % 60) + " min";
        }
        return (horas / 24) + " d " + (horas % 24) + " h";
    }

    private static String porcentaje(long parte, long total) {
        if (total == 0) {
            return "0,0 %";
        }
        return String.format(Locale.forLanguageTag("es-DO"), "%.1f %%", parte * 100.0 / total);
    }

    private static String etiquetaEstado(String estado) {
        try {
            return EstadoAutorizacion.valueOf(estado).etiqueta();
        } catch (IllegalArgumentException | NullPointerException e) {
            return String.valueOf(estado);
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    // ------------------------------------------------------------------ CSV

    /** CSV para Excel en español: BOM UTF-8, separador ";", campos con ; " o saltos de línea entre comillas. */
    public String aCsv(TablaReporte tabla) {
        StringBuilder sb = new StringBuilder(BOM);
        agregarFila(sb, tabla.columnas());
        for (List<String> fila : tabla.filas()) {
            agregarFila(sb, fila);
        }
        return sb.toString();
    }

    public void exportarCsv(TablaReporte tabla, Path destino) {
        try {
            Files.write(destino, aCsv(tabla).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ServicioException("No se pudo guardar el archivo. Verifica que la ruta exista y no esté en uso.", e);
        }
    }

    /** Nombre de archivo sugerido, por ejemplo reporte_autorizaciones_2026-10-10.csv */
    public static String nombreArchivo(Reporte reporte) {
        return "reporte_" + reporte.name().toLowerCase() + "_" + LocalDate.now() + ".csv";
    }

    // ------------------------------------------------------------------ PDF

    /** Nombre de archivo PDF sugerido, por ejemplo reporte_autorizaciones_2026-10-10.pdf */
    public static String nombreArchivoPdf(Reporte reporte) {
        return nombreArchivo(reporte).replace(".csv", ".pdf");
    }

    /**
     * PDF del reporte: título, rango de fechas, tabla, resumen y fecha de generación.
     * Usa la fuente Helvetica en codificación CP1252, que cubre tildes y ñ.
     *
     * @param desde inicio del rango (null = sin límite)
     * @param hasta fin del rango (null = sin límite)
     */
    public byte[] aPdf(TablaReporte tabla, LocalDate desde, LocalDate hasta) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, salida);
            doc.open();
            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, BaseFont.CP1252, false, 16, Font.NORMAL, new Color(0x1F, 0x3A, 0x5F));
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, 9, Font.NORMAL, Color.DARK_GRAY);
            Font cabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, BaseFont.CP1252, false, 9, Font.NORMAL, Color.WHITE);

            doc.add(new Paragraph("ARS VitaSalud - " + tabla.reporte().titulo(), titulo));
            String rango = "Período: " + (desde == null ? "sin límite" : Formatos.fecha(desde))
                    + " al " + (hasta == null ? "sin límite" : Formatos.fecha(hasta));
            doc.add(new Paragraph(rango, normal));
            doc.add(new Paragraph("Generado el " + LocalDateTime.now().format(FECHA_HORA), normal));
            doc.add(new Paragraph(" ", normal));

            int columnas = Math.max(1, tabla.columnas().size());
            PdfPTable t = new PdfPTable(columnas);
            t.setWidthPercentage(100);
            t.setHeaderRows(1);
            for (String c : tabla.columnas()) {
                PdfPCell celda = new PdfPCell(new Phrase(c == null ? "" : c, cabecera));
                celda.setBackgroundColor(new Color(0x1F, 0x3A, 0x5F));
                celda.setPadding(4);
                t.addCell(celda);
            }
            if (tabla.filas().isEmpty()) {
                PdfPCell vacia = new PdfPCell(new Phrase("Sin datos para el período seleccionado.", normal));
                vacia.setColspan(columnas);
                vacia.setPadding(6);
                t.addCell(vacia);
            }
            for (List<String> fila : tabla.filas()) {
                for (int i = 0; i < columnas; i++) {
                    String valor = i < fila.size() && fila.get(i) != null ? fila.get(i) : "";
                    PdfPCell celda = new PdfPCell(new Phrase(valor, normal));
                    celda.setPadding(4);
                    celda.setHorizontalAlignment(Element.ALIGN_LEFT);
                    t.addCell(celda);
                }
            }
            doc.add(t);
            if (tabla.resumen() != null && !tabla.resumen().isBlank()) {
                Paragraph resumen = new Paragraph(tabla.resumen(), normal);
                resumen.setSpacingBefore(10);
                doc.add(resumen);
            }
        } catch (DocumentException e) {
            throw new ServicioException("No se pudo generar el PDF del reporte.", e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
        return salida.toByteArray();
    }

    public void exportarPdf(TablaReporte tabla, LocalDate desde, LocalDate hasta, Path destino) {
        byte[] pdf = aPdf(tabla, desde, hasta);
        try {
            Files.write(destino, pdf);
        } catch (IOException e) {
            throw new ServicioException("No se pudo guardar el archivo. Verifica que la ruta exista y no esté en uso.", e);
        }
    }

    private static void agregarFila(StringBuilder sb, List<String> celdas) {
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) {
                sb.append(SEPARADOR_CSV);
            }
            sb.append(escapar(celdas.get(i)));
        }
        sb.append("\r\n");
    }

    static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        boolean comillas = valor.indexOf(SEPARADOR_CSV) >= 0 || valor.indexOf('"') >= 0
                || valor.indexOf('\n') >= 0 || valor.indexOf('\r') >= 0;
        return comillas ? '"' + valor.replace("\"", "\"\"") + '"' : valor;
    }
}
