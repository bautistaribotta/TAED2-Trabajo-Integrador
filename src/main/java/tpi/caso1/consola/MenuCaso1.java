package tpi.caso1.consola;

import tpi.caso1.logica.Dijkstra;
import tpi.caso1.logica.Exploracion;
import tpi.caso1.logica.OperacionInvalidaException;
import tpi.caso1.logica.RedLogistica;
import tpi.caso1.modelo.Centro;
import tpi.caso1.modelo.Ruta;

/**
 * Menú principal del Caso 1 y sus submenús.
 *
 * Esta clase solo lee datos (con Entrada), llama a la lógica y muestra los resultados
 * (con Vista). No valida reglas de negocio ni calcula nada: si una operación no se puede
 * hacer, la lógica lanza OperacionInvalidaException y acá solo se muestra su mensaje.
 *
 * El programa nunca termina por una excepción: cada acción se ejecuta dentro de un
 * try/catch que informa el error y vuelve al menú.
 */
public class MenuCaso1 {

    private static final String MENU_PRINCIPAL = """

            ===== RED LOGÍSTICA - CASO 1 =====
             1. Gestión de centros
             2. Gestión de rutas
             3. Camino mínimo entre dos centros (Dijkstra)
             4. Explorar la red desde un centro (BFS / DFS)
             5. Mostrar la red
             0. Salir""";

    private static final String MENU_CENTROS = """

            --- 1. Gestión de centros ---
             1. Alta de centro
             2. Modificar descripción
             3. Consultar centro por id
             4. Listar centros (ordenados)
             5. Baja de centro
             6. Ver árbol AVL del índice
             0. Volver""";

    private static final String MENU_RUTAS = """

            --- 2. Gestión de rutas ---
             1. Alta de ruta (bidireccional / un sentido)
             2. Modificar tiempo
             3. Deshabilitar ruta (baja temporal)
             4. Habilitar ruta
             5. Eliminar ruta
             6. Aislar centro
             7. Listar rutas
             0. Volver""";

    private final Entrada entrada;
    private RedLogistica red;

    public MenuCaso1() {
        this.entrada = new Entrada();
        this.red = new RedLogistica();
    }

    /**
     * Muestra el menú principal hasta que el usuario elige salir, o hasta que se termina
     * la entrada (por ejemplo, si se cierra la consola).
     */
    public void iniciar() {
        try {
            int opcion;
            do {
                System.out.println(MENU_PRINCIPAL);
                opcion = entrada.leerEntero("Opción: ", 0, 5);
                switch (opcion) {
                    case 1 -> menuCentros();
                    case 2 -> menuRutas();
                    case 3 -> ejecutar(this::caminoMinimo);
                    case 4 -> ejecutar(this::explorar);
                    case 5 -> ejecutar(() -> System.out.println(Vista.red(red)));
                    default -> System.out.println("Fin del programa.");
                }
            } while (opcion != 0);
        } catch (Entrada.EntradaTerminadaException e) {
            System.out.println("\nNo hay más datos de entrada. Fin del programa.");
        }
    }

