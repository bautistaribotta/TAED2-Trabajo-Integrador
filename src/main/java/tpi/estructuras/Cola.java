package tpi.estructuras;

/**
 * Cola FIFO con nodos enlazados. Se usa en el recorrido BFS.
 *
 * FIFO significa "first in, first out": el primero que entra es el primero que sale,
 * como la fila de un banco. Se entra por el fin y se sale por el frente:
 *
 *   frente                        fin
 *     |                            |
 *     v                            v
 *   [ A | o-]--> [ B | o-]--> [ C | null ]   <-- encolar agrega acá
 *     ^
 *     desencolar saca de acá
 *
 * Guardamos el frente y el fin para que encolar y desencolar sean O(1).
 *
 * No admite elementos null.
 *
 * @param <T> tipo de los elementos que guarda la cola
 */
public class Cola<T> {

    /** Eslabón de la cadena: el dato y la referencia al nodo que está detrás en la fila. */
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo<T> frente;   // próximo en salir, o null si la cola está vacía
    private Nodo<T> fin;      // último en entrar, o null si la cola está vacía
    private int tamanio;      // cantidad de elementos

    /** Crea una cola vacía. */
    public Cola() {
        frente = null;
        fin = null;
        tamanio = 0;
    }

    /**
     * Agrega un elemento al final de la cola. Complejidad O(1).
     *
     * @throws IllegalArgumentException si el dato es null
     */
    public void encolar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("La cola no admite elementos null");
        }
        Nodo<T> nuevo = new Nodo<>(dato);

        if (estaVacia()) {
            // Único elemento: es a la vez el frente y el fin.
            frente = nuevo;
        } else {
            // El que era último ahora apunta al nuevo.
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamanio++;
    }

    /**
     * Saca y devuelve el elemento del frente. Complejidad O(1).
     *
     * @throws IllegalStateException si la cola está vacía
     */
    public T desencolar() {
        validarNoVacia();
        T dato = frente.dato;
        frente = frente.siguiente;   // el segundo pasa a ser el frente
        if (frente == null) {
            fin = null;   // era el único elemento: la cola queda vacía
        }
        tamanio--;
        return dato;
    }

    /**
     * Devuelve el elemento del frente sin sacarlo. Complejidad O(1).
     *
     * @throws IllegalStateException si la cola está vacía
     */
    public T verFrente() {
        validarNoVacia();
        return frente.dato;
    }

    /** Indica si la cola no tiene elementos. Complejidad O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Cantidad de elementos. Complejidad O(1). */
    public int tamanio() {
        return tamanio;
    }

    private void validarNoVacia() {
        if (estaVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
    }
}
