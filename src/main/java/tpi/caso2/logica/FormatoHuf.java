package tpi.caso2.logica;

import tpi.estructuras.ArbolHuffman;
import tpi.estructuras.EscritorBits;

/**
 * Formato del archivo comprimido (.huf): constantes, escritura y lectura de la cabecera.
 *
 * Disposición del archivo (los enteros se guardan en big-endian, el byte más alto primero):
 *
 *   posición  tamaño   campo
 *   0         4        firma: los caracteres T H U F
 *   4         1        versión del formato: 1
 *   5         8        tamaño del archivo original, en bytes
 *   13        4        suma Adler-32 del contenido original
 *   17        2        cantidad de símbolos distintos (0 a 256)
 *   19        resto    flujo de bits: el árbol en preorden y a continuación los datos
 *                      codificados, sin separación; el último byte se completa con ceros
 *
 * El archivo es autónomo: con solo leerlo se puede reconstruir el original, porque el
 * árbol viaja adentro y el tamaño original dice cuántos símbolos hay que decodificar
 * (hace falta saberlo porque el relleno del último byte no se distingue de los datos).
 */
public final class FormatoHuf {

    /** Primeros 4 bytes de todo archivo generado por este programa: "THUF". */
    public static final byte[] FIRMA = {'T', 'H', 'U', 'F'};

    /** Versión actual del formato. */
    public static final int VERSION = 1;

    /** Bytes fijos antes del flujo de bits. */
    public static final int TAMANIO_CABECERA = 19;

    /**
     * Mayor tamaño de archivo admitido: el máximo de un arreglo de Java, que es un poco
     * menos que Integer.MAX_VALUE. El archivo se procesa entero en memoria.
     */
    public static final long TAMANIO_MAXIMO = Integer.MAX_VALUE - 8;

    /** Datos de la cabecera ya leídos y validados. */
    public static final class Cabecera {
        private final long tamanioOriginal;
        private final long sumaVerificacion;
        private final int simbolosDistintos;

        Cabecera(long tamanioOriginal, long sumaVerificacion, int simbolosDistintos) {
            this.tamanioOriginal = tamanioOriginal;
            this.sumaVerificacion = sumaVerificacion;
            this.simbolosDistintos = simbolosDistintos;
        }

        public long getTamanioOriginal() {
            return tamanioOriginal;
        }

        public long getSumaVerificacion() {
            return sumaVerificacion;
        }

        public int getSimbolosDistintos() {
            return simbolosDistintos;
        }
    }

    private FormatoHuf() {
        // Clase de utilidad: no se crean objetos.
    }

    /** Escribe los 19 bytes de la cabecera. */
    static void escribirCabecera(EscritorBits salida, long tamanioOriginal, long sumaVerificacion,
                                 int simbolosDistintos) {
        for (byte b : FIRMA) {
            salida.escribirByte(b);
        }
        salida.escribirBits(VERSION, 8);
        salida.escribirBits(tamanioOriginal, 64);
        salida.escribirBits(sumaVerificacion, 32);
        salida.escribirBits(simbolosDistintos, 16);
    }

    /**
     * Lee la cabecera y aplica las validaciones 1 a 5 del formato:
     * 1. que el archivo tenga al menos los 19 bytes de la cabecera;
     * 2. que empiece con la firma THUF;
     * 3. que la versión sea 1;
     * 4. que el tamaño original esté entre 0 y el máximo admitido;
     * 5. que la cantidad de símbolos esté entre 0 y 256, que sea 0 solo si el original está
     *    vacío, y que no supere el tamaño original (no puede haber más símbolos distintos
     *    que bytes).
     *
     * @throws ArchivoDanadoException con el motivo, si alguna validación falla
     */
    public static Cabecera leerCabecera(byte[] datos) {
        if (datos.length < TAMANIO_CABECERA) {
            throw new ArchivoDanadoException("El archivo es demasiado corto para ser un archivo comprimido");
        }
        for (int i = 0; i < FIRMA.length; i++) {
            if (datos[i] != FIRMA[i]) {
                throw new ArchivoDanadoException("Formato no reconocido: el archivo no fue generado por este programa");
            }
        }
        int version = datos[4] & 0xFF;
        if (version != VERSION) {
            throw new ArchivoDanadoException("Versión de formato no soportada: " + version);
        }
        long tamanioOriginal = leerEntero(datos, 5, 8);
        if (tamanioOriginal < 0 || tamanioOriginal > TAMANIO_MAXIMO) {
            throw new ArchivoDanadoException("Tamaño original inválido en la cabecera");
        }
        long suma = leerEntero(datos, 13, 4);
        int simbolos = (int) leerEntero(datos, 17, 2);
        boolean vacio = tamanioOriginal == 0;
        boolean fueraDeRango = simbolos > ArbolHuffman.SIMBOLOS;
        boolean noCoincideConVacio = (simbolos == 0) != vacio;
        boolean masSimbolosQueBytes = simbolos > tamanioOriginal;
        if (fueraDeRango || noCoincideConVacio || masSimbolosQueBytes) {
            throw new ArchivoDanadoException("Cantidad de símbolos inválida en la cabecera");
        }
        return new Cabecera(tamanioOriginal, suma, simbolos);
    }

    /**
     * Arma un número sin signo a partir de "cantidad" bytes consecutivos en big-endian:
     * cada byte nuevo se agrega a la derecha corriendo 8 lugares lo que ya había.
     */
    private static long leerEntero(byte[] datos, int desde, int cantidad) {
        long valor = 0;
        for (int i = desde; i < desde + cantidad; i++) {
            valor = (valor << 8) | (datos[i] & 0xFF);
        }
        return valor;
    }
}
