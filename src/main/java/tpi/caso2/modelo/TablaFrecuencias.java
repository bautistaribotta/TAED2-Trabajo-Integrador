package tpi.caso2.modelo;

import tpi.estructuras.ArbolHuffman;

/**
 * Cuántas veces aparece cada valor de byte (0 a 255) en un archivo.
 *
 * Se guarda en un arreglo de 256 posiciones donde el propio byte es el índice:
 * frecuencias[65] es la cantidad de bytes 65 ('A'). Contar cuesta O(1) por byte, sin
 * buscar ni comparar nada. Se eligió el arreglo frente a un árbol (por ejemplo un AVL de
 * símbolos) porque el alfabeto es fijo y chico: los 256 valores se conocen de antemano.
 *
 * Las frecuencias son long porque en un archivo grande la cuenta de un byte, y sobre todo
 * los productos frecuencia por longitud de código, pueden superar el rango de un int.
 */
public class TablaFrecuencias {

    private final long[] frecuencias;
    private final long total;       // cantidad de bytes contados
    private final int distintos;    // cantidad de valores con frecuencia mayor que 0

    private TablaFrecuencias(long[] frecuencias) {
        this.frecuencias = frecuencias;
        long suma = 0;
        int conteo = 0;
        for (long f : frecuencias) {
            suma += f;
            if (f > 0) {
                conteo++;
            }
        }
        this.total = suma;
        this.distintos = conteo;
    }

    /**
     * Cuenta las apariciones de cada byte. Recorre los datos una sola vez: O(N).
     *
     * En Java el tipo byte tiene signo y va de -128 a 127: el byte 200 se lee como -56.
     * Por eso se usa b & 0xFF, que lo convierte al valor sin signo 0 a 255 y sirve de índice.
     */
    public static TablaFrecuencias contar(byte[] datos) {
        long[] frecuencias = new long[ArbolHuffman.SIMBOLOS];
        for (byte b : datos) {
            frecuencias[b & 0xFF]++;
        }
        return new TablaFrecuencias(frecuencias);
    }

    /** Cantidad de veces que aparece el byte simbolo (0 a 255). */
    public long frecuencia(int simbolo) {
        return frecuencias[simbolo];
    }

    /** Copia del arreglo de frecuencias, para que nadie pueda modificar la tabla desde afuera. */
    public long[] comoArreglo() {
        long[] copia = new long[frecuencias.length];
        for (int i = 0; i < frecuencias.length; i++) {
            copia[i] = frecuencias[i];
        }
        return copia;
    }

    /** Cantidad total de bytes contados. */
    public long total() {
        return total;
    }

    /** Cantidad de valores de byte distintos que aparecen. */
    public int distintos() {
        return distintos;
    }
}
