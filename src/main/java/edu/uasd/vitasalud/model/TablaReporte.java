package edu.uasd.vitasalud.model;

import java.util.List;

/**
 * Resultado de un reporte ya formateado para mostrar y exportar. Cada fila tiene tantas celdas como columnas.
 * {@code resumen} es una línea de texto opcional (por ejemplo, el tiempo promedio general).
 */
public record TablaReporte(Reporte reporte, List<String> columnas, List<List<String>> filas, String resumen) {
}
