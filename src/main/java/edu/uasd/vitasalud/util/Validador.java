package edu.uasd.vitasalud.util;

import java.util.regex.Pattern;

/**
 * Validaciones de formato compartidas por todo el equipo (dueña: Liannelis Pérez).
 * Solo validan la forma del texto; la unicidad (RN-01) se verifica en la capa de servicio.
 */
public final class Validador {

    private static final Pattern CEDULA = Pattern.compile("\\d{3}-\\d{7}-\\d");
    private static final Pattern RNC = Pattern.compile("\\d{9}");
    private static final Pattern TELEFONO = Pattern.compile("\\(\\d{3}\\) \\d{3}-\\d{4}");
    private static final Pattern CORREO = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private Validador() {
    }

    /** Cédula con formato 000-0000000-0. */
    public static boolean esCedula(String valor) {
        return coincide(CEDULA, valor);
    }

    /** RNC de 9 dígitos. */
    public static boolean esRnc(String valor) {
        return coincide(RNC, valor);
    }

    /** Teléfono con formato (000) 000-0000. */
    public static boolean esTelefono(String valor) {
        return coincide(TELEFONO, valor);
    }

    public static boolean esCorreo(String valor) {
        return coincide(CORREO, valor);
    }

    private static boolean coincide(Pattern patron, String valor) {
        return valor != null && patron.matcher(valor.trim()).matches();
    }
}
