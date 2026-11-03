package tpi.caso1.logica;

/**
 * Error de regla de negocio (id duplicado, centro inexistente, tiempo negativo, etc.).
 * El mensaje es el que se le muestra al usuario.
 */
public class OperacionInvalidaException extends RuntimeException {

    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
