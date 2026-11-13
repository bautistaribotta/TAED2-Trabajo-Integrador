package tpi.caso2.logica;

/**
 * Se lanza al descomprimir cuando el archivo no tiene el formato esperado o está dañado:
 * firma desconocida, cabecera inválida, datos truncados o de más, árbol inválido, suma de
 * verificación distinta, etc.
 *
 * El mensaje explica qué validación falló y es el que se le muestra al usuario. Cuando se
 * lanza, no se generó ningún archivo de salida.
 */
public class ArchivoDanadoException extends RuntimeException {

    public ArchivoDanadoException(String mensaje) {
        super(mensaje);
    }
}
