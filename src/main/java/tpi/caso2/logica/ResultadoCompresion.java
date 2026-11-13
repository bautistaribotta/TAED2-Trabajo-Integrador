package tpi.caso2.logica;

import tpi.caso2.modelo.TablaCodigos;
import tpi.caso2.modelo.TablaFrecuencias;
import tpi.estructuras.ArbolHuffman;

/**
 * Resultado de comprimir: el archivo .huf completo y todas las medidas que se informan.
 *
 * El tamaño comprimido se descompone así:
 *   19 bytes de cabecera + bits del árbol + bits de los datos + relleno (0 a 7 bits)
 */
public class ResultadoCompresion {

    private final byte[] datos;
    private final long tamanioOriginal;
    private final long bitsArbol;
    private final long bitsDatos;
    private final TablaFrecuencias frecuencias;
    private final TablaCodigos codigos;
    private final ArbolHuffman arbol;
    private final long nanosegundos;

    ResultadoCompresion(byte[] datos, long tamanioOriginal, long bitsArbol, long bitsDatos,
                        TablaFrecuencias frecuencias, TablaCodigos codigos, ArbolHuffman arbol,
                        long nanosegundos) {
        this.datos = datos;
        this.tamanioOriginal = tamanioOriginal;
        this.bitsArbol = bitsArbol;
        this.bitsDatos = bitsDatos;
        this.frecuencias = frecuencias;
        this.codigos = codigos;
        this.arbol = arbol;
        this.nanosegundos = nanosegundos;
    }

    /** El archivo comprimido completo, listo para escribir en disco. */
    public byte[] getDatos() {
        return datos;
    }

    public long getTamanioOriginal() {
        return tamanioOriginal;
    }

    public long getTamanioComprimido() {
        return datos.length;
    }

    /** Bytes fijos de la cabecera: siempre 19. */
    public int getBytesCabecera() {
        return FormatoHuf.TAMANIO_CABECERA;
    }

    /** Bits que ocupa el árbol: 10 por símbolo distinto, menos 1. */
    public long getBitsArbol() {
        return bitsArbol;
    }

    /** Bits que ocupan los datos codificados. */
    public long getBitsDatos() {
        return bitsDatos;
    }

    /** Bits de ceros agregados al final para completar el último byte (0 a 7). */
    public long getBitsRelleno() {
        return (getTamanioComprimido() - FormatoHuf.TAMANIO_CABECERA) * 8 - bitsArbol - bitsDatos;
    }

    public int getSimbolosDistintos() {
        return frecuencias.distintos();
    }

    public TablaFrecuencias getFrecuencias() {
        return frecuencias;
    }

    public TablaCodigos getCodigos() {
        return codigos;
    }

    public ArbolHuffman getArbol() {
        return arbol;
    }

    /**
     * Porcentaje de reducción: (original - comprimido) / original · 100. Es positivo si el
     * archivo se achicó y negativo si creció. Si el original está vacío no se puede dividir:
     * devuelve NaN y la consola muestra "no aplica".
     */
    public double getPorcentaje() {
        if (tamanioOriginal == 0) {
            return Double.NaN;
        }
        return (tamanioOriginal - getTamanioComprimido()) * 100.0 / tamanioOriginal;
    }

    /**
     * Longitud promedio del código, en bits por byte del original (el original usa 8).
     * NaN si el original está vacío.
     */
    public double getBitsPorSimbolo() {
        if (tamanioOriginal == 0) {
            return Double.NaN;
        }
        return (double) bitsDatos / tamanioOriginal;
    }

    /** Tiempo del proceso en memoria, sin contar la lectura ni la escritura en disco. */
    public long getNanosegundos() {
        return nanosegundos;
    }
}
