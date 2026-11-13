package tpi.estructuras;

/**
 * Árbol de Huffman sobre el alfabeto de los bytes (símbolos 0 a 255).
 *
 * Es un árbol binario donde cada hoja es un símbolo y el camino desde la raíz hasta la
 * hoja es su código: bajar a la izquierda agrega un 0 y bajar a la derecha agrega un 1.
 * Como los símbolos están solo en las hojas, ningún código es el principio de otro
 * (código libre de prefijos), y por eso al leer los bits se sabe dónde termina cada uno.
 *
 * Se construye con el algoritmo de Huffman: los símbolos más frecuentes quedan cerca de la
 * raíz (códigos cortos) y los menos frecuentes, lejos (códigos largos). Así el total de
 * bits, la suma de frecuencia por longitud del código, es el mínimo posible para un código
 * de este tipo.
 *
 * Ejemplo con frecuencias A=5, B=2, C=1, D=1:
 *
 *              (9)
 *             0/ \1
 *           (4)   A        A = 1
 *          0/ \1           B = 00
 *          B  (2)          C = 010
 *            0/ \1         D = 011
 *            C   D
 *
 * A, el más frecuente, usa 1 bit; C y D, los más raros, usan 3. En total
 * 5·1 + 2·2 + 1·3 + 1·3 = 15 bits, contra 9·8 = 72 si cada símbolo ocupara un byte.
 *
 * Esta clase no sabe nada de archivos: recibe frecuencias, genera códigos, y sabe
 * escribirse y leerse como bits (ver escribirEnPreorden y leerEnPreorden).
 */
public class ArbolHuffman {

    /** Cantidad de símbolos distintos posibles: un byte puede valer de 0 a 255. */
    public static final int SIMBOLOS = 256;

    /**
     * Nodo del árbol. Las hojas tienen símbolo y no tienen hijos; los nodos internos
     * tienen siempre los dos hijos.
     */
    private static class NodoHuffman {
        final long frecuencia;    // suma de las frecuencias de las hojas de este subárbol
        final int simbolo;        // solo en hojas: valor del byte (0 a 255); -1 en internos
        final int menorSimbolo;   // menor símbolo del subárbol, para desempatar
        final NodoHuffman izquierdo;
        final NodoHuffman derecho;

        /** Crea una hoja. */
        NodoHuffman(int simbolo, long frecuencia) {
            this.frecuencia = frecuencia;
            this.simbolo = simbolo;
            this.menorSimbolo = simbolo;
            this.izquierdo = null;
            this.derecho = null;
        }

        /** Crea un nodo interno que une dos subárboles. */
        NodoHuffman(NodoHuffman izquierdo, NodoHuffman derecho) {
            this.frecuencia = izquierdo.frecuencia + derecho.frecuencia;
            this.simbolo = -1;
            this.menorSimbolo = Math.min(izquierdo.menorSimbolo, derecho.menorSimbolo);
            this.izquierdo = izquierdo;
            this.derecho = derecho;
        }

        boolean esHoja() {
            return izquierdo == null;
        }
    }

    private final NodoHuffman raiz;      // null si el árbol está vacío
    private final int hojas;             // cantidad de símbolos distintos
    private final boolean conFrecuencias; // false si se leyó de un archivo (ahí no viajan las frecuencias)

    private ArbolHuffman(NodoHuffman raiz, int hojas, boolean conFrecuencias) {
        this.raiz = raiz;
        this.hojas = hojas;
        this.conFrecuencias = conFrecuencias;
    }

    // ------------------------------------------------------------------
    // Construcción
    // ------------------------------------------------------------------

