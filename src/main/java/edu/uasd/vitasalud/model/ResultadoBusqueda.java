package edu.uasd.vitasalud.model;

/**
 * Un resultado de la búsqueda global (RF-SEG-08).
 *
 * @param tipo     qué clase de registro es
 * @param titulo   texto principal (nombre del afiliado, número de autorización, nombre del prestador)
 * @param detalle  texto de apoyo (documento, estado, tipo...)
 * @param clave    valor que identifica el registro en su pantalla: número de afiliado, número de
 *                 autorización o RNC/cédula del prestador
 */
public record ResultadoBusqueda(Tipo tipo, String titulo, String detalle, String clave) {

    /** Tipos de registro que busca el encabezado; {@code pantallaId} es el id de {@code Menu.Item}. */
    public enum Tipo {
        AFILIADO("Afiliado", "afiliados"),
        AUTORIZACION("Autorización", "bandeja"),
        PRESTADOR("Prestador", "prestadores");

        private final String etiqueta;
        private final String pantallaId;

        Tipo(String etiqueta, String pantallaId) {
            this.etiqueta = etiqueta;
            this.pantallaId = pantallaId;
        }

        public String etiqueta() {
            return etiqueta;
        }

        /** Id de la pantalla donde se muestra este tipo de registro. */
        public String pantallaId() {
            return pantallaId;
        }
    }
}
