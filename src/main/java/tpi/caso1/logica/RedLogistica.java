package tpi.caso1.logica;

import tpi.caso1.modelo.Centro;
import tpi.caso1.modelo.Ruta;
import tpi.estructuras.ArbolAVL;
import tpi.estructuras.ListaEnlazada;

/**
 * Grafo de la red logística y reglas de negocio: alta, baja y modificación de centros y rutas.
 *
 * Los vértices del grafo son los centros guardados en el árbol AVL, que funciona a la vez
 * como índice por id y como único conjunto de vértices. Las aristas son las rutas que cada
 * centro guarda en su lista de adyacencia.
 *
 * Todas las validaciones viven acá, no en el modelo. Esta clase nunca imprime ni lee del
 * teclado: cuando una operación no se puede hacer, lanza OperacionInvalidaException con
 * el mensaje que se le muestra al usuario.
 */
public class RedLogistica {

    private final ArbolAVL<String, Centro> centros = new ArbolAVL<>();

    // ------------------------------------------------------------------
    // Centros
    // ------------------------------------------------------------------

    /**
     * Registra un centro nuevo. Complejidad O(log n).
     *
     * @param id          identificador único; se normaliza antes de guardarlo
     * @param descripcion nombre o detalle del centro
     * @throws OperacionInvalidaException si el id o la descripción no son válidos,
     *                                    o si ya existe un centro con ese id
     */
    public void altaCentro(String id, String descripcion) {
        String idNormalizado = normalizarId(id);
        String descripcionValida = validarDescripcion(descripcion);

        // El árbol ya rechaza las claves repetidas: no hace falta buscar antes de insertar.
        Centro centro = new Centro(idNormalizado, descripcionValida);
        if (!centros.insertar(idNormalizado, centro)) {
            throw new OperacionInvalidaException("Ya existe un centro con id " + idNormalizado);
        }
    }

    /**
     * Cambia la descripción de un centro. Complejidad O(log n).
     *
     * El id no se puede modificar porque es la clave del árbol: cambiarlo rompería el orden.
     * Para cambiar un id hay que dar de baja el centro y volver a darlo de alta.
     *
     * @throws OperacionInvalidaException si el centro no existe o la descripción está vacía
     */
    public void modificarCentro(String id, String nuevaDescripcion) {
        Centro centro = consultarCentro(id);
        centro.setDescripcion(validarDescripcion(nuevaDescripcion));
    }

    /**
     * Busca un centro por id. Complejidad O(log n).
     *
     * @return el centro encontrado (nunca null)
     * @throws OperacionInvalidaException si el id no es válido o el centro no existe
     */
    public Centro consultarCentro(String id) {
        String idNormalizado = normalizarId(id);
        Centro centro = centros.buscar(idNormalizado);
        if (centro == null) {
            throw new OperacionInvalidaException("No existe un centro con id " + idNormalizado);
        }
        return centro;
    }

    /**
     * Indica si hay un centro registrado con ese id. Complejidad O(log n).
     *
     * @throws OperacionInvalidaException si el id no es válido
     */
    public boolean existeCentro(String id) {
        return centros.contiene(normalizarId(id));
    }

    /**
     * Devuelve los centros ordenados por id, usando el recorrido inorden del árbol.
     * Complejidad O(n).
     *
     * @return una lista nueva: modificarla no cambia la red
     */
    public ListaEnlazada<Centro> listarCentros() {
        return centros.valoresEnOrden();
    }

    /**
     * Da de baja un centro y elimina todas las rutas que lo tocan.
     *
     * No alcanza con mirar la lista del centro: una ruta de un solo sentido que llega a él
     * está guardada solamente en la lista de su origen. Por eso se recorren todos los centros.
     *
     * Complejidad O(V + E): se pasa una vez por cada centro y por cada lista de adyacencia.
     *
     * @throws OperacionInvalidaException si el id no es válido o el centro no existe
     */
    public void bajaCentro(String id) {
        Centro centro = consultarCentro(id);

        for (Centro otro : centros.valoresEnOrden()) {
            quitarRutasQueTocan(otro, centro);
        }
        centros.eliminar(centro.getId());
    }

    /** Devuelve la cantidad de centros registrados. Complejidad O(1). */
    public int cantidadCentros() {
        return centros.tamanio();
    }

    // ------------------------------------------------------------------
    // Normalización y validaciones
    // ------------------------------------------------------------------

    /**
     * Lleva un id a su forma única: sin espacios en los extremos y en mayúsculas.
     * Así, " a " y "A" son el mismo centro.
     *
     * @return el id normalizado
     * @throws OperacionInvalidaException si queda vacío o contiene punto y coma
     */
    public static String normalizarId(String id) {
        if (id == null) {
            throw new OperacionInvalidaException("El identificador no puede estar vacío ni contener ;");
        }
        String normalizado = id.trim().toUpperCase();
        if (normalizado.isEmpty() || normalizado.contains(";")) {
            throw new OperacionInvalidaException("El identificador no puede estar vacío ni contener ;");
        }
        return normalizado;
    }

    /**
     * Comprueba que la descripción tenga texto.
     *
     * @return la descripción sin espacios en los extremos
     * @throws OperacionInvalidaException si es null o solo tiene espacios
     */
    private static String validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new OperacionInvalidaException("La descripción no puede estar vacía");
        }
        return descripcion.trim();
    }

    /**
     * Quita de la lista de adyacencia de un centro las rutas que empiezan o terminan
     * en el centro indicado.
     *
     * Se hace en dos pasos porque una lista no se puede modificar mientras se la recorre:
     * primero se juntan las rutas a quitar y después se las elimina.
     *
     * @param duenio centro cuya lista se limpia
     * @param centro centro que se está dando de baja
     */
    private void quitarRutasQueTocan(Centro duenio, Centro centro) {
        ListaEnlazada<Ruta> aQuitar = new ListaEnlazada<>();
        for (Ruta ruta : duenio.getRutas()) {
            if (ruta.getOrigen() == centro || ruta.getDestino() == centro) {
                aQuitar.agregarAlFinal(ruta);
            }
        }
        for (Ruta ruta : aQuitar) {
            duenio.getRutas().eliminar(ruta);
        }
    }
}
