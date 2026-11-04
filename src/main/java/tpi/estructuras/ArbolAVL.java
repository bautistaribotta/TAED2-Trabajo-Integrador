package tpi.estructuras;

/**
 * Árbol AVL (ABB autobalanceado). Se usa como índice de centros por id.
 *
 * Es un árbol binario de búsqueda: para cada nodo, las claves menores están en su
 * subárbol izquierdo y las mayores en el derecho. Buscar es bajar comparando.
 *
 * El problema de un árbol binario de búsqueda común es que puede degenerar en una lista.
 * Si las claves llegan ordenadas (A, B, C...), todas van a la derecha y buscar cuesta O(n).
 *
 * El AVL lo evita con una regla: en cada nodo, la altura de sus dos subárboles puede
 * diferir como mucho en 1. A esa diferencia la llamamos factor de balance:
 *
 *   factor de balance = altura(izquierdo) - altura(derecho)
 *
 * Tiene que valer -1, 0 o 1. Si después de una inserción o una baja vale 2 o -2, el nodo
 * está desbalanceado y se corrige con rotaciones. Así la altura queda siempre en O(log n),
 * y buscar, insertar y eliminar cuestan O(log n) garantizado.
 *
 * No admite claves ni valores null.
 *
 * @param <K> tipo de la clave por la que se ordena (en este trabajo, el id del centro)
 * @param <V> tipo del valor asociado a cada clave (en este trabajo, el centro)
 */
public class ArbolAVL<K extends Comparable<K>, V> {

    /** Nodo del árbol. Guarda su altura para no tener que recalcularla recorriendo el subárbol. */
    private static class NodoAVL<K, V> {
        K clave;
        V valor;
        NodoAVL<K, V> izquierdo;
        NodoAVL<K, V> derecho;
        int altura;

        NodoAVL(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.izquierdo = null;
            this.derecho = null;
            this.altura = 1;   // un nodo nuevo siempre es una hoja
        }
    }

    private NodoAVL<K, V> raiz;   // null si el árbol está vacío
    private int tamanio;          // cantidad de claves

    // Lo usan insertar y eliminar: como la recursión ya devuelve el nodo,
    // el resultado de la operación se informa por acá.
    private boolean huboCambio;

    /** Crea un árbol vacío. */
    public ArbolAVL() {
        raiz = null;
        tamanio = 0;
    }

    /**
     * Agrega una clave con su valor. Complejidad O(log n).
     *
     * @return true si se agregó; false si la clave ya existía (en ese caso no la pisa)
     * @throws IllegalArgumentException si la clave o el valor son null
     */
    public boolean insertar(K clave, V valor) {
        if (clave == null || valor == null) {
            throw new IllegalArgumentException("El árbol no admite claves ni valores null");
        }
        huboCambio = false;
        raiz = insertar(raiz, clave, valor);
        if (huboCambio) {
            tamanio++;
        }
        return huboCambio;
    }

    /**
     * Baja comparando hasta encontrar el lugar libre y crea ahí el nodo.
     * Al volver de la recursión, cada nodo del camino actualiza su altura y se balancea.
     *
     * @return la raíz del subárbol después de insertar, que puede cambiar por una rotación
     */
    private NodoAVL<K, V> insertar(NodoAVL<K, V> nodo, K clave, V valor) {
        if (nodo == null) {
            huboCambio = true;
            return new NodoAVL<>(clave, valor);
        }

        int comparacion = clave.compareTo(nodo.clave);
        if (comparacion < 0) {
            nodo.izquierdo = insertar(nodo.izquierdo, clave, valor);
        } else if (comparacion > 0) {
            nodo.derecho = insertar(nodo.derecho, clave, valor);
        } else {
            return nodo;   // clave duplicada: no se modifica nada
        }
        return balancear(nodo);
    }

    /**
     * Devuelve el valor asociado a la clave. Complejidad O(log n).
     *
     * @return el valor, o null si la clave no existe
     */
    public V buscar(K clave) {
        NodoAVL<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.clave);
            if (comparacion == 0) {
                return actual.valor;
            }
            actual = comparacion < 0 ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    /** Indica si la clave existe. Complejidad O(log n). */
    public boolean contiene(K clave) {
        return buscar(clave) != null;
    }

