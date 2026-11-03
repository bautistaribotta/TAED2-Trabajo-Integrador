package tpi.estructuras;

/**
 * Pila LIFO con nodos enlazados. Se usa para reconstruir el camino de Dijkstra.
 *
 * LIFO significa "last in, first out": el último que entra es el primero que sale,
 * como una pila de platos. Todo ocurre en el mismo extremo, el tope:
 *
 *   tope
 *     |
 *     v
 *   [ C | o-]--> [ B | o-]--> [ A | null ]
 *     ^
 *     apilar y desapilar trabajan acá
 *
 * Cada nodo apunta al que tiene debajo. Como solo se toca el tope,
 * apilar y desapilar son O(1) y alcanza con una sola referencia.
 *
 * No admite elementos null.
 *
 * @param <T> tipo de los elementos que guarda la pila
 */
public class Pila<T> {

    /** Eslabón de la cadena: el dato y la referencia al nodo que está debajo. */
    private static class Nodo<T> {
        T dato;
        Nodo<T> debajo;

        Nodo(T dato, Nodo<T> debajo) {
            this.dato = dato;
            this.debajo = debajo;
        }
    }

    private Nodo<T> tope;   // último en entrar, o null si la pila está vacía
    private int tamanio;    // cantidad de elementos

    /** Crea una pila vacía. */
    public Pila() {
        tope = null;
        tamanio = 0;
    }

    /**
     * Agrega un elemento en el tope de la pila. Complejidad O(1).
     *
     * @throws IllegalArgumentException si el dato es null
     */
    public void apilar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("La pila no admite elementos null");
        }
        // El nuevo nodo queda arriba y apunta al que era el tope.
        tope = new Nodo<>(dato, tope);
        tamanio++;
    }

    /**
     * Saca y devuelve el elemento del tope. Complejidad O(1).
     *
     * @throws IllegalStateException si la pila está vacía
     */
    public T desapilar() {
        validarNoVacia();
        T dato = tope.dato;
        tope = tope.debajo;   // el de abajo pasa a ser el tope
        tamanio--;
        return dato;
    }

    /**
     * Devuelve el elemento del tope sin sacarlo. Complejidad O(1).
     *
     * @throws IllegalStateException si la pila está vacía
     */
    public T verTope() {
        validarNoVacia();
        return tope.dato;
    }

    /** Indica si la pila no tiene elementos. Complejidad O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Cantidad de elementos. Complejidad O(1). */
    public int tamanio() {
        return tamanio;
    }

    private void validarNoVacia() {
        if (estaVacia()) {
            throw new IllegalStateException("La pila está vacía");
        }
    }
}
