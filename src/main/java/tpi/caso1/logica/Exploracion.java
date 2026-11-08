package tpi.caso1.logica;

import tpi.caso1.modelo.Centro;
import tpi.caso1.modelo.Ruta;
import tpi.estructuras.Cola;
import tpi.estructuras.ListaEnlazada;

/**
 * Recorridos de exploración de la red: BFS (por anchura) y DFS (en profundidad).
 *
 * Los dos visitan todos los centros alcanzables desde el origen, pasando solo por rutas
 * activas y respetando el sentido de las rutas de un solo sentido. Los vecinos se visitan
 * en orden alfabético de id, porque las listas de adyacencia están ordenadas así; eso hace
 * que el resultado sea siempre el mismo.
 *
 * Ninguno de los dos minimiza el tiempo: BFS minimiza la cantidad de tramos y DFS ni
 * siquiera eso. Para el menor tiempo total está Dijkstra.
 *
 * Complejidad: O(V + E) los dos, porque cada centro se visita una vez y cada ruta se
 * mira una vez por cada extremo desde el que se puede salir.
 */
public class Exploracion {

    private Exploracion() {
        // Clase de utilidad: no se crean objetos.
    }

    /**
     * Recorrido por anchura (BFS): primero el origen, después todos sus vecinos, después
     * los vecinos de esos vecinos, y así nivel por nivel. La cola garantiza ese orden:
     * los centros se atienden en el mismo orden en que se descubrieron.
     *
     * Un centro se marca como visitado al encolarlo, no al atenderlo, para no encolarlo
     * dos veces si lo descubren dos vecinos distintos.
     *
     * @throws OperacionInvalidaException si el id está vacío o contiene punto y coma
     */
    public static ResultadoExploracion bfs(RedLogistica red, String origen) {
        String idOrigen = RedLogistica.normalizarId(origen);
        if (!red.existeCentro(idOrigen)) {
            return origenInexistente(ResultadoExploracion.Criterio.BFS, idOrigen);
        }
        Centro inicio = red.consultarCentro(idOrigen);
        ListaEnlazada<Centro> todos = reindexar(red);

        boolean[] visitado = new boolean[todos.tamanio()];
        int[] nivel = new int[todos.tamanio()];
        ListaEnlazada<Centro> orden = new ListaEnlazada<>();
        ListaEnlazada<Integer> niveles = new ListaEnlazada<>();

        Cola<Centro> cola = new Cola<>();
        cola.encolar(inicio);
        visitado[inicio.getIndice()] = true;
        nivel[inicio.getIndice()] = 0;

        while (!cola.estaVacia()) {
            Centro u = cola.desencolar();
            orden.agregarAlFinal(u);
            niveles.agregarAlFinal(nivel[u.getIndice()]);

            for (Ruta ruta : u.getRutas()) {           // ya están ordenadas por id del vecino
                if (ruta.permiteSalirDesde(u)) {
                    Centro v = ruta.otroExtremo(u);
                    if (!visitado[v.getIndice()]) {
                        visitado[v.getIndice()] = true;
                        nivel[v.getIndice()] = nivel[u.getIndice()] + 1;
                        cola.encolar(v);
                    }
                }
            }
        }

        return new ResultadoExploracion(ResultadoExploracion.Criterio.BFS, idOrigen, true,
                orden, niveles, noAlcanzados(todos, visitado));
    }

    /**
     * Recorrido en profundidad (DFS): desde cada centro se avanza al primer vecino
     * (en orden alfabético) que todavía no se visitó, y se sigue lo más lejos posible.
     * Cuando un centro no tiene vecinos sin visitar, se retrocede al anterior y se prueba
     * con su siguiente vecino.
     *
     * Está hecho con recursión: el retroceso lo resuelve la propia pila de llamadas de Java.
     * Con miles de centros en línea la recursión podría desbordar esa pila; la alternativa
     * sería una versión iterativa con la clase Pila.
     *
     * @throws OperacionInvalidaException si el id está vacío o contiene punto y coma
     */
    public static ResultadoExploracion dfs(RedLogistica red, String origen) {
        String idOrigen = RedLogistica.normalizarId(origen);
        if (!red.existeCentro(idOrigen)) {
            return origenInexistente(ResultadoExploracion.Criterio.DFS, idOrigen);
        }
        Centro inicio = red.consultarCentro(idOrigen);
        ListaEnlazada<Centro> todos = reindexar(red);

        boolean[] visitado = new boolean[todos.tamanio()];
        ListaEnlazada<Centro> orden = new ListaEnlazada<>();
        ListaEnlazada<Integer> niveles = new ListaEnlazada<>();

        dfs(inicio, 0, visitado, orden, niveles);

        return new ResultadoExploracion(ResultadoExploracion.Criterio.DFS, idOrigen, true,
                orden, niveles, noAlcanzados(todos, visitado));
    }

    /**
     * Paso recursivo del DFS: visita u y después, uno por uno, a cada vecino no visitado.
     *
     * @param profundidad cantidad de llamadas desde el origen hasta u
     */
    private static void dfs(Centro u, int profundidad, boolean[] visitado,
                            ListaEnlazada<Centro> orden, ListaEnlazada<Integer> niveles) {
        visitado[u.getIndice()] = true;
        orden.agregarAlFinal(u);
        niveles.agregarAlFinal(profundidad);

        for (Ruta ruta : u.getRutas()) {
            if (ruta.permiteSalirDesde(u)) {
                Centro v = ruta.otroExtremo(u);
                if (!visitado[v.getIndice()]) {
                    dfs(v, profundidad + 1, visitado, orden, niveles);
                }
            }
        }
    }

    /**
     * Asigna a cada centro un índice de 0 a n-1, en orden de id, para poder usar arreglos.
     *
     * @return todos los centros, ordenados por id
     */
    private static ListaEnlazada<Centro> reindexar(RedLogistica red) {
        ListaEnlazada<Centro> todos = red.listarCentros();
        int i = 0;
        for (Centro centro : todos) {
            centro.setIndice(i);
            i++;
        }
        return todos;
    }

    /** Devuelve los centros que el recorrido no marcó como visitados, en orden de id. */
    private static ListaEnlazada<Centro> noAlcanzados(ListaEnlazada<Centro> todos, boolean[] visitado) {
        ListaEnlazada<Centro> resultado = new ListaEnlazada<>();
        for (Centro centro : todos) {
            if (!visitado[centro.getIndice()]) {
                resultado.agregarAlFinal(centro);
            }
        }
        return resultado;
    }

    /** Resultado vacío para cuando el centro de origen no está registrado. */
    private static ResultadoExploracion origenInexistente(ResultadoExploracion.Criterio criterio, String idOrigen) {
        return new ResultadoExploracion(criterio, idOrigen, false,
                new ListaEnlazada<>(), new ListaEnlazada<>(), new ListaEnlazada<>());
    }
}
