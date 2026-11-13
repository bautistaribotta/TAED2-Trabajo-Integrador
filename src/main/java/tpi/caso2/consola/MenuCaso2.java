package tpi.caso2.consola;

import tpi.caso2.logica.ArchivoDanadoException;
import tpi.caso2.logica.ArchivosDePrueba;
import tpi.caso2.logica.CompresorHuffman;
import tpi.caso2.logica.GestorArchivos;
import tpi.caso2.logica.OperacionArchivoException;
import tpi.caso2.logica.ResultadoComparacion;
import tpi.caso2.logica.ResultadoCompresion;
import tpi.caso2.logica.ResultadoDescompresion;
import tpi.consola.Entrada;
import tpi.estructuras.ListaEnlazada;

/**
 * Menú del Caso 2: compresión y descompresión de archivos con codificación de Huffman.
 *
 * Esta clase solo pide rutas, llama a la lógica y muestra los resultados con VistaCaso2.
 * Los errores llegan como excepciones con un mensaje para el usuario:
 * - OperacionArchivoException: problemas con el disco (archivo inexistente, sin permiso...);
 * - ArchivoDanadoException: el archivo comprimido no es válido.
 *
 * El programa nunca termina por una excepción: cada acción se ejecuta dentro de un
 * try/catch que informa el error y vuelve al menú.
 */
public class MenuCaso2 {

    private static final String MENU = """

            ===== ARCHIVO DE MENSAJES - CASO 2 =====
             1. Comprimir un archivo
             2. Descomprimir un archivo
             3. Comprimir, restaurar y verificar (ida y vuelta)
             4. Ver frecuencias, códigos y árbol de un archivo
             5. Comparar dos archivos byte a byte
             6. Generar archivos de prueba T1 a T6
             7. Ejecutar pruebas obligatorias
             0. Volver al menú principal""";

    /** Carpeta donde se generan los archivos de prueba si el usuario no indica otra. */
    private static final String CARPETA_PRUEBAS = "pruebas-caso2";

    /** Tamaños comprimidos esperados de T1 a T4, calculados a mano en el plan (Fase 8.3). */
    private static final long[] TAMANIOS_ESPERADOS = {19, 1271, 595, 42};

    private final Entrada entrada;

    /**
     * @param entrada lectura del teclado compartida con el resto del programa
     */
    public MenuCaso2(Entrada entrada) {
        this.entrada = entrada;
    }

    /**
     * Muestra el menú del Caso 2 hasta que el usuario elige volver al menú principal.
     *
     * @throws Entrada.EntradaTerminadaException si se termina la entrada; la atrapa MenuPrincipal
     */
    public void iniciar() {
        int opcion;
        do {
            System.out.println(MENU);
            opcion = entrada.leerEntero("Opción: ", 0, 7);
            switch (opcion) {
                case 1 -> ejecutar(this::comprimir);
                case 2 -> ejecutar(this::descomprimir);
                case 3 -> ejecutar(this::idaYVuelta);
                case 4 -> ejecutar(this::analizar);
                case 5 -> ejecutar(this::comparar);
                case 6 -> ejecutar(this::generarArchivosDePrueba);
                case 7 -> ejecutar(this::ejecutarPruebas);
                default -> {
                    // 0: volver al menú principal
                }
            }
        } while (opcion != 0);
    }