    /**
     * Elimina una clave. Complejidad O(log n).
     *
     * @return true si la eliminó; false si no existía
     */
    public boolean eliminar(K clave) {
        huboCambio = false;
        raiz = eliminar(raiz, clave);
        if (huboCambio) {
            tamanio--;
        }
        return huboCambio;
    }

    /**
     * Baja comparando hasta encontrar la clave y la elimina según cuántos hijos tenga el nodo.
     * Al volver de la recursión, cada nodo del camino actualiza su altura y se balancea.
     *
     * @return la raíz del subárbol después de eliminar
     */
    private NodoAVL<K, V> eliminar(NodoAVL<K, V> nodo, K clave) {
        if (nodo == null) {
            return null;   // la clave no estaba
        }

        int comparacion = clave.compareTo(nodo.clave);
        if (comparacion < 0) {
            nodo.izquierdo = eliminar(nodo.izquierdo, clave);
        } else if (comparacion > 0) {
            nodo.derecho = eliminar(nodo.derecho, clave);
        } else {
            huboCambio = true;

            // Casos 1 y 2: es una hoja o tiene un solo hijo. Ese hijo (o null) ocupa su lugar.
            if (nodo.izquierdo == null) {
                return nodo.derecho;
            }
            if (nodo.derecho == null) {
                return nodo.izquierdo;
            }

            // Caso 3: tiene dos hijos. No se puede sacar directamente, así que lo reemplazamos
            // por su sucesor (la clave siguiente en orden), y borramos el sucesor de donde estaba.
            // El sucesor es el mínimo del subárbol derecho: es mayor que todo lo de la izquierda
            // y menor que el resto de la derecha, así que el orden se mantiene.
            NodoAVL<K, V> sucesor = minimo(nodo.derecho);
            nodo.clave = sucesor.clave;
            nodo.valor = sucesor.valor;
            nodo.derecho = eliminar(nodo.derecho, sucesor.clave);
        }
        return balancear(nodo);
    }

    /** Devuelve el nodo con la clave más chica del subárbol: el que está más a la izquierda. */
    private NodoAVL<K, V> minimo(NodoAVL<K, V> nodo) {
        while (nodo.izquierdo != null) {
            nodo = nodo.izquierdo;
        }
        return nodo;
    }

    /**
     * Actualiza la altura del nodo y, si quedó desbalanceado, aplica la rotación que corresponda.
     * Los nombres de los casos indican hacia dónde está cargado el árbol:
     * LL es izquierda-izquierda, RR es derecha-derecha, LR es izquierda-derecha y RL derecha-izquierda.
     *
     * @return la raíz del subárbol, que cambia si hubo rotación
     */
    private NodoAVL<K, V> balancear(NodoAVL<K, V> nodo) {
        actualizarAltura(nodo);
        int balance = factorBalance(nodo);

        if (balance == 2) {   // cargado a la izquierda
            if (factorBalance(nodo.izquierdo) < 0) {
                // Caso LR: primero se lo convierte en un caso LL.
                nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            }
            return rotarDerecha(nodo);   // caso LL
        }

        if (balance == -2) {   // cargado a la derecha
            if (factorBalance(nodo.derecho) > 0) {
                // Caso RL: primero se lo convierte en un caso RR.
                nodo.derecho = rotarDerecha(nodo.derecho);
            }
            return rotarIzquierda(nodo);   // caso RR
        }

        return nodo;   // ya estaba balanceado
    }

