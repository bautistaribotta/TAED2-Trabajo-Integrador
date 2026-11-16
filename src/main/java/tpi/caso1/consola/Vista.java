package tpi.caso1.consola;

import tpi.caso1.logica.RedLogistica;
import tpi.caso1.logica.ResultadoCamino;
import tpi.caso1.logica.ResultadoExploracion;
import tpi.caso1.modelo.Centro;
import tpi.caso1.modelo.Ruta;
import tpi.estructuras.ListaEnlazada;

import java.util.Iterator;

/**
 * Formateo de resultados y dibujo de la red en consola.
 *
 * Todos los métodos son estáticos y devuelven un String: no imprimen. El menú decide
 * cuándo mostrarlos. Así esta clase no depende de System.out y se puede revisar su
 * salida sin ejecutar el menú.
 *
 * Se usan solo caracteres ASCII para flechas y líneas (-> y +-), porque la consola de
 * Windows puede no mostrar bien los símbolos Unicode. Los acentos sí se mantienen.
 */
public final class Vista {

    /** Aclaración que se muestra al final de cada exploración, para diferenciarla del camino mínimo. */
    private static final String ACLARACION_EXPLORACION =
            "Este recorrido muestra qué centros son alcanzables y en qué orden se descubren;\n"
            + "no calcula tiempos mínimos. Para eso usar la opción Camino mínimo (Dijkstra).";

    /** Por encima de esta cantidad de centros la matriz no entra en pantalla y se omite. */
    private static final int MAXIMO_CENTROS_MATRIZ = 20;

    private Vista() {
        // Clase de utilidad: no se crean objetos.
    }

    // ------------------------------------------------------------------
    // Camino mínimo
    // ------------------------------------------------------------------

    /**
     * Muestra el resultado de Dijkstra. Si hay camino, un renglón por tramo con el tiempo
     * del tramo y el acumulado; si no, el mensaje que corresponde al estado.
     *
     *   Camino mínimo de A a F (Dijkstra)
     *     A  (0 min)
     *     +- 5 min -> C  (5 min)
     *     +- 7 min -> D  (12 min)
     *     +- 6 min -> F  (18 min)
     *   Tiempo total: 18 minutos | 3 tramos | centros procesados: 7
     */
    public static String camino(ResultadoCamino resultado) {
        StringBuilder sb = new StringBuilder();
        sb.append("Camino mínimo de ").append(resultado.getIdOrigen())
          .append(" a ").append(resultado.getIdDestino()).append(" (Dijkstra)\n");

        switch (resultado.getEstado()) {
            case ORIGEN_INEXISTENTE ->
                    sb.append("  El centro ").append(resultado.getIdOrigen()).append(" no está registrado.");
            case DESTINO_INEXISTENTE ->
                    sb.append("  El centro ").append(resultado.getIdDestino()).append(" no está registrado.");
            case MISMO_CENTRO ->
                    sb.append("  El origen y el destino coinciden (").append(resultado.getIdOrigen())
                      .append("): tiempo 0.");
            case SIN_CAMINO ->
                    sb.append("  No existe un recorrido de ").append(resultado.getIdOrigen())
                      .append(" a ").append(resultado.getIdDestino())
                      .append(" (centros procesados: ").append(resultado.getCentrosProcesados()).append(").");
            case ENCONTRADO -> agregarTramos(sb, resultado);
        }
        return sb.toString();
    }

    /** Escribe los tramos de un camino encontrado y la línea de totales. */
    private static void agregarTramos(StringBuilder sb, ResultadoCamino resultado) {
        // Se recorren las dos listas a la par con iteradores: usar obtener(i) en un for
        // costaría O(n) en cada paso, porque la lista es enlazada.
        Iterator<Centro> centros = resultado.getCamino().iterator();
        Iterator<Integer> costos = resultado.getCostosAcumulados().iterator();

        Centro primero = centros.next();
        int acumuladoAnterior = costos.next();
        sb.append("  ").append(primero.getId()).append("  (0 min)\n");

        while (centros.hasNext()) {
            Centro centro = centros.next();
            int acumulado = costos.next();
            int tramo = acumulado - acumuladoAnterior;
            sb.append("  +- ").append(tramo).append(" min -> ").append(centro.getId())
              .append("  (").append(acumulado).append(" min)\n");
            acumuladoAnterior = acumulado;
        }

        int tramos = resultado.getCamino().tamanio() - 1;
        sb.append("Tiempo total: ").append(plural(resultado.getCostoTotal(), "minuto", "minutos"))
          .append(" | ").append(plural(tramos, "tramo", "tramos"))
          .append(" | centros procesados: ").append(resultado.getCentrosProcesados());
    }

    // ------------------------------------------------------------------
    // Exploración
    // ------------------------------------------------------------------

