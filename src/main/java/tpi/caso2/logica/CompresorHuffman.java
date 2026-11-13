package tpi.caso2.logica;

import tpi.caso2.modelo.TablaCodigos;
import tpi.caso2.modelo.TablaFrecuencias;
import tpi.estructuras.ArbolHuffman;
import tpi.estructuras.EscritorBits;
import tpi.estructuras.LectorBits;

/**
 * Compresión y descompresión con codificación de Huffman, en memoria.
 *
 * Trabaja solo con arreglos de bytes: no lee ni escribe archivos (de eso se encarga
 * GestorArchivos). Así la medición del tiempo refleja solo el algoritmo, y se puede
 * comprimir y descomprimir sin tocar el disco, como hacen las pruebas obligatorias.
 *
 * El formato del resultado está documentado en FormatoHuf.
 */
public final class CompresorHuffman {

    private CompresorHuffman() {
        // Clase de utilidad: no se crean objetos.
    }

    /**
     * Comprime un contenido cualquiera (texto o binario, de 0 a 255 en cada byte).
     *
     * Pasos:
     * 1. Contar la frecuencia de cada byte.                         O(N)
     * 2. Construir el árbol de Huffman con un montículo de mínimos.  O(n log n), n ≤ 256
     * 3. Calcular el código de cada byte.                           O(n)
     * 4. Calcular el tamaño exacto de salida: 19 + techo((bits del árbol + bits de los
     *    datos) / 8). Así el arreglo se crea una sola vez, sin tener que agrandarlo.
     * 5. Escribir la cabecera, el árbol en preorden y el código de cada byte, en orden.  O(N)
     *
     * @throws OperacionArchivoException si el resultado no entra en un arreglo de Java
     */
    public static ResultadoCompresion comprimir(byte[] original) {
        long inicio = System.nanoTime();

        TablaFrecuencias frecuencias = TablaFrecuencias.contar(original);
        ArbolHuffman arbol = ArbolHuffman.construir(frecuencias.comoArreglo());
        TablaCodigos codigos = TablaCodigos.desde(arbol);

        long bitsArbol = arbol.bitsSerializado();
        long bitsDatos = codigos.bitsDeDatos(frecuencias);
        long bytesDeBits = (bitsArbol + bitsDatos + 7) / 8;   // techo de la división por 8
        long tamanio = FormatoHuf.TAMANIO_CABECERA + bytesDeBits;
        if (tamanio > FormatoHuf.TAMANIO_MAXIMO) {
            throw new OperacionArchivoException("El archivo comprimido superaría el tamaño máximo que se puede manejar en memoria");
        }

        EscritorBits salida = new EscritorBits((int) tamanio);
        FormatoHuf.escribirCabecera(salida, original.length, SumaAdler32.calcular(original), frecuencias.distintos());
        arbol.escribirEnPreorden(salida);
        for (byte b : original) {
            int simbolo = b & 0xFF;
            salida.escribirBits(codigos.bits(simbolo), codigos.longitud(simbolo));
        }
        byte[] comprimido = salida.obtenerBytes();

        long fin = System.nanoTime();
        return new ResultadoCompresion(comprimido, original.length, bitsArbol, bitsDatos,
                frecuencias, codigos, arbol, fin - inicio);
    }

    /**
     * Reconstruye el contenido original a partir de un archivo .huf.
     *
     * Aplica, en orden, las 10 validaciones del plan. Si alguna falla lanza
     * ArchivoDanadoException con el motivo; en ese caso no devuelve nada, así que quien lo
     * llama no puede escribir un archivo parcial ni informar un éxito falso.
     *
     * Validaciones 1 a 5: cabecera (ver FormatoHuf.leerCabecera).
     * 6. Que alcancen los bits: cada símbolo ocupa al menos 1 bit, así que el tamaño original
     *    no puede superar los bits que hay después de la cabecera. Se controla antes de crear
     *    el arreglo de salida, para que una cabecera dañada no pida memoria de más.
     * 7. Que el árbol sea válido (ver ArbolHuffman.leerEnPreorden).
     * 8. Que se puedan decodificar exactamente "tamaño original" símbolos.
     * 9. Que después solo quede el relleno del último byte, todo en ceros.
     * 10. Que la suma Adler-32 de lo reconstruido coincida con la de la cabecera.
     *
     * Complejidad O(bits del archivo): cada bit se lee una sola vez.
     *
     * @throws ArchivoDanadoException si el archivo no es válido
     */
    public static ResultadoDescompresion descomprimir(byte[] comprimido) {
        long inicio = System.nanoTime();

        // 1 a 5
        FormatoHuf.Cabecera cabecera = FormatoHuf.leerCabecera(comprimido);
        long tamanioOriginal = cabecera.getTamanioOriginal();
        int simbolos = cabecera.getSimbolosDistintos();

        if (tamanioOriginal == 0) {
            // Archivo vacío: no hay árbol ni datos, solo la cabecera.
            if (comprimido.length != FormatoHuf.TAMANIO_CABECERA) {
                throw new ArchivoDanadoException("El archivo tiene datos de más o un relleno inválido");
            }
            validarSuma(new byte[0], cabecera);
            return new ResultadoDescompresion(new byte[0], comprimido.length, 0, System.nanoTime() - inicio);
        }

        // 6
        long bitsDisponibles = (long) (comprimido.length - FormatoHuf.TAMANIO_CABECERA) * 8;
        if (tamanioOriginal > bitsDisponibles) {
            throw new ArchivoDanadoException("El archivo está truncado o dañado");
        }

        LectorBits lector = new LectorBits(comprimido, FormatoHuf.TAMANIO_CABECERA);

        // 7
        ArbolHuffman arbol;
        try {
            arbol = ArbolHuffman.leerEnPreorden(lector, simbolos);
        } catch (IllegalStateException e) {
            throw new ArchivoDanadoException("El árbol de códigos está dañado");
        }

        // 8
        byte[] salida = new byte[(int) tamanioOriginal];
        try {
            for (int i = 0; i < salida.length; i++) {
                salida[i] = (byte) arbol.decodificarSimbolo(lector);
            }
        } catch (IllegalStateException e) {
            if (lector.bitsRestantes() == 0) {
                throw new ArchivoDanadoException("El archivo está truncado: faltan datos comprimidos");
            }
            throw new ArchivoDanadoException("Los datos comprimidos están dañados");
        }

        // 9: lo que queda tiene que ser solo el resto del último byte, y en ceros.
        if (lector.bitsRestantes() != lector.bitsDeRellenoPendientes()) {
            throw new ArchivoDanadoException("El archivo tiene datos de más o un relleno inválido");
        }
        while (lector.bitsRestantes() > 0) {
            if (lector.leerBit() != 0) {
                throw new ArchivoDanadoException("El archivo tiene datos de más o un relleno inválido");
            }
        }

        // 10
        validarSuma(salida, cabecera);

        long fin = System.nanoTime();
        return new ResultadoDescompresion(salida, comprimido.length, simbolos, fin - inicio);
    }

    private static void validarSuma(byte[] reconstruido, FormatoHuf.Cabecera cabecera) {
        if (SumaAdler32.calcular(reconstruido) != cabecera.getSumaVerificacion()) {
            throw new ArchivoDanadoException("La suma de verificación no coincide: el archivo está dañado");
        }
    }
}
