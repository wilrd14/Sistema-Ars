package edu.uasd.vitasalud.service;

/**
 * Error de negocio o de infraestructura con un mensaje ya redactado para el usuario.
 * Los controladores lo capturan y muestran {@code getMessage()} con {@code Alertas.error(...)}.
 */
public class ServicioException extends RuntimeException {

    public ServicioException(String mensaje) {
        super(mensaje);
    }

    public ServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
