package tpi.estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Lista simplemente enlazada genérica. Se usa como lista de adyacencia y para devolver colecciones.
 *
 * Cómo funciona: cada elemento se guarda dentro de un Nodo, que además tiene una
 * referencia al nodo siguiente. Así se forma una cadena:
 *
 *   cabeza                          cola
 *     |                               |
 *     v                               v
 *   [ A | o-]--> [ B | o-]--> [ C | null ]
 *
 * Guardamos una referencia al primer nodo (cabeza) y otra al último (cola).
 * Tener la cola permite agregar al final en O(1), sin recorrer toda la lista.
 *
 * Implementa Iterable para poder recorrerla con un for-each:
 * for (Ruta r : lista) { ... }
 *
 * No admite elementos null.
 *
 * @param <T> tipo de los elementos que guarda la lista
 */
public class ListaEnlazada<T> implements Iterable<T> {

    /**
     * Eslabón de la cadena: el dato y la referencia al siguiente nodo.
     * Es una clase interna privada porque nadie fuera de la lista necesita conocerla.
     */
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo<T> cabeza;   // primer nodo, o null si la lista está vacía
    private Nodo<T> cola;     // último nodo, o null si la lista está vacía
    private int tamanio;      // cantidad de elementos; se actualiza en cada alta y baja

    /** Crea una lista vacía. */
    public ListaEnlazada() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    /**
     * Agrega un elemento al final de la lista. Complejidad O(1).
     *
     * @throws IllegalArgumentException si el dato es null
     */
    public void agregarAlFinal(T dato) {
        validarNoNulo(dato);
        Nodo<T> nuevo = new Nodo<>(dato);

        if (estaVacia()) {
            // Único elemento: es a la vez el primero y el último.
            cabeza = nuevo;
        } else {
            // El que era último ahora apunta al nuevo.
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        tamanio++;
    }

    /**
     * Agrega un elemento manteniendo la lista ordenada según el criterio indicado.
     * Complejidad O(n), porque hay que recorrer hasta encontrar la posición.
     *
     * Si hay elementos iguales, el nuevo queda después de ellos (inserción estable).
     * Solo mantiene el orden si todos los elementos se agregaron con este método
     * y con el mismo criterio.
     *
     * Ejemplo: con la lista [B, D] y un criterio alfabético, agregar C da [B, C, D].
     *
     * @param criterio compara dos elementos: negativo si el primero va antes,
     *                 0 si son iguales y positivo si va después
     * @throws IllegalArgumentException si el dato es null
     */
    public void agregarOrdenado(T dato, Comparator<T> criterio) {
        validarNoNulo(dato);

        // Caso 1: lista vacía, o el nuevo va antes que el primero → se inserta al inicio.
        if (estaVacia() || criterio.compare(dato, cabeza.dato) < 0) {
            Nodo<T> nuevo = new Nodo<>(dato);
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
            if (cola == null) {
                cola = nuevo;   // era la lista vacía: el nuevo también es el último
            }
            tamanio++;
            return;
        }

        // Caso 2: avanzamos mientras el siguiente nodo sea menor o igual que el nuevo.
        // Al terminar, 'anterior' es el nodo después del cual hay que insertar.
        Nodo<T> anterior = cabeza;
        while (anterior.siguiente != null && criterio.compare(anterior.siguiente.dato, dato) <= 0) {
            anterior = anterior.siguiente;
        }

        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = anterior.siguiente;   // el nuevo apunta al que seguía
        anterior.siguiente = nuevo;             // el anterior ahora apunta al nuevo
        if (nuevo.siguiente == null) {
            cola = nuevo;   // se insertó al final
        }
        tamanio++;
    }

    /**
     * Elimina la primera aparición del elemento (comparando con equals).
     * Complejidad O(n).
     *
     * @return true si lo encontró y lo eliminó; false si no estaba
     */
    public boolean eliminar(T dato) {
        if (dato == null || estaVacia()) {
            return false;
        }

        // Caso 1: el elemento está en la cabeza → la cabeza pasa a ser el segundo nodo.
        if (cabeza.dato.equals(dato)) {
            cabeza = cabeza.siguiente;
            if (cabeza == null) {
                cola = null;   // era el único elemento: la lista queda vacía
            }
            tamanio--;
            return true;
        }

        // Caso 2: buscamos el nodo ANTERIOR al que hay que borrar, porque en una lista
        // simplemente enlazada hay que cambiar el 'siguiente' del nodo previo.
        Nodo<T> anterior = cabeza;
        while (anterior.siguiente != null && !anterior.siguiente.dato.equals(dato)) {
            anterior = anterior.siguiente;
        }

        if (anterior.siguiente == null) {
            return false;   // llegamos al final sin encontrarlo
        }

        Nodo<T> aBorrar = anterior.siguiente;
        anterior.siguiente = aBorrar.siguiente;   // "saltamos" el nodo a borrar
        if (aBorrar == cola) {
            cola = anterior;   // borramos el último: el anterior pasa a ser la cola
        }
        tamanio--;
        // El nodo borrado ya no tiene referencias: lo libera el garbage collector.
        return true;
    }

    /**
     * Devuelve el primer elemento que cumple la condición. Complejidad O(n).
     *
     * Ejemplo: lista.buscar(c -> c.getId().equals("A"))
     *
     * @return el elemento encontrado, o null si ninguno cumple
     */
    public T buscar(Predicate<T> condicion) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (condicion.test(actual.dato)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Devuelve el elemento en la posición indicada (la primera es 0). Complejidad O(n),
     * porque hay que recorrer desde la cabeza: no conviene usarlo dentro de un for
     * que recorra toda la lista (para eso está el for-each).
     *
     * @throws IndexOutOfBoundsException si el índice está fuera de rango
     */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fuera de rango (tamaño " + tamanio + ")");
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /** Cantidad de elementos. Complejidad O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Indica si la lista no tiene elementos. Complejidad O(1). */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /**
     * Devuelve un iterador que recorre la lista de la cabeza a la cola.
     * Es lo que usa Java por detrás cuando se escribe un for-each.
     *
     * No modificar la lista (agregar o eliminar) mientras se la recorre con este iterador.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;   // próximo nodo a devolver

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException("No hay más elementos en la lista");
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    /** Representación en texto, por ejemplo "[A, B, C]". Útil para depurar. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) {
                sb.append(", ");
            }
            actual = actual.siguiente;
        }
        return sb.append("]").toString();
    }

    private void validarNoNulo(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("La lista no admite elementos null");
        }
    }
}
