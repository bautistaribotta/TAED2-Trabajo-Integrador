package tpi.estructuras;

import java.util.Comparator;

/**
 * Montículo binario de mínimos sobre arreglo. Se usa como cola de prioridad en Dijkstra (y en Huffman en el Caso 2).
 *
 * Es un árbol binario con una sola regla: cada padre es menor o igual que sus hijos.
 * Por eso el mínimo siempre está en la raíz. No está ordenado del todo: entre hermanos
 * no hay ningún orden, y eso es lo que hace que mantenerlo sea barato.
 *
 * El árbol se llena por niveles, de izquierda a derecha, y por eso se puede guardar
 * en un arreglo sin usar nodos ni referencias:
 *
 *            2                 índice:  0  1  2  3  4  5
 *          /   \               valor:  [2, 5, 3, 9, 7, 8]
 *         5     3
 *        / \   /
 *       9   7 8
 *
 * Las posiciones se calculan con cuentas. Para el elemento en el índice i:
 *
 *   padre           = (i - 1) / 2
 *   hijo izquierdo  = 2 * i + 1
 *   hijo derecho    = 2 * i + 2
 *
 * Como el árbol está siempre lleno por niveles, su altura es log2(n). Insertar y extraer
 * recorren como mucho esa altura, así que cuestan O(log n).
 *
 * No admite elementos null.
 *
 * @param <T> tipo de los elementos que guarda el montículo
 */
public class MonticuloMinimo<T> {

    private static final int CAPACIDAD_INICIAL = 16;

    private T[] elementos;                  // el árbol guardado por niveles; la raíz está en el índice 0
    private int tamanio;                    // cantidad de elementos; las posiciones desde acá están libres
    private final Comparator<T> criterio;   // define qué elemento es "menor"

    /**
     * Crea un montículo vacío.
     *
     * @param criterio compara dos elementos: negativo si el primero es menor,
     *                 0 si son iguales y positivo si es mayor
     */
    @SuppressWarnings("unchecked")   // Java no permite crear un arreglo de T: se crea de Object y se convierte
    public MonticuloMinimo(Comparator<T> criterio) {
        this.elementos = (T[]) new Object[CAPACIDAD_INICIAL];
        this.tamanio = 0;
        this.criterio = criterio;
    }

    /**
     * Agrega un elemento. Complejidad O(log n).
     *
     * Se pone en la primera posición libre (así el árbol sigue lleno por niveles)
     * y después se lo hace flotar hasta su lugar.
     *
     * @throws IllegalArgumentException si el dato es null
     */
    public void insertar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("El montículo no admite elementos null");
        }
        if (tamanio == elementos.length) {
            duplicarCapacidad();
        }
        elementos[tamanio] = dato;
        tamanio++;
        flotar(tamanio - 1);
    }

    /**
     * Saca y devuelve el elemento mínimo. Complejidad O(log n).
     *
     * El mínimo está en la raíz. Para no dejar un hueco, el último elemento pasa
     * a la raíz y después se lo hace hundir hasta su lugar.
     *
     * @throws IllegalStateException si el montículo está vacío
     */
    public T extraerMinimo() {
        validarNoVacio();
        T minimo = elementos[0];

        tamanio--;
        elementos[0] = elementos[tamanio];
        elementos[tamanio] = null;   // sin esta referencia, el garbage collector puede liberar el objeto

        if (tamanio > 0) {
            hundir(0);
        }
        return minimo;
    }

    /**
     * Devuelve el elemento mínimo sin sacarlo. Complejidad O(1).
     *
     * @throws IllegalStateException si el montículo está vacío
     */
    public T verMinimo() {
        validarNoVacio();
        return elementos[0];
    }

    /** Indica si el montículo no tiene elementos. Complejidad O(1). */
    public boolean estaVacio() {
        return tamanio == 0;
    }

    /** Cantidad de elementos. Complejidad O(1). */
    public int tamanio() {
        return tamanio;
    }

    /**
     * Sube el elemento mientras sea menor que su padre.
     * Se detiene al llegar a la raíz o cuando el padre ya es menor o igual.
     */
    private void flotar(int indice) {
        while (indice > 0) {
            int padre = (indice - 1) / 2;
            if (criterio.compare(elementos[indice], elementos[padre]) >= 0) {
                return;   // la regla ya se cumple
            }
            intercambiar(indice, padre);
            indice = padre;
        }
    }

    /**
     * Baja el elemento mientras sea mayor que alguno de sus hijos.
     * Siempre se intercambia con el hijo menor: así ese hijo, que queda como padre,
     * es menor o igual que su hermano y la regla se sigue cumpliendo.
     */
    private void hundir(int indice) {
        while (true) {
            int izquierdo = 2 * indice + 1;
            int derecho = 2 * indice + 2;

            if (izquierdo >= tamanio) {
                return;   // no tiene hijos: es una hoja
            }

            // Buscamos el hijo menor. Puede no tener hijo derecho.
            int menor = izquierdo;
            if (derecho < tamanio && criterio.compare(elementos[derecho], elementos[izquierdo]) < 0) {
                menor = derecho;
            }

            if (criterio.compare(elementos[indice], elementos[menor]) <= 0) {
                return;   // la regla ya se cumple
            }
            intercambiar(indice, menor);
            indice = menor;
        }
    }

    private void intercambiar(int i, int j) {
        T auxiliar = elementos[i];
        elementos[i] = elementos[j];
        elementos[j] = auxiliar;
    }

    /**
     * Un arreglo no puede crecer: se crea uno del doble de tamaño y se copian los elementos.
     * Esta copia cuesta O(n), pero ocurre pocas veces, así que no cambia el costo de insertar.
     */
    @SuppressWarnings("unchecked")
    private void duplicarCapacidad() {
        T[] nuevo = (T[]) new Object[elementos.length * 2];
        for (int i = 0; i < tamanio; i++) {
            nuevo[i] = elementos[i];
        }
        elementos = nuevo;
    }

    private void validarNoVacio() {
        if (estaVacio()) {
            throw new IllegalStateException("El montículo está vacío");
        }
    }
}
