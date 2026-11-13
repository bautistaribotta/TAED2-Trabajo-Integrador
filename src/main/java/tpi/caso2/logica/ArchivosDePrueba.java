package tpi.caso2.logica;

import tpi.estructuras.ListaEnlazada;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Genera el contenido de los archivos de prueba T1 a T6 que pide el enunciado.
 *
 * Los contenidos se generan siempre igual (T5 usa una semilla fija), así las pruebas son
 * reproducibles: cualquiera que ejecute el programa obtiene exactamente los mismos bytes.
 * Por eso no hace falta subir los archivos al repositorio.
 *
 *   T1  0 bytes                                   formato y reconstrucción vacía
 *   T2  10.000 veces el byte 65 ('A')             caso de símbolo único
 *   T3  los valores 0 a 255, una vez cada uno     distribución uniforme, sin repeticiones
 *   T4  la muestra de mensajes del enunciado      costo de cabecera y frecuencias
 *   T5  102.400 bytes de registros binarios        integridad y medición de tamaño
 *   T6  T5 comprimido y cortado a la mitad         detección de error
 */
public final class ArchivosDePrueba {

    /** Nombres de archivo de T1 a T6, en ese orden. */
    public static final String[] NOMBRES = {
            "T1-vacio.bin",
            "T2-simbolo-unico.bin",
            "T3-todos-los-bytes.bin",
            "T4-muestra.txt",
            "T5-binario.bin",
            "T6-truncado.huf",
    };

    /** Descripción breve de cada prueba, en el mismo orden que NOMBRES. */
    public static final String[] DESCRIPCIONES = {
            "0 bytes",
            "10.000 veces el byte 65 ('A')",
            "los valores 0 a 255, una vez cada uno",
            "muestra de mensajes del enunciado (39 bytes)",
            "102.400 bytes de registros binarios de sensores",
            "T5 comprimido y cortado a la mitad",
    };

    /** Muestra de mensajes del enunciado, en UTF-8 y sin salto de línea final. */
    public static final String MUESTRA = "OK|OK|OK|ERROR|OK|OK|ALERTA|OK|OK|ERROR";

    /** Semilla fija del generador de T5: con la misma semilla salen siempre los mismos bytes. */
    private static final long SEMILLA_T5 = 2025;
    private static final int REGISTROS_T5 = 6400;
    private static final int BYTES_POR_REGISTRO = 16;

    private ArchivosDePrueba() {
        // Clase de utilidad: no se crean objetos.
    }

    /**
     * Contenido de la prueba número n (1 a 6).
     */
    public static byte[] contenido(int n) {
        return switch (n) {
            case 1 -> t1Vacio();
            case 2 -> t2SimboloUnico();
            case 3 -> t3TodosLosBytes();
            case 4 -> t4MuestraMensajes();
            case 5 -> t5Binario();
            case 6 -> t6Truncado();
            default -> throw new IllegalArgumentException("Las pruebas van de 1 a 6");
        };
    }

    public static byte[] t1Vacio() {
        return new byte[0];
    }

    public static byte[] t2SimboloUnico() {
        byte[] datos = new byte[10000];
        for (int i = 0; i < datos.length; i++) {
            datos[i] = 65;
        }
        return datos;
    }

    public static byte[] t3TodosLosBytes() {
        byte[] datos = new byte[256];
        for (int i = 0; i < datos.length; i++) {
            datos[i] = (byte) i;   // de 128 a 255 se guardan como negativos, pero son esos valores
        }
        return datos;
    }

    public static byte[] t4MuestraMensajes() {
        return MUESTRA.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 6.400 registros binarios de 16 bytes que simulan los reportes de equipos en campo.
     * Cada registro tiene, en big-endian:
     *
     *   bytes 0-3    id del equipo: uno de 20 equipos (1000 a 1019)
     *   byte  4      estado: 0 = OK (85 %), 1 = ALERTA (10 %), 2 = ERROR (5 %)
     *   bytes 5-6    temperatura en décimas de grado, que varía de a poco
     *   bytes 7-10   marca de tiempo en segundos, creciente (cada 60 a 64 segundos)
     *   bytes 11-12  tensión de la batería en milivoltios, cerca de 3.300
     *   byte  13     nivel de señal, de 60 a 79
     *   bytes 14-15  número de secuencia del registro
     *
     * Como en los archivos reales, hay mucha repetición (los ids, el estado OK, los bytes
     * altos de los números) mezclada con partes que cambian en cada registro.
     */
    public static byte[] t5Binario() {
        Random azar = new Random(SEMILLA_T5);
        byte[] datos = new byte[REGISTROS_T5 * BYTES_POR_REGISTRO];
        int temperatura = 215;          // 21,5 grados
        long tiempo = 1_700_000_000L;
        int pos = 0;
        for (int i = 0; i < REGISTROS_T5; i++) {
            int equipo = 1000 + azar.nextInt(20);
            int sorteo = azar.nextInt(100);
            int estado = sorteo < 85 ? 0 : (sorteo < 95 ? 1 : 2);
            temperatura = Math.max(150, Math.min(400, temperatura + azar.nextInt(7) - 3));
            tiempo += 60 + azar.nextInt(5);
            int tension = 3300 + azar.nextInt(41) - 20;
            int senal = 60 + azar.nextInt(20);

            pos = escribirEntero(datos, pos, equipo, 4);
            pos = escribirEntero(datos, pos, estado, 1);
            pos = escribirEntero(datos, pos, temperatura, 2);
            pos = escribirEntero(datos, pos, tiempo, 4);
            pos = escribirEntero(datos, pos, tension, 2);
            pos = escribirEntero(datos, pos, senal, 1);
            pos = escribirEntero(datos, pos, i, 2);
        }
        return datos;
    }

    /** T5 comprimido y cortado a la mitad de su longitud: un archivo comprimido truncado. */
    public static byte[] t6Truncado() {
        byte[] comprimido = CompresorHuffman.comprimir(t5Binario()).getDatos();
        byte[] mitad = new byte[comprimido.length / 2];
        for (int i = 0; i < mitad.length; i++) {
            mitad[i] = comprimido[i];
        }
        return mitad;
    }

    /**
     * Escribe T1 a T6 en la carpeta indicada (la crea si no existe).
     *
     * @return las rutas absolutas de los archivos generados
     */
    public static ListaEnlazada<String> generarEn(String carpeta) {
        GestorArchivos.crearCarpeta(carpeta);
        ListaEnlazada<String> rutas = new ListaEnlazada<>();
        for (int n = 1; n <= NOMBRES.length; n++) {
            String ruta = carpeta + "/" + NOMBRES[n - 1];
            GestorArchivos.escribir(ruta, contenido(n));
            rutas.agregarAlFinal(GestorArchivos.rutaAbsoluta(ruta));
        }
        return rutas;
    }

    /** Escribe "cantidad" bytes de valor en big-endian a partir de pos y devuelve la posición siguiente. */
    private static int escribirEntero(byte[] datos, int pos, long valor, int cantidad) {
        for (int i = cantidad - 1; i >= 0; i--) {
            datos[pos] = (byte) (valor >>> (8 * i));
            pos++;
        }
        return pos;
    }
}