    /**
     * Rotación simple a la derecha: el hijo izquierdo sube y el nodo baja a la derecha.
     * El subárbol B cambia de padre, porque sus claves están entre las de Y y las de X.
     *
     *         X                Y
     *        / \              / \
     *       Y   C    --->    A   X
     *      / \                  / \
     *     A   B                B   C
     *
     * @return la nueva raíz del subárbol
     */
    private NodoAVL<K, V> rotarDerecha(NodoAVL<K, V> x) {
        NodoAVL<K, V> y = x.izquierdo;
        x.izquierdo = y.derecho;
        y.derecho = x;

        // Primero el que bajó, porque la altura del que subió depende de la suya.
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    /**
     * Rotación simple a la izquierda: es el espejo de la anterior.
     *
     *       X                    Y
     *      / \                  / \
     *     A   Y      --->      X   C
     *        / \              / \
     *       B   C            A   B
     *
     * @return la nueva raíz del subárbol
     */
    private NodoAVL<K, V> rotarIzquierda(NodoAVL<K, V> x) {
        NodoAVL<K, V> y = x.derecho;
        x.derecho = y.izquierdo;
        y.izquierdo = x;

        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    /** Altura de un subárbol. Un subárbol vacío tiene altura 0. */
    private int altura(NodoAVL<K, V> nodo) {
        return nodo == null ? 0 : nodo.altura;
    }

    private void actualizarAltura(NodoAVL<K, V> nodo) {
        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    private int factorBalance(NodoAVL<K, V> nodo) {
        return altura(nodo.izquierdo) - altura(nodo.derecho);
    }

    /**
     * Devuelve los valores ordenados por clave. Complejidad O(n).
     *
     * Usa el recorrido inorden: primero el subárbol izquierdo, después el nodo y por último
     * el derecho. En un árbol binario de búsqueda, eso visita las claves de menor a mayor.
     */
    public ListaEnlazada<V> valoresEnOrden() {
        ListaEnlazada<V> valores = new ListaEnlazada<>();
        recorrerInorden(raiz, valores);
        return valores;
    }

    private void recorrerInorden(NodoAVL<K, V> nodo, ListaEnlazada<V> valores) {
        if (nodo == null) {
            return;
        }
        recorrerInorden(nodo.izquierdo, valores);
        valores.agregarAlFinal(nodo.valor);
        recorrerInorden(nodo.derecho, valores);
    }

    /** Cantidad de claves. Complejidad O(1). */
    public int tamanio() {
        return tamanio;
    }

    /** Altura del árbol: 0 si está vacío y 1 si solo tiene la raíz. Complejidad O(1). */
    public int altura() {
        return altura(raiz);
    }

    /** Indica si el árbol no tiene claves. Complejidad O(1). */
    public boolean estaVacio() {
        return raiz == null;
    }

    /**
     * Devuelve un dibujo del árbol acostado: la raíz queda a la izquierda,
     * el subárbol derecho arriba y el izquierdo abajo. Complejidad O(n).
     *
     *         G
     *     F
     *         E
     * D
     *         C
     *     B
     *         A
     */
    public String dibujar() {
        if (estaVacio()) {
            return "(árbol vacío)";
        }
        StringBuilder dibujo = new StringBuilder();
        dibujar(raiz, 0, dibujo);
        return dibujo.toString();
    }

    private void dibujar(NodoAVL<K, V> nodo, int nivel, StringBuilder dibujo) {
        if (nodo == null) {
            return;
        }
        dibujar(nodo.derecho, nivel + 1, dibujo);
        dibujo.append("    ".repeat(nivel)).append(nodo.clave).append('\n');
        dibujar(nodo.izquierdo, nivel + 1, dibujo);
    }

    /**
     * Comprueba que el árbol cumpla todas sus reglas: orden de las claves, factor de balance
     * entre -1 y 1 y alturas guardadas correctas. Sirve para revisar el árbol durante el
     * desarrollo. Complejidad O(n).
     */
    public boolean esValido() {
        return esValido(raiz, null, null);
    }

    /**
     * Revisa un subárbol cuyas claves tienen que estar entre minimo y maximo.
     * Un límite en null significa que por ese lado no hay restricción.
     */
    private boolean esValido(NodoAVL<K, V> nodo, K minimo, K maximo) {
        if (nodo == null) {
            return true;
        }
        boolean ordenCorrecto = (minimo == null || nodo.clave.compareTo(minimo) > 0)
                && (maximo == null || nodo.clave.compareTo(maximo) < 0);
        boolean alturaCorrecta = nodo.altura == 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
        boolean balanceCorrecto = Math.abs(factorBalance(nodo)) <= 1;

        return ordenCorrecto && alturaCorrecta && balanceCorrecto
                && esValido(nodo.izquierdo, minimo, nodo.clave)
                && esValido(nodo.derecho, nodo.clave, maximo);
    }
}
