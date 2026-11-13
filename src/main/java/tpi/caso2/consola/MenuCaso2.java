package tpi.caso2.consola;

import tpi.consola.Entrada;

/**
 * Menú del Caso 2: compresión y descompresión de archivos con Huffman.
 *
 * Por ahora solo tiene la opción de volver; las opciones se agregan en la Fase 7 del plan.
 */
public class MenuCaso2 {

    private static final String MENU = """

            ===== ARCHIVO DE MENSAJES - CASO 2 =====
             (en construcción)
             0. Volver al menú principal""";

    private final Entrada entrada;

    /**
     * @param entrada lectura del teclado compartida con el resto del programa
     */
    public MenuCaso2(Entrada entrada) {
        this.entrada = entrada;
    }

    /** Muestra el menú del Caso 2 hasta que el usuario elige volver al menú principal. */
    public void iniciar() {
        int opcion;
        do {
            System.out.println(MENU);
            opcion = entrada.leerEntero("Opción: ", 0, 0);
        } while (opcion != 0);
    }
}
