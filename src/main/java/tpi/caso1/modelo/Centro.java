package tpi.caso1.modelo;

import tpi.estructuras.ListaEnlazada;

/**
 * Centro logístico: vértice del grafo, con su lista de adyacencia.
 *
 * Cada centro guarda las rutas que salen de él o que lo tocan. Esa lista es lo que
 * forma el grafo: para saber a dónde se puede ir desde un centro, se recorren sus rutas.
 *
 *   Centro A
 *     rutas: [ A <-> B (8 min) ] --> [ A <-> C (5 min) ]
 *
 * Esta clase solo guarda datos. Las validaciones (id repetido, descripción vacía, etc.)
 * las hace RedLogistica, que es la que crea y administra los centros.
 *
 * No redefine equals: dos centros son el mismo solo si son el mismo objeto. Alcanza,
 * porque la red tiene un único objeto Centro por cada id.
 */
public class Centro {

    private final String id;                  // clave en el árbol AVL: es final porque cambiarla rompería el orden
    private String descripcion;
    private final ListaEnlazada<Ruta> rutas;  // lista de adyacencia; nace vacía
    private int indice;                       // posición en los arreglos de Dijkstra y de los recorridos

    /**
     * Crea un centro sin rutas.
     *
     * @param id          identificador único del centro
     * @param descripcion nombre o detalle del centro
     */
    public Centro(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
        this.rutas = new ListaEnlazada<>();
        this.indice = -1;   // todavía sin asignar: lo define cada algoritmo antes de usarlo
    }

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Devuelve la lista de adyacencia: las rutas que tocan a este centro. */
    public ListaEnlazada<Ruta> getRutas() {
        return rutas;
    }

    /**
     * Posición del centro en los arreglos de los algoritmos. Solo es válida durante
     * la consulta que la asignó, porque se recalcula en cada una.
     */
    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }

    /**
     * Representación en texto, por ejemplo "A - Depósito central".
     * No incluye las rutas: cada ruta imprime sus centros, y mostrarlas acá
     * haría que unos y otros se llamen entre sí sin terminar nunca.
     */
    @Override
    public String toString() {
        return id + " - " + descripcion;
    }
}
