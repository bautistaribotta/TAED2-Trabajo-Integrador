package tpi.caso2.logica;

/**
 * Suma de verificación Adler-32, implementada por el equipo.
 *
 * Sirve para detectar si un archivo cambió: se calcula un número de 32 bits a partir del
 * contenido original y se guarda en la cabecera del archivo comprimido. Al descomprimir se
 * vuelve a calcular sobre lo reconstruido; si no coincide, el archivo se dañó en algún lado.
 *
 * Funciona con dos sumas módulo 65521 (el mayor número primo menor que 65536):
 * - a: 1 más la suma de todos los bytes;
 * - b: la suma de los valores que fue tomando a después de cada byte.
 *
 * La suma a detecta un byte cambiado; la suma b además depende de la posición de cada
 * byte, así que también detecta bytes intercambiados de lugar. El resultado es b·65536 + a.
 *
 * Detecta errores accidentales (bits cambiados, bytes faltantes o de más), no
 * modificaciones hechas a propósito para engañarla. En archivos de pocos cientos de bytes
 * es algo más débil, porque las sumas no llegan a dar la vuelta del módulo.
 */
public final class SumaAdler32 {

    private static final int MODULO = 65521;

    private SumaAdler32() {
        // Clase de utilidad: no se crean objetos.
    }

    /**
     * Calcula la suma Adler-32 de los datos. Complejidad O(N).
     *
     * @return un valor entre 0 y 2^32 - 1, guardado en un long para que no tenga signo
     */
    public static long calcular(byte[] datos) {
        long a = 1;
        long b = 0;
        for (byte x : datos) {
            a = (a + (x & 0xFF)) % MODULO;
            b = (b + a) % MODULO;
        }
        return (b << 16) | a;
    }
}
