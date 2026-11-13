package tpi.consola;

import tpi.caso1.consola.MenuCaso1;
import tpi.caso2.consola.MenuCaso2;

/**
 * Primer menú del programa: permite elegir el Caso 1, el Caso 2 o salir.
 *
 * Crea una única Entrada (un solo Scanner sobre el teclado) y se la pasa a los menús de
 * los dos casos. También crea cada menú una sola vez: así, si el usuario modifica la red
 * del Caso 1, vuelve a este menú y después entra otra vez al Caso 1, encuentra la red
 * con sus cambios.
 */
public class MenuPrincipal {

    private static final String MENU = """

            ===== TPI - TALLER DE ALGORITMOS Y ESTRUCTURAS DE DATOS II =====
             1. Caso 1: Distribución de insumos entre centros logísticos
             2. Caso 2: Archivo histórico de mensajes de sensores
             0. Salir""";

    private final Entrada entrada;
    private final MenuCaso1 caso1;
    private final MenuCaso2 caso2;

    public MenuPrincipal() {
        this.entrada = new Entrada();
        this.caso1 = new MenuCaso1(entrada);
        this.caso2 = new MenuCaso2(entrada);
    }

    /**
     * Muestra el menú hasta que el usuario elige salir, o hasta que se termina la entrada
     * (por ejemplo, si se cierra la consola o si la entrada viene de un archivo).
     */
    public void iniciar() {
        try {
            int opcion;
            do {
                System.out.println(MENU);
                opcion = entrada.leerEntero("Opción: ", 0, 2);
                switch (opcion) {
                    case 1 -> caso1.iniciar();
                    case 2 -> caso2.iniciar();
                    default -> System.out.println("Fin del programa.");
                }
            } while (opcion != 0);
        } catch (Entrada.EntradaTerminadaException e) {
            System.out.println("\nNo hay más datos de entrada. Fin del programa.");
        }
    }
}
