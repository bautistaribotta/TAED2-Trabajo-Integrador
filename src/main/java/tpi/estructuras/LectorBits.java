package tpi.estructuras;

/**
 * Lee de a un bit un arreglo de bytes. Es la operación inversa de EscritorBits.
 *
 * Mantiene una posición formada por dos números: en qué byte está (posByte) y qué bit de
 * ese byte toca leer (posBit, de 0 a 7, donde 0 es el bit más significativo). Leer un bit
 * es mirar ese bit y avanzar una posición; al pasar el bit 7 se salta al byte siguiente.
 *
 *     byte:     10100000
 *     posBit:   01234567
 *
 * El lector no sabe qué significan los bits: solo los entrega en orden. Si se piden más
 * bits de los que hay, lanza IllegalStateException; quien lo usa decide qué significa eso
 * (para el descompresor, que el archivo está truncado).
 */
public class LectorBits {

    private final byte[] datos;
    private int posByte;   // byte que se está leyendo
    private int posBit;    // próximo bit a leer dentro de ese byte (0 = el de más a la izquierda)

    /**
     * @param datos     arreglo a leer (no se copia ni se modifica)
     * @param desdeByte posición del primer byte a leer; los anteriores se ignoran
     */
    public LectorBits(byte[] datos, int desdeByte) {
        if (desdeByte < 0 || desdeByte > datos.length) {
            throw new IllegalArgumentException("Posición inicial fuera del arreglo");
        }
        this.datos = datos;
        this.posByte = desdeByte;
        this.posBit = 0;
    }

    /**
     * Lee el próximo bit. Complejidad O(1).
     *
     * @return 0 o 1
     * @throws IllegalStateException si ya no quedan bits
     */
    public int leerBit() {
        if (posByte >= datos.length) {
            throw new IllegalStateException("No quedan bits para leer");
        }
        // Se corre el byte para dejar el bit buscado en la posición 0 y se descartan los demás.
        int bit = (datos[posByte] >>> (7 - posBit)) & 1;
        posBit++;
        if (posBit == 8) {
            posBit = 0;
            posByte++;
        }
        return bit;
    }

    /**
     * Lee los próximos 8 bits y los devuelve como un número entre 0 y 255.
     *
     * @throws IllegalStateException si no quedan 8 bits
     */
    public int leerByte() {
        if (bitsRestantes() < 8) {
            throw new IllegalStateException("No quedan bits para leer");
        }
        int valor = 0;
        for (int i = 0; i < 8; i++) {
            valor = (valor << 1) | leerBit();
        }
        return valor;
    }

    /** Cantidad de bits que todavía no se leyeron, hasta el final del arreglo. */
    public long bitsRestantes() {
        return (long) (datos.length - posByte) * 8 - posBit;
    }

    /**
     * Cantidad de bits que faltan para terminar el byte actual: 0 si la lectura está justo
     * al principio de un byte. Sirve para saber cuántos bits de relleno hay al final.
     */
    public int bitsDeRellenoPendientes() {
        return posBit == 0 ? 0 : 8 - posBit;
    }
}
