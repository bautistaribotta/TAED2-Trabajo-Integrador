package tpi.caso2.consola;

import tpi.caso2.logica.ArchivoDanadoException;
import tpi.caso2.logica.ArchivosDePrueba;
import tpi.caso2.logica.CompresorHuffman;
import tpi.caso2.logica.GestorArchivos;
import tpi.caso2.logica.ResultadoCompresion;

import java.util.Random;

/**
 * Pruebas adicionales del Caso 2: archivos dañados de distintas formas (además del
 * truncado de T6) y archivos válidos poco comunes.
 *
 * Todo se hace en memoria. Los archivos dañados se arman a partir de un comprimido válido
 * cambiando un byte o un bit en una posición conocida, así se sabe qué validación tiene que
 * fallar. Para cada prueba se muestra qué se esperaba y qué devolvió realmente el programa.
 */
final class PruebasAdicionalesCaso2 {

    private int numero;
    private int correctas;

    /** Ejecuta todas las pruebas e imprime el resultado de cada una y un resumen. */
    void ejecutar() {
        System.out.println("===== PRUEBAS ADICIONALES DEL CASO 2 =====");
        byte[] t3 = CompresorHuffman.comprimir(ArchivosDePrueba.t3TodosLosBytes()).getDatos();
        byte[] t4 = CompresorHuffman.comprimir(ArchivosDePrueba.t4MuestraMensajes()).getDatos();
        byte[] t5 = CompresorHuffman.comprimir(ArchivosDePrueba.t5Binario()).getDatos();
        System.out.println("Archivos dañados a partir de T4 comprimido (" + VistaCaso2.bytes(t4.length)
                + "), T3 comprimido (" + VistaCaso2.bytes(t3.length) + ") y T5 comprimido ("
                + VistaCaso2.bytes(t5.length) + ").");
        System.out.println("En todos los rechazos no se genera ningún archivo de salida.");

        // ---- Archivos dañados ----
        byte[] dato = t4.clone();
        dato[0] = 'X';
        rechazo("Firma cambiada (primer byte de THUF reemplazado por X)", dato,
                "Formato no reconocido: el archivo no fue generado por este programa");

        rechazo("Archivo que no es comprimido (T4 original, sin comprimir)", ArchivosDePrueba.t4MuestraMensajes(),
                "Formato no reconocido: el archivo no fue generado por este programa");

        rechazo("Archivo de 10 bytes (más corto que la cabecera)", copia(t4, 10),
                "El archivo es demasiado corto para ser un archivo comprimido");

        dato = t4.clone();
        dato[4] = 2;
        rechazo("Versión del formato cambiada a 2", dato, "Versión de formato no soportada: 2");

        dato = t4.clone();
        dato[9] = 1;   // bytes 5 a 12: tamaño original; el byte 9 vale 2^24, así que pasa a 2^24 + 39
        rechazo("Tamaño original de la cabecera agrandado (39 B -> 16.777.255 B)", dato,
                "El archivo está truncado o dañado");

        dato = t4.clone();
        dato[FormatoHufPosiciones.PRIMER_BYTE_DE_BITS] ^= (byte) 0x80;
        rechazo("Árbol dañado (primer bit después de la cabecera invertido)", dato,
                "El árbol de códigos está dañado");

        // Con este bit cambiado la lectura de los códigos igual termina justo al final: se
        // reconstruyen 39 bytes, pero alguno es distinto. Solo lo detecta la suma Adler-32.
        dato = t4.clone();
        dato[dato.length - 5] ^= 0x10;
        rechazo("Un bit cambiado en los datos de T4 (solo lo detecta Adler-32)", dato,
                "La suma de verificación no coincide: el archivo está dañado");

        // En cambio, en T5 este bit desfasa la lectura de los códigos y los bits se terminan
        // antes de reconstruir todos los bytes.
        dato = t5.clone();
        dato[dato.length / 2] ^= 0x10;
        rechazo("Un bit cambiado en medio de los datos de T5 (desfasa la lectura)", dato,
                "El archivo está truncado: faltan datos comprimidos");

        rechazo("Un byte de más al final (T4 comprimido + 1 byte)", copia(t4, t4.length + 1),
                "El archivo tiene datos de más o un relleno inválido");

        dato = t3.clone();
        dato[dato.length - 1] |= 1;
        rechazo("Relleno final distinto de cero (último bit de T3 comprimido)", dato,
                "El archivo tiene datos de más o un relleno inválido");

        rechazo("Archivo truncado: falta el último byte de T4 comprimido", copia(t4, t4.length - 1),
                "El archivo está truncado: faltan datos comprimidos");

        // ---- Archivos válidos poco comunes ----
        idaYVuelta("Archivo de un solo byte (200)", new byte[] {(byte) 200}, "21 B");
        idaYVuelta("Dos símbolos distintos (0, 255, 0, 0)", new byte[] {0, (byte) 255, 0, 0}, "22 B");
        byte[] aleatorio = new byte[100_000];
        new Random(1).nextBytes(aleatorio);
        idaYVuelta("100.000 bytes aleatorios (sin redundancia)", aleatorio, "100.339 B");

        System.out.println();
        System.out.println(correctas + " de " + numero + " pruebas adicionales correctas.");
    }

