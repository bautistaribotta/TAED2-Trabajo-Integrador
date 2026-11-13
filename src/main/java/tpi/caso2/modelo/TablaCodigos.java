package tpi.caso2.modelo;

import tpi.estructuras.ArbolHuffman;

/**
 * El código de Huffman de cada byte, listo para comprimir.
 *
 * Recorrer el árbol desde la raíz por cada byte del archivo sería lento; por eso los
 * códigos se calculan una sola vez y se guardan en dos arreglos indexados por el byte:
 * - bits[s]: los bits del código, alineados a la derecha dentro de un long;
 * - longitudes[s]: cuántos bits tiene el código (0 si el byte no aparece).
 *
 * Por ejemplo, si el código de 'A' es 11010, entonces bits[65] = 26 (11010 en binario)
 * y longitudes[65] = 5. La longitud hace falta porque 01 y 1 valen lo mismo como número
 * pero son códigos distintos.
 *
 * Un long alcanza: con archivos de hasta unos 2.147 millones de bytes, ningún código de
 * Huffman puede pasar de 44 bits (ver el plan del Caso 2, sección 2.3).
 */
public class TablaCodigos {

    private final long[] bits = new long[ArbolHuffman.SIMBOLOS];
    private final int[] longitudes = new int[ArbolHuffman.SIMBOLOS];

    private TablaCodigos() {
    }

    /** Calcula los códigos de todos los símbolos del árbol. */
    public static TablaCodigos desde(ArbolHuffman arbol) {
        TablaCodigos tabla = new TablaCodigos();
        arbol.generarCodigos(tabla.bits, tabla.longitudes);
        return tabla;
    }

    /** Bits del código del símbolo, alineados a la derecha. */
    public long bits(int simbolo) {
        return bits[simbolo];
    }

    /** Cantidad de bits del código del símbolo; 0 si el símbolo no aparece. */
    public int longitud(int simbolo) {
        return longitudes[simbolo];
    }

    /** El código como texto de ceros y unos (por ejemplo "11010"), solo para mostrarlo en pantalla. */
    public String comoTexto(int simbolo) {
        StringBuilder texto = new StringBuilder();
        for (int i = longitudes[simbolo] - 1; i >= 0; i--) {
            texto.append((bits[simbolo] >>> i) & 1);
        }
        return texto.toString();
    }

    /**
     * Cantidad de bits que ocupan los datos comprimidos: la suma, para cada símbolo, de su
     * frecuencia por la longitud de su código.
     */
    public long bitsDeDatos(TablaFrecuencias frecuencias) {
        long total = 0;
        for (int s = 0; s < ArbolHuffman.SIMBOLOS; s++) {
            total += frecuencias.frecuencia(s) * longitudes[s];
        }
        return total;
    }
}