    /**
     * Muestra un recorrido BFS o DFS: el criterio usado, el orden de visita con el nivel
     * (BFS) o la profundidad (DFS) de cada centro, los centros no alcanzados y la aclaración
     * de que esto no es un camino mínimo.
     */
    public static String exploracion(ResultadoExploracion resultado) {
        StringBuilder sb = new StringBuilder();
        boolean esBfs = resultado.getCriterio() == ResultadoExploracion.Criterio.BFS;

        sb.append("Exploración desde ").append(resultado.getIdOrigen()).append(" - ");
        if (esBfs) {
            sb.append("BFS, por anchura: nivel por nivel, vecinos en orden alfabético\n");
        } else {
            sb.append("DFS, en profundidad: se avanza por el primer vecino alfabético antes de retroceder\n");
        }

        if (!resultado.isOrigenExiste()) {
            sb.append("  El centro ").append(resultado.getIdOrigen()).append(" no está registrado.");
            return sb.toString();
        }

        String etiqueta = esBfs ? "nivel" : "profundidad";
        Iterator<Centro> centros = resultado.getOrdenDeVisita().iterator();
        Iterator<Integer> niveles = resultado.getNiveles().iterator();
        int posicion = 1;
        while (centros.hasNext()) {
            Centro centro = centros.next();
            int nivel = niveles.next();
            sb.append(String.format("  %2d. ", posicion));
            // En DFS se sangra según la profundidad, para que se vea cómo se avanza y retrocede.
            if (!esBfs) {
                sb.append("  ".repeat(nivel));
            }
            sb.append(centro.getId()).append("  (").append(etiqueta).append(' ').append(nivel).append(")\n");
            posicion++;
        }

        sb.append("Centros no alcanzados: ");
        if (resultado.getNoAlcanzados().estaVacia()) {
            sb.append("ninguno");
        } else {
            sb.append(unirIds(resultado.getNoAlcanzados()));
        }
        sb.append("\n\n").append(ACLARACION_EXPLORACION);
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Centros y rutas
    // ------------------------------------------------------------------

    /**
     * Ficha de un centro: id, descripción y todas las rutas que lo tocan, activas e inactivas,
     * incluidas las de un solo sentido que llegan a él.
     */
    public static String centro(Centro centro, RedLogistica red) {
        ListaEnlazada<Ruta> rutas = new ListaEnlazada<>();
        for (Ruta ruta : red.listarRutas()) {
            if (ruta.getOrigen() == centro || ruta.getDestino() == centro) {
                rutas.agregarAlFinal(ruta);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Centro ").append(centro.getId()).append('\n');
        sb.append("  Descripción: ").append(centro.getDescripcion()).append('\n');
        sb.append("  Rutas (").append(rutas.tamanio()).append("):");
        if (rutas.estaVacia()) {
            sb.append(" ninguna");
        }
        for (Ruta ruta : rutas) {
            sb.append("\n    ").append(ruta);
        }
        return sb.toString();
    }

    /** Listado de centros, uno por renglón, en el orden recibido (por id). */
    public static String listaCentros(ListaEnlazada<Centro> centros) {
        if (centros.estaVacia()) {
            return "No hay centros registrados.";
        }
        StringBuilder sb = new StringBuilder("Centros registrados (" + centros.tamanio() + "):");
        for (Centro centro : centros) {
            sb.append("\n  ").append(centro);
        }
        return sb.toString();
    }

    /** Listado de rutas, numeradas. "<->" es bidireccional y "->" de un solo sentido. */
    public static String listaRutas(ListaEnlazada<Ruta> rutas) {
        if (rutas.estaVacia()) {
            return "No hay rutas registradas.";
        }
        StringBuilder sb = new StringBuilder("Rutas registradas (" + rutas.tamanio() + "):");
        int numero = 1;
        for (Ruta ruta : rutas) {
            sb.append(String.format("%n  %2d. ", numero)).append(ruta);
            numero++;
        }
        sb.append("\n  <-> = bidireccional, -> = un solo sentido");
        return sb.toString();
    }

    /** Dibujo del árbol AVL del índice, con su altura y cantidad de nodos. */
    public static String arbol(RedLogistica red) {
        return "Índice de centros (árbol AVL acostado: la raíz queda a la izquierda,\n"
                + "el subárbol derecho arriba y el izquierdo abajo)\n\n"
                + red.dibujarIndice()
                + "\nAltura: " + red.alturaIndice() + " | centros: " + red.cantidadCentros();
    }

    // ------------------------------------------------------------------
    // Mostrar la red
    // ------------------------------------------------------------------

    /**
     * Muestra la red completa: lista de adyacencia, matriz de tiempos y un resumen.
     */
    public static String red(RedLogistica red) {
        if (red.cantidadCentros() == 0) {
            return "La red no tiene centros.";
        }
        ListaEnlazada<Centro> centros = red.listarCentros();
        return listaAdyacencia(centros) + "\n\n" + matriz(red, centros) + "\n\n" + resumen(red);
    }

    /**
     * Lista de adyacencia: para cada centro, las rutas por las que se puede salir de él,
     * en el mismo orden en que están guardadas (por id del vecino).
     *
     *   A: -> B (8)  -> C (5)
     *   G: -> E (8) [X]  -> F (2) [X]
     */
    private static String listaAdyacencia(ListaEnlazada<Centro> centros) {
        StringBuilder sb = new StringBuilder("Lista de adyacencia (tiempos en minutos, [X] = deshabilitada)");
        int ancho = anchoMaximoDeId(centros);
        for (Centro centro : centros) {
            sb.append('\n').append(rellenarDerecha(centro.getId(), ancho)).append(':');
            boolean alguna = false;
            for (Ruta ruta : centro.getRutas()) {
                // Una ruta de un solo sentido que llega a este centro no está en su lista,
                // así que todas las de la lista son de salida.
                sb.append("  -> ").append(ruta.otroExtremo(centro).getId())
                  .append(" (").append(ruta.getTiempo()).append(')');
                if (!ruta.isActiva()) {
                    sb.append(" [X]");
                }
                alguna = true;
            }
            if (!alguna) {
                sb.append("  (sin rutas de salida)");
            }
        }
        return sb.toString();
    }

    /**
     * Matriz de tiempos: la fila es el origen y la columna el destino. Un punto indica que
     * no hay ruta directa y una X que la ruta existe pero está deshabilitada. Con rutas de un
     * solo sentido la matriz deja de ser simétrica.
     */
    private static String matriz(RedLogistica red, ListaEnlazada<Centro> centros) {
        if (centros.tamanio() > MAXIMO_CENTROS_MATRIZ) {
            return "Matriz de tiempos: se omite porque hay más de " + MAXIMO_CENTROS_MATRIZ
                    + " centros y no entraría en pantalla.";
        }

        // Ancho de columna: lo que ocupe el id o el tiempo más largo, con un margen.
        int ancho = anchoMaximoDeId(centros);
        for (Ruta ruta : red.listarRutas()) {
            ancho = Math.max(ancho, String.valueOf(ruta.getTiempo()).length());
        }
        ancho += 2;
        int anchoFila = anchoMaximoDeId(centros) + 1;

        StringBuilder sb = new StringBuilder("Matriz de tiempos (fila = origen, columna = destino; . = sin ruta, X = deshabilitada)\n");
        sb.append(" ".repeat(anchoFila));
        for (Centro columna : centros) {
            sb.append(rellenarIzquierda(columna.getId(), ancho));
        }
        for (Centro fila : centros) {
            sb.append('\n').append(rellenarDerecha(fila.getId(), anchoFila));
            for (Centro columna : centros) {
                String celda = ".";
                if (fila != columna) {
                    Ruta ruta = red.buscarRuta(fila.getId(), columna.getId());
                    if (ruta != null) {
                        celda = ruta.isActiva() ? String.valueOf(ruta.getTiempo()) : "X";
                    }
                }
                sb.append(rellenarIzquierda(celda, ancho));
            }
        }
        return sb.toString();
    }

    /** Resumen: cantidad de centros, rutas, rutas deshabilitadas y altura del AVL. */
    private static String resumen(RedLogistica red) {
        int rutas = 0;
        int deshabilitadas = 0;
        for (Ruta ruta : red.listarRutas()) {
            rutas++;
            if (!ruta.isActiva()) {
                deshabilitadas++;
            }
        }
        return "Resumen: " + plural(red.cantidadCentros(), "centro", "centros")
                + " | " + plural(rutas, "ruta", "rutas")
                + " (" + deshabilitadas + " deshabilitada" + (deshabilitadas == 1 ? "" : "s") + ")"
                + " | altura del AVL: " + red.alturaIndice();
    }

    // ------------------------------------------------------------------
    // Auxiliares de formato
    // ------------------------------------------------------------------

    /** "1 minuto", "18 minutos". */
    private static String plural(int cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    /** Ids separados por coma: "E, G". */
    private static String unirIds(ListaEnlazada<Centro> centros) {
        StringBuilder sb = new StringBuilder();
        for (Centro centro : centros) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(centro.getId());
        }
        return sb.toString();
    }

    private static int anchoMaximoDeId(ListaEnlazada<Centro> centros) {
        int ancho = 1;
        for (Centro centro : centros) {
            ancho = Math.max(ancho, centro.getId().length());
        }
        return ancho;
    }

    private static String rellenarDerecha(String texto, int ancho) {
        return texto + " ".repeat(Math.max(0, ancho - texto.length()));
    }

    private static String rellenarIzquierda(String texto, int ancho) {
        return " ".repeat(Math.max(0, ancho - texto.length())) + texto;
    }
}