    /** Posiciones fijas del formato .huf que usan las pruebas. */
    private static final class FormatoHufPosiciones {
        static final int PRIMER_BYTE_DE_BITS = 19;   // después de la cabecera de 19 bytes
    }

    /** Intenta descomprimir un archivo dañado y comprueba que se rechace con el mensaje esperado. */
    private void rechazo(String nombre, byte[] datos, String mensajeEsperado) {
        String obtenido;
        try {
            CompresorHuffman.descomprimir(datos);
            obtenido = "se descomprimió sin error";
        } catch (ArchivoDanadoException e) {
            obtenido = "rechazado: " + e.getMessage();
        }
        mostrar(nombre, "rechazado: " + mensajeEsperado, obtenido);
    }

    /** Comprime, descomprime y compara; comprueba el tamaño comprimido esperado. */
    private void idaYVuelta(String nombre, byte[] original, String tamanioEsperado) {
        ResultadoCompresion r = CompresorHuffman.comprimir(original);
        byte[] restaurado = CompresorHuffman.descomprimir(r.getDatos()).getDatos();
        boolean iguales = GestorArchivos.comparar(original, restaurado).sonIguales();
        mostrar(nombre,
                "comprimido " + tamanioEsperado + " | restaurado IGUAL",
                "comprimido " + VistaCaso2.bytes(r.getTamanioComprimido()) + " ("
                        + VistaCaso2.variacion(r.getPorcentaje()) + ") | restaurado " + (iguales ? "IGUAL" : "DISTINTO"),
                "comprimido " + VistaCaso2.bytes(r.getTamanioComprimido()) + " | restaurado " + (iguales ? "IGUAL" : "DISTINTO"));
    }

    private void mostrar(String nombre, String esperado, String obtenido) {
        mostrar(nombre, esperado, obtenido, obtenido);
    }

    /**
     * Imprime una prueba. "comparable" es la parte de lo obtenido que se compara con lo
     * esperado; "obtenido" puede agregar información (como el porcentaje) que no se compara.
     */
    private void mostrar(String nombre, String esperado, String obtenido, String comparable) {
        numero++;
        boolean ok = esperado.equals(comparable);
        if (ok) {
            correctas++;
        }
        System.out.println();
        System.out.println("[" + numero + "] " + nombre);
        System.out.println("    Esperado: " + esperado);
        System.out.println("    Obtenido: " + obtenido);
        System.out.println("    " + (ok ? "CORRECTA" : "INCORRECTA"));
    }

    private static byte[] copia(byte[] datos, int largo) {
        byte[] resultado = new byte[largo];
        for (int i = 0; i < largo && i < datos.length; i++) {
            resultado[i] = datos[i];
        }
        return resultado;
    }
}
