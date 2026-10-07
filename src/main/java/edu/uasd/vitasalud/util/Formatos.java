package edu.uasd.vitasalud.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos de presentación: moneda RD$ con dos decimales y fechas dd/mm/aaaa (PRD, usabilidad). */
public final class Formatos {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formatos() {
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    public static String moneda(BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        // DecimalFormat no es seguro entre hilos: se crea uno por llamada.
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "RD$ " + df.format(monto);
    }
}