    /**
     * Ejecuta una acción atrapando los errores y después hace una pausa para que el usuario
     * lea el resultado. EntradaTerminadaException se deja pasar porque indica que hay que salir.
     */
    private void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (Entrada.EntradaTerminadaException e) {
            throw e;
        } catch (ArchivoDanadoException e) {
            System.out.println(VistaCaso2.rechazo(e.getMessage()));
        } catch (OperacionArchivoException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error inesperado: " + e);
        }
        entrada.pausa();
    }

    // ------------------------------------------------------------------
    // Opciones
    // ------------------------------------------------------------------

    /** 1. Comprime un archivo y guarda el resultado. */
    private void comprimir() {
        String origen = leerRuta("Archivo a comprimir: ");
        byte[] original = GestorArchivos.leer(origen);   // si no existe, se avisa antes de pedir más datos
        String destino = leerRuta("Archivo comprimido a generar", origen + ".huf");
        if (!puedeEscribir(origen, destino)) {
            return;
        }
        ResultadoCompresion r = CompresorHuffman.comprimir(original);
        GestorArchivos.escribir(destino, r.getDatos());
        System.out.println(VistaCaso2.compresion(origen, GestorArchivos.rutaAbsoluta(destino), r));
    }

    /**
     * 2. Descomprime un archivo .huf. Primero descomprime en memoria y recién si pasaron
     * todas las validaciones pregunta dónde guardar y escribe el resultado: si el archivo
     * está dañado, no se genera nada.
     */
    private void descomprimir() {
        String origen = leerRuta("Archivo comprimido (.huf): ");
        ResultadoDescompresion r = CompresorHuffman.descomprimir(GestorArchivos.leer(origen));
        System.out.println("El archivo es válido y se descomprimió en memoria.");
        String destino = leerRuta("Archivo restaurado a generar", rutaRestaurada(origen));
        if (!puedeEscribir(origen, destino)) {
            return;
        }
        GestorArchivos.escribir(destino, r.getDatos());
        System.out.println(VistaCaso2.descompresion(origen, GestorArchivos.rutaAbsoluta(destino), r));
        if (entrada.leerSiNo("¿Comparar el resultado con el archivo original?")) {
            String original = leerRuta("Archivo original: ");
            System.out.println(VistaCaso2.comparacion(original, destino, GestorArchivos.comparar(original, destino)));
        }
    }

    /**
     * 3. Ciclo completo: comprime, guarda el .huf, lo vuelve a leer desde el disco, lo
     * descomprime, guarda el restaurado y lo compara con el original byte a byte.
     */
    private void idaYVuelta() {
        String origen = leerRuta("Archivo a verificar: ");
        byte[] original = GestorArchivos.leer(origen);
        String comprimido = origen + ".huf";
        String restaurado = origen + ".restaurado";
        if (!puedeEscribir(origen, comprimido) || !puedeEscribir(origen, restaurado)) {
            return;
        }

        ResultadoCompresion rc = CompresorHuffman.comprimir(original);
        GestorArchivos.escribir(comprimido, rc.getDatos());
        System.out.println(VistaCaso2.compresion(origen, GestorArchivos.rutaAbsoluta(comprimido), rc));

        // Se vuelve a leer desde el disco para comprobar el archivo tal como quedó guardado.
        ResultadoDescompresion rd = CompresorHuffman.descomprimir(GestorArchivos.leer(comprimido));
        GestorArchivos.escribir(restaurado, rd.getDatos());
        System.out.println(VistaCaso2.descompresion(comprimido, GestorArchivos.rutaAbsoluta(restaurado), rd));

        System.out.println(VistaCaso2.comparacion(origen, restaurado, GestorArchivos.comparar(origen, restaurado)));
    }

    /** 4. Muestra frecuencias, códigos y árbol sin escribir nada en el disco. */
    private void analizar() {
        String ruta = leerRuta("Archivo a analizar: ");
        ResultadoCompresion r = CompresorHuffman.comprimir(GestorArchivos.leer(ruta));
        System.out.println(VistaCaso2.analisis(ruta, r));
        System.out.println(VistaCaso2.compresion(ruta, null, r));
    }

    /** 5. Compara dos archivos cualesquiera. */
    private void comparar() {
        String a = leerRuta("Primer archivo: ");
        String b = leerRuta("Segundo archivo: ");
        ResultadoComparacion c = GestorArchivos.comparar(a, b);
        System.out.println(VistaCaso2.comparacion(a, b, c));
    }

    /** 6. Escribe T1 a T6 en una carpeta. */
    private void generarArchivosDePrueba() {
        String carpeta = leerRuta("Carpeta donde generarlos", CARPETA_PRUEBAS);
        ListaEnlazada<String> rutas = ArchivosDePrueba.generarEn(carpeta);
        System.out.println("Archivos generados:");
        int n = 0;
        for (String ruta : rutas) {
            System.out.println("  T" + (n + 1) + "  " + ruta + "  (" + ArchivosDePrueba.DESCRIPCIONES[n] + ")");
            n++;
        }
    }

    /**
     * 7. Pruebas obligatorias del enunciado. Trabajan en memoria con los contenidos de T1 a
     * T6, así no dependen de que existan los archivos ni de lo que el usuario haya cambiado.
     * Los resultados se calculan al ejecutar; los tamaños esperados de T1 a T4 están en el
     * plan, calculados a mano, y solo se usan para comparar.
     */
    private void ejecutarPruebas() {
        System.out.println("===== PRUEBAS OBLIGATORIAS DEL CASO 2 =====");
        System.out.printf("%-6s %12s %12s  %-24s %-11s %s%n",
                "Prueba", "Original", "Comprimido", "Resultado", "Restaurado", "Tiempo (comp. + desc.)");
        int correctas = 0;
        for (int n = 1; n <= 5; n++) {
            byte[] original = ArchivosDePrueba.contenido(n);
            ResultadoCompresion rc = CompresorHuffman.comprimir(original);
            ResultadoDescompresion rd = CompresorHuffman.descomprimir(rc.getDatos());
            boolean iguales = GestorArchivos.comparar(original, rd.getDatos()).sonIguales();
            boolean tamanioOk = n <= 4
                    ? rc.getTamanioComprimido() == TAMANIOS_ESPERADOS[n - 1]
                    : rc.getTamanioComprimido() < rc.getTamanioOriginal();
            if (iguales && tamanioOk) {
                correctas++;
            }
            System.out.printf("%-6s %12s %12s  %-24s %-11s %s + %s%s%n",
                    "T" + n,
                    VistaCaso2.bytes(rc.getTamanioOriginal()),
                    VistaCaso2.bytes(rc.getTamanioComprimido()),
                    resumenVariacion(rc.getPorcentaje()),
                    iguales ? "IGUAL" : "DISTINTO",
                    VistaCaso2.milisegundos(rc.getNanosegundos()),
                    VistaCaso2.milisegundos(rd.getNanosegundos()),
                    tamanioOk ? "" : "  <- tamaño inesperado");
        }

        byte[] truncado = ArchivosDePrueba.contenido(6);
        try {
            CompresorHuffman.descomprimir(truncado);
            System.out.printf("%-6s %12s  INCORRECTO: el archivo truncado se descomprimió sin error%n",
                    "T6", VistaCaso2.bytes(truncado.length));
        } catch (ArchivoDanadoException e) {
            correctas++;
            System.out.printf("%-6s %12s  RECHAZADO: %s%n", "T6", VistaCaso2.bytes(truncado.length), e.getMessage());
        }

        System.out.println();
        System.out.println(correctas + " de 6 pruebas correctas.");
        System.out.println("""

                Qué muestra cada prueba:
                  T1  Un archivo vacío ocupa 19 B: solo la cabecera. No hay porcentaje porque el original mide 0.
                  T2  Un solo símbolo usa 1 bit por byte: la reducción tiende al 87,5 %, el límite de Huffman.
                  T3  Sin repeticiones, cada símbolo sigue ocupando 8 bits y además se paga el árbol: el archivo crece.
                  T4  En un archivo chico la cabecera y el árbol pesan más que lo que se ahorra: 39 B pasan a 42 B.
                  T5  Un archivo binario con repeticiones se reduce, y se restaura igual byte a byte.
                  T6  Un archivo truncado se rechaza sin generar salida ni informar un éxito falso.""");
        System.out.println("El análisis detallado de T4 (frecuencias, códigos y árbol) está en la opción 4,");
        System.out.println("sobre el archivo generado con la opción 6.");
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static String resumenVariacion(double porcentaje) {
        if (Double.isNaN(porcentaje)) {
            return "no aplica";
        }
        return VistaCaso2.variacion(porcentaje);
    }

    /**
     * Lee una ruta y le quita las comillas que agrega Windows al usar "Copiar como ruta de
     * acceso" (por ejemplo "C:\datos\archivo.bin").
     */
    private String leerRuta(String mensaje) {
        return sinComillas(entrada.leerTexto(mensaje));
    }

    private String leerRuta(String mensaje, String porDefecto) {
        return sinComillas(entrada.leerTexto(mensaje, porDefecto));
    }

    private static String sinComillas(String ruta) {
        if (ruta.length() >= 2 && ruta.startsWith("\"") && ruta.endsWith("\"")) {
            return ruta.substring(1, ruta.length() - 1).trim();
        }
        return ruta;
    }

    /** Ruta sugerida para el archivo restaurado: se quita ".huf" y se agrega ".restaurado". */
    private static String rutaRestaurada(String rutaComprimida) {
        String base = rutaComprimida.toLowerCase().endsWith(".huf")
                ? rutaComprimida.substring(0, rutaComprimida.length() - 4)
                : rutaComprimida;
        return base + ".restaurado";
    }

    /**
     * Controla que se pueda escribir el destino: que no sea el mismo archivo que el origen
     * y, si ya existe, que el usuario confirme que quiere reemplazarlo.
     */
    private boolean puedeEscribir(String origen, String destino) {
        if (GestorArchivos.mismoArchivo(origen, destino)) {
            System.out.println("El archivo de salida no puede ser el mismo que el de entrada.");
            return false;
        }
        if (GestorArchivos.existe(destino)
                && !entrada.leerSiNo("El archivo " + destino + " ya existe. ¿Reemplazarlo?")) {
            System.out.println("Operación cancelada.");
            return false;
        }
        return true;
    }
}
