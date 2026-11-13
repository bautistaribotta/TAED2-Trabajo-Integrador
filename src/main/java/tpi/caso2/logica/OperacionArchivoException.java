package tpi.caso2.logica;

/**
 * Se lanza cuando falla una operación con el disco: el archivo no existe, la ruta es una
 * carpeta, no hay permiso, no se pudo escribir, el archivo es demasiado grande, etc.
 *
 * El mensaje está pensado para mostrárselo al usuario tal cual. La causa original (la
 * excepción de Java) se conserva por si hace falta para depurar.
 */
public class OperacionArchivoException extends RuntimeException {

    public OperacionArchivoException(String mensaje) {
        super(mensaje);
    }

    public OperacionArchivoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
