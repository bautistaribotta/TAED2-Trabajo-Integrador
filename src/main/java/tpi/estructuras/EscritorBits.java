package tpi.estructuras;

/**
 * Escribe una secuencia de bits sueltos dentro de un arreglo de bytes, de a 8 bits por byte.
 *
 * Los códigos de Huffman tienen longitud variable (2 bits, 5 bits, 11 bits...), pero un
 * archivo solo se puede escribir de a bytes enteros. Esta clase hace de puente: va juntando
 * los bits en un acumulador y, cada vez que completa 8, guarda ese byte en el arreglo.
 *
 * Los bits se guardan como bits reales, no como los caracteres "0" y "1": escribir el
 * texto "0" ocuparía un byte entero (8 bits) para guardar un solo bit.
 *
 * Orden de los bits: el primer bit escrito ocupa la posición más significativa del byte
 * (la de más a la izquierda). Por ejemplo, escribir 1, 0, 1 y terminar produce el byte
 * 10100000: los 5 bits que faltaban se completan con ceros (relleno).
 *
 * Como el tamaño final se puede calcular antes de empezar, el arreglo se crea una sola vez
 * con la capacidad exacta y no necesita crecer.
 */
public class EscritorBits {

    private final byte[] buffer;   // bytes ya completos
    private int posicion;          // primer byte libre de buffer
    private int acumulador;        // bits del byte que se está armando, alineados a la derecha
    private int bitsEnAcumulador;  // cuántos bits tiene el acumulador (0 a 7)
    private long bitsEscritos;     // total de bits escritos desde el principio

    /**
     * @param capacidadEnBytes cantidad de bytes que va a ocupar todo lo que se escriba,
     *                         contando el último byte con relleno
     */
    public EscritorBits(int capacidadEnBytes) {
        if (capacidadEnBytes < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        this.buffer = new byte[capacidadEnBytes];
        this.posicion = 0;
        this.acumulador = 0;
        this.bitsEnAcumulador = 0;
        this.bitsEscritos = 0;
    }

    /**
     * Escribe un bit. Complejidad O(1).
     *
     * El acumulador se corre una posición a la izquierda para hacer lugar y el bit nuevo
     * entra por la derecha. Al juntar 8 bits, el byte se guarda y el acumulador se vacía.
     *
     * @param bit 0 o 1
     * @throws IllegalStateException si ya se llenó la capacidad
     */
    public void escribirBit(int bit) {
        if (bit != 0 && bit != 1) {
            throw new IllegalArgumentException("Un bit solo puede valer 0 o 1");
        }
        if (posicion == buffer.length) {
            throw new IllegalStateException("Se superó la capacidad del escritor de bits");
        }
        acumulador = (acumulador << 1) | bit;
        bitsEnAcumulador++;
        bitsEscritos++;
        if (bitsEnAcumulador == 8) {
            buffer[posicion] = (byte) acumulador;
            posicion++;
            acumulador = 0;
            bitsEnAcumulador = 0;
        }
    }

    /**
     * Escribe los "cantidad" bits menos significativos de valor, empezando por el más alto.
     * Por ejemplo, escribirBits(6, 3) escribe 1, 1, 0 (6 en binario es 110).
     *
     * Se usa para escribir un código de Huffman (guardado en un long junto con su longitud)
     * y para escribir números de la cabecera.
     *
     * @param valor    número que contiene los bits a escribir
     * @param cantidad cuántos bits escribir, entre 0 y 64
     */
    public void escribirBits(long valor, int cantidad) {
        if (cantidad < 0 || cantidad > 64) {
            throw new IllegalArgumentException("Se pueden escribir entre 0 y 64 bits por vez");
        }
        for (int i = cantidad - 1; i >= 0; i--) {
            // >>> i lleva el bit i a la posición 0; & 1 descarta todos los demás.
            escribirBit((int) ((valor >>> i) & 1));
        }
    }

    /**
     * Escribe un byte completo (8 bits).
     *
     * @param valor número entre 0 y 255
     */
    public void escribirByte(int valor) {
        escribirBits(valor & 0xFF, 8);
    }

    /** Cantidad total de bits escritos, sin contar el relleno final. */
    public long bitsEscritos() {
        return bitsEscritos;
    }

    /**
     * Termina la escritura: si quedó un byte a medio armar, completa los bits que faltan
     * con ceros a la derecha y lo guarda. Devuelve el arreglo con todo lo escrito.
     *
     * Después de llamarlo no se debe seguir escribiendo.
     */
    public byte[] obtenerBytes() {
        if (bitsEnAcumulador > 0) {
            // Ejemplo: con 3 bits 101 se corre 5 lugares y queda 10100000.
            buffer[posicion] = (byte) (acumulador << (8 - bitsEnAcumulador));
            posicion++;
            acumulador = 0;
            bitsEnAcumulador = 0;
        }
        if (posicion != buffer.length) {
            throw new IllegalStateException("Se escribieron " + posicion + " bytes pero la capacidad era "
                    + buffer.length + ": el tamaño calculado de antemano no coincide");
        }
        return buffer;
    }
}