    /**
     * Construye el árbol de Huffman a partir de las frecuencias de cada byte.
     * Complejidad O(n log n), con n la cantidad de símbolos distintos (como mucho 256).
     *
     * Algoritmo:
     * 1. Se crea una hoja por cada símbolo que aparece y se la pone en un montículo de mínimos.
     * 2. Mientras quede más de un subárbol: se sacan los dos de menor frecuencia y se los
     *    une bajo un nodo nuevo cuya frecuencia es la suma. El primero que sale va a la
     *    izquierda (bit 0) y el segundo a la derecha (bit 1). El nodo nuevo vuelve al montículo.
     * 3. El último subárbol que queda es el árbol completo.
     *
     * Desempate: el montículo compara primero por frecuencia y, si empatan, por el menor
     * símbolo de cada subárbol. Dos subárboles distintos nunca comparten símbolos, así que
     * nunca empatan del todo: el orden de extracción queda fijado por los datos y la misma
     * entrada produce siempre el mismo árbol, en cualquier máquina.
     *
     * Casos especiales:
     * - Ninguna frecuencia mayor que 0 (archivo vacío): árbol vacío.
     * - Un solo símbolo distinto: el árbol es esa única hoja. Ver generarCodigos.
     *
     * @param frecuencias arreglo de 256 posiciones: frecuencias[b] es cuántas veces aparece el byte b
     */
    public static ArbolHuffman construir(long[] frecuencias) {
        if (frecuencias.length != SIMBOLOS) {
            throw new IllegalArgumentException("Se esperaban " + SIMBOLOS + " frecuencias");
        }
        MonticuloMinimo<NodoHuffman> monticulo = new MonticuloMinimo<>((a, b) -> {
            if (a.frecuencia != b.frecuencia) {
                return Long.compare(a.frecuencia, b.frecuencia);
            }
            return Integer.compare(a.menorSimbolo, b.menorSimbolo);
        });
        int hojas = 0;
        for (int s = 0; s < SIMBOLOS; s++) {
            if (frecuencias[s] < 0) {
                throw new IllegalArgumentException("Una frecuencia no puede ser negativa");
            }
            if (frecuencias[s] > 0) {
                monticulo.insertar(new NodoHuffman(s, frecuencias[s]));
                hojas++;
            }
        }
        if (monticulo.estaVacio()) {
            return new ArbolHuffman(null, 0, true);
        }
        while (monticulo.tamanio() > 1) {
            NodoHuffman primero = monticulo.extraerMinimo();   // menor: va a la izquierda (0)
            NodoHuffman segundo = monticulo.extraerMinimo();   // siguiente: va a la derecha (1)
            monticulo.insertar(new NodoHuffman(primero, segundo));
        }
        return new ArbolHuffman(monticulo.extraerMinimo(), hojas, true);
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public boolean estaVacio() {
        return raiz == null;
    }

    /** true si el árbol tiene un solo símbolo: la raíz es una hoja. */
    public boolean esHojaUnica() {
        return raiz != null && raiz.esHoja();
    }

    /** Cantidad de hojas, que es la cantidad de símbolos distintos. */
    public int cantidadHojas() {
        return hojas;
    }

    /** Altura del árbol: 0 si está vacío, 1 si es una sola hoja. */
    public int altura() {
        return altura(raiz);
    }

    private static int altura(NodoHuffman nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    /** Frecuencia total (suma de todas las hojas); 0 si está vacío o si se leyó de un archivo. */
    public long frecuenciaTotal() {
        return raiz == null || !conFrecuencias ? 0 : raiz.frecuencia;
    }

    // ------------------------------------------------------------------
    // Códigos
    // ------------------------------------------------------------------

    /**
     * Calcula el código de cada símbolo recorriendo el árbol desde la raíz.
     * Complejidad O(cantidad de nodos), como mucho 511.
     *
     * El código se va armando en un long: bajar a la izquierda es (codigo << 1), que agrega
     * un 0 a la derecha, y bajar a la derecha es (codigo << 1) | 1, que agrega un 1. La
     * longitud cuenta cuántos bits tiene. Al llegar a una hoja se guardan los dos valores.
     *
     * Caso de un solo símbolo: la raíz es la hoja y su camino tiene 0 bits, pero un código
     * de 0 bits no se podría escribir ni contar. Por convención se le asigna el código "0"
     * (un bit).
     *
     * @param bits       arreglo de 256 posiciones donde se guarda cada código (alineado a la derecha)
     * @param longitudes arreglo de 256 posiciones donde se guarda cada longitud; queda en 0
     *                   para los símbolos que no aparecen
     */
    public void generarCodigos(long[] bits, int[] longitudes) {
        if (bits.length != SIMBOLOS || longitudes.length != SIMBOLOS) {
            throw new IllegalArgumentException("Se esperaban arreglos de " + SIMBOLOS + " posiciones");
        }
        for (int s = 0; s < SIMBOLOS; s++) {
            bits[s] = 0;
            longitudes[s] = 0;
        }
        if (raiz != null) {
            generarCodigos(raiz, 0L, 0, bits, longitudes);
        }
    }

    private static void generarCodigos(NodoHuffman nodo, long codigo, int longitud,
                                       long[] bits, int[] longitudes) {
        if (nodo.esHoja()) {
            bits[nodo.simbolo] = codigo;
            longitudes[nodo.simbolo] = longitud == 0 ? 1 : longitud;   // hoja única: código "0"
            return;
        }
        generarCodigos(nodo.izquierdo, codigo << 1, longitud + 1, bits, longitudes);
        generarCodigos(nodo.derecho, (codigo << 1) | 1, longitud + 1, bits, longitudes);
    }

    // ------------------------------------------------------------------
    // Serialización: guardar el árbol como bits y volver a leerlo
    // ------------------------------------------------------------------

    /**
     * Cantidad de bits que ocupa el árbol al escribirlo en preorden: 9 por hoja (1 bit de
     * marca y 8 del símbolo) y 1 por nodo interno. Un árbol con n hojas tiene n - 1 nodos
     * internos, así que el total es 9n + (n - 1) = 10n - 1. Vale 0 si el árbol está vacío.
     */
    public long bitsSerializado() {
        return hojas == 0 ? 0 : 10L * hojas - 1;
    }

    /**
     * Escribe el árbol en preorden: primero el nodo, después el subárbol izquierdo y después
     * el derecho. Cada hoja se escribe como un bit 1 seguido de los 8 bits del símbolo, y
     * cada nodo interno como un bit 0.
     *
     * Ejemplo: una raíz interna con 'O' a la izquierda y 'K' a la derecha se escribe
     * 0, 1 01001111, 1 01001011.
     *
     * No hace falta guardar las frecuencias: para decodificar solo importa la forma del
     * árbol y qué símbolo hay en cada hoja.
     */
    public void escribirEnPreorden(EscritorBits salida) {
        if (raiz != null) {
            escribirEnPreorden(raiz, salida);
        }
    }

    private static void escribirEnPreorden(NodoHuffman nodo, EscritorBits salida) {
        if (nodo.esHoja()) {
            salida.escribirBit(1);
            salida.escribirByte(nodo.simbolo);
            return;
        }
        salida.escribirBit(0);
        escribirEnPreorden(nodo.izquierdo, salida);
        escribirEnPreorden(nodo.derecho, salida);
    }

    /** Estado de una lectura en curso: qué símbolos ya aparecieron y cuántas hojas se leyeron. */
    private static final class Lectura {
        final LectorBits entrada;
        final int hojasEsperadas;
        final boolean[] simboloVisto = new boolean[SIMBOLOS];
        int hojasLeidas = 0;

        Lectura(LectorBits entrada, int hojasEsperadas) {
            this.entrada = entrada;
            this.hojasEsperadas = hojasEsperadas;
        }
    }

    /**
     * Profundidad máxima de un árbol válido: con 256 hojas, el árbol más desparejo posible
     * tiene una hoja por nivel y llega a profundidad 255.
     */
    private static final int PROFUNDIDAD_MAXIMA = SIMBOLOS - 1;

    /**
     * Lee un árbol escrito con escribirEnPreorden. Es la operación inversa: un bit 1 es una
     * hoja (y los 8 bits siguientes son su símbolo); un bit 0 es un nodo interno, y a
     * continuación vienen su subárbol izquierdo y su subárbol derecho.
     *
     * Como el árbol puede venir de un archivo dañado, se controla que sea válido:
     * - que no se terminen los bits a mitad del árbol;
     * - que ningún símbolo aparezca dos veces;
     * - que la cantidad de hojas sea la esperada (la que dice la cabecera del archivo);
     * - que la profundidad no pase de 255. Sin este límite, un archivo dañado lleno de bits 0
     *   haría que la recursión baje sin fin hasta desbordar la pila de llamadas.
     *
     * @param entrada        lector posicionado al principio del árbol
     * @param hojasEsperadas cantidad de símbolos distintos (0 a 256); con 0 no se lee nada
     * @return el árbol leído, sin frecuencias
     * @throws IllegalStateException si el árbol es inválido o faltan bits
     */
    public static ArbolHuffman leerEnPreorden(LectorBits entrada, int hojasEsperadas) {
        if (hojasEsperadas < 0 || hojasEsperadas > SIMBOLOS) {
            throw new IllegalStateException("Cantidad de hojas inválida: " + hojasEsperadas);
        }
        if (hojasEsperadas == 0) {
            return new ArbolHuffman(null, 0, false);
        }
        Lectura lectura = new Lectura(entrada, hojasEsperadas);
        NodoHuffman raiz = leerNodo(lectura, 0);
        if (lectura.hojasLeidas != hojasEsperadas) {
            throw new IllegalStateException("El árbol tiene " + lectura.hojasLeidas
                    + " hojas y se esperaban " + hojasEsperadas);
        }
        return new ArbolHuffman(raiz, hojasEsperadas, false);
    }

    private static NodoHuffman leerNodo(Lectura lectura, int profundidad) {
        if (profundidad > PROFUNDIDAD_MAXIMA) {
            throw new IllegalStateException("El árbol es más profundo de lo posible");
        }
        if (lectura.entrada.leerBit() == 1) {
            int simbolo = lectura.entrada.leerByte();
            if (lectura.simboloVisto[simbolo]) {
                throw new IllegalStateException("El símbolo " + nombreSimbolo(simbolo) + " aparece dos veces");
            }
            lectura.simboloVisto[simbolo] = true;
            lectura.hojasLeidas++;
            if (lectura.hojasLeidas > lectura.hojasEsperadas) {
                throw new IllegalStateException("El árbol tiene más hojas de las esperadas");
            }
            return new NodoHuffman(simbolo, 0);
        }
        NodoHuffman izquierdo = leerNodo(lectura, profundidad + 1);
        NodoHuffman derecho = leerNodo(lectura, profundidad + 1);
        return new NodoHuffman(izquierdo, derecho);
    }

    // ------------------------------------------------------------------
    // Decodificación
    // ------------------------------------------------------------------

    /**
     * Lee los bits de un código y devuelve el símbolo que representa.
     *
     * Se parte de la raíz y, por cada bit, se baja a la izquierda (0) o a la derecha (1)
     * hasta llegar a una hoja. Como el código es libre de prefijos, al llegar a una hoja el
     * código terminó sin ambigüedad. Complejidad O(longitud del código).
     *
     * Si el árbol es una sola hoja, cada símbolo se codificó con el bit 0 (ver
     * generarCodigos): se lee un bit y tiene que ser 0.
     *
     * @throws IllegalStateException si se terminan los bits a mitad de un código, si el
     *         árbol está vacío o si, con una sola hoja, el bit leído es 1
     */
    public int decodificarSimbolo(LectorBits entrada) {
        if (raiz == null) {
            throw new IllegalStateException("No se puede decodificar con un árbol vacío");
        }
        if (raiz.esHoja()) {
            if (entrada.leerBit() != 0) {
                throw new IllegalStateException("Con un solo símbolo, todos los bits deben ser 0");
            }
            return raiz.simbolo;
        }
        NodoHuffman nodo = raiz;
        while (!nodo.esHoja()) {
            nodo = entrada.leerBit() == 0 ? nodo.izquierdo : nodo.derecho;
        }
        return nodo.simbolo;
    }

    // ------------------------------------------------------------------
    // Dibujo
    // ------------------------------------------------------------------

    /**
     * Nombre legible de un byte: el carácter entre comillas si es imprimible (por ejemplo
     * 'O'), "espacio" para el 32 y el valor en hexadecimal para el resto (por ejemplo 0x0A
     * para el salto de línea).
     */
    public static String nombreSimbolo(int simbolo) {
        if (simbolo == ' ') {
            return "espacio";
        }
        if (simbolo > ' ' && simbolo < 127) {
            return "'" + (char) simbolo + "'";
        }
        String hex = Integer.toHexString(simbolo).toUpperCase();
        return "0x" + (hex.length() == 1 ? "0" + hex : hex);
    }

    /**
     * Dibujo del árbol de arriba hacia abajo, con el bit de cada rama. Los nodos internos
     * muestran entre paréntesis la suma de frecuencias, y las hojas, su símbolo y frecuencia.
     *
     *     (39)
     *     +-0 (16)
     *     |   +-0 'R' 7
     *     |   +-1 'O' 9
     *     +-1 (23)
     *         ...
     */
    public String dibujar() {
        if (raiz == null) {
            return "(árbol vacío)";
        }
        StringBuilder dibujo = new StringBuilder();
        dibujo.append(textoNodo(raiz)).append('\n');
        if (!raiz.esHoja()) {
            dibujarHijos(raiz, "", dibujo);
        }
        return dibujo.toString();
    }

    private void dibujarHijos(NodoHuffman nodo, String sangria, StringBuilder dibujo) {
        // Hijo izquierdo (bit 0): la línea vertical sigue porque falta el derecho.
        dibujo.append(sangria).append("+-0 ").append(textoNodo(nodo.izquierdo)).append('\n');
        if (!nodo.izquierdo.esHoja()) {
            dibujarHijos(nodo.izquierdo, sangria + "|   ", dibujo);
        }
        // Hijo derecho (bit 1): es el último, así que debajo ya no hace falta la línea.
        dibujo.append(sangria).append("+-1 ").append(textoNodo(nodo.derecho)).append('\n');
        if (!nodo.derecho.esHoja()) {
            dibujarHijos(nodo.derecho, sangria + "    ", dibujo);
        }
    }

    private String textoNodo(NodoHuffman nodo) {
        if (nodo.esHoja()) {
            return conFrecuencias ? nombreSimbolo(nodo.simbolo) + " " + nodo.frecuencia : nombreSimbolo(nodo.simbolo);
        }
        return conFrecuencias ? "(" + nodo.frecuencia + ")" : "( )";
    }
}