    /**
     * Ejecuta una acción del menú atrapando los errores, y después hace una pausa para
     * que el usuario lea el resultado.
     *
     * - OperacionInvalidaException: error de regla de negocio; se muestra su mensaje.
     * - Cualquier otra RuntimeException: no debería pasar, pero si pasa se informa
     *   y el programa sigue, en lugar de cortarse.
     * - EntradaTerminadaException se deja pasar, porque indica que hay que salir.
     */
    private void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (Entrada.EntradaTerminadaException e) {
            throw e;
        } catch (OperacionInvalidaException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error inesperado: " + e);
        }
        entrada.pausa();
    }

    // ------------------------------------------------------------------
    // 1. Gestión de centros
    // ------------------------------------------------------------------

    private void menuCentros() {
        int opcion;
        do {
            System.out.println(MENU_CENTROS);
            opcion = entrada.leerEntero("Opción: ", 0, 6);
            switch (opcion) {
                case 1 -> ejecutar(this::altaCentro);
                case 2 -> ejecutar(this::modificarCentro);
                case 3 -> ejecutar(this::consultarCentro);
                case 4 -> ejecutar(() -> System.out.println(Vista.listaCentros(red.listarCentros())));
                case 5 -> ejecutar(this::bajaCentro);
                case 6 -> ejecutar(() -> System.out.println(Vista.arbol(red)));
                default -> { }   // 0: volver al menú principal
            }
        } while (opcion != 0);
    }

    private void altaCentro() {
        String id = entrada.leerTexto("Id del centro: ");
        String descripcion = entrada.leerTexto("Descripción: ");
        red.altaCentro(id, descripcion);
        System.out.println("Centro registrado: " + red.consultarCentro(id));
    }

    private void modificarCentro() {
        // Primero se busca el centro, para no pedir la descripción si el id no existe.
        Centro centro = red.consultarCentro(entrada.leerTexto("Id del centro: "));
        System.out.println("Descripción actual: " + centro.getDescripcion());
        String nueva = entrada.leerTexto("Nueva descripción: ");
        red.modificarCentro(centro.getId(), nueva);
        System.out.println("Centro modificado: " + centro);
    }

    private void consultarCentro() {
        Centro centro = red.consultarCentro(entrada.leerTexto("Id del centro: "));
        System.out.println(Vista.centro(centro, red));
    }

    private void bajaCentro() {
        Centro centro = red.consultarCentro(entrada.leerTexto("Id del centro: "));
        if (entrada.leerSiNo("Se eliminará " + centro + " y todas sus rutas. ¿Confirma?")) {
            red.bajaCentro(centro.getId());
            System.out.println("Centro " + centro.getId() + " eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // ------------------------------------------------------------------
    // 2. Gestión de rutas
    // ------------------------------------------------------------------

    private void menuRutas() {
        int opcion;
        do {
            System.out.println(MENU_RUTAS);
            opcion = entrada.leerEntero("Opción: ", 0, 7);
            switch (opcion) {
                case 1 -> ejecutar(this::altaRuta);
                case 2 -> ejecutar(this::modificarTiempoRuta);
                case 3 -> ejecutar(this::deshabilitarRuta);
                case 4 -> ejecutar(this::habilitarRuta);
                case 5 -> ejecutar(this::eliminarRuta);
                case 6 -> ejecutar(this::aislarCentro);
                case 7 -> ejecutar(() -> System.out.println(Vista.listaRutas(red.listarRutas())));
                default -> { }   // 0: volver al menú principal
            }
        } while (opcion != 0);
    }

    private void altaRuta() {
        String origen = entrada.leerTexto("Centro de origen: ");
        String destino = entrada.leerTexto("Centro de destino: ");
        // Se acepta cualquier entero: si es negativo, lo rechaza la lógica con su mensaje.
        int tiempo = entrada.leerEntero("Tiempo en minutos: ");
        boolean bidireccional = entrada.leerSiNo("¿Se puede recorrer en los dos sentidos?");
        red.altaRuta(origen, destino, tiempo, bidireccional);
        System.out.println("Ruta registrada: " + red.buscarRuta(origen, destino));
    }

    private void modificarTiempoRuta() {
        String origen = entrada.leerTexto("Centro de origen: ");
        String destino = entrada.leerTexto("Centro de destino: ");
        int tiempo = entrada.leerEntero("Nuevo tiempo en minutos: ");
        red.modificarTiempoRuta(origen, destino, tiempo);
        System.out.println("Ruta modificada: " + red.buscarRuta(origen, destino));
    }

    private void deshabilitarRuta() {
        String origen = entrada.leerTexto("Centro de origen: ");
        String destino = entrada.leerTexto("Centro de destino: ");
        if (red.deshabilitarRuta(origen, destino)) {
            System.out.println("Ruta deshabilitada: " + red.buscarRuta(origen, destino));
        } else {
            System.out.println("La ruta ya estaba deshabilitada.");
        }
    }

    private void habilitarRuta() {
        String origen = entrada.leerTexto("Centro de origen: ");
        String destino = entrada.leerTexto("Centro de destino: ");
        if (red.habilitarRuta(origen, destino)) {
            System.out.println("Ruta habilitada: " + red.buscarRuta(origen, destino));
        } else {
            System.out.println("La ruta ya estaba habilitada.");
        }
    }

    private void eliminarRuta() {
        String origen = entrada.leerTexto("Centro de origen: ");
        String destino = entrada.leerTexto("Centro de destino: ");
        Ruta ruta = red.buscarRuta(origen, destino);
        if (ruta == null) {
            // Se delega en la lógica para que el mensaje sea el mismo que en el resto de las operaciones.
            red.eliminarRuta(origen, destino);
            return;
        }
        if (entrada.leerSiNo("Se eliminará definitivamente " + ruta + ". ¿Confirma?")) {
            red.eliminarRuta(origen, destino);
            System.out.println("Ruta eliminada.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private void aislarCentro() {
        String id = entrada.leerTexto("Id del centro a aislar: ");
        int cantidad = red.aislarCentro(id);
        String rutas = cantidad == 1 ? "1 ruta" : cantidad + " rutas";
        System.out.println("Centro " + RedLogistica.normalizarId(id) + " aislado: se deshabilitaron " + rutas + ".");
    }

    // ------------------------------------------------------------------
    // 3 y 4. Consultas
    // ------------------------------------------------------------------

    private void caminoMinimo() {
        String origen = entrada.leerTexto("Centro de origen: ");
        String destino = entrada.leerTexto("Centro de destino: ");
        System.out.println(Vista.camino(Dijkstra.calcular(red, origen, destino)));
    }

    private void explorar() {
        String origen = entrada.leerTexto("Centro de origen: ");
        System.out.println(" 1. BFS - por anchura (nivel por nivel)");
        System.out.println(" 2. DFS - en profundidad");
        int criterio = entrada.leerEntero("Criterio: ", 1, 2);
        if (criterio == 1) {
            System.out.println(Vista.exploracion(Exploracion.bfs(red, origen)));
        } else {
            System.out.println(Vista.exploracion(Exploracion.dfs(red, origen)));
        }
    }
}
