package tpi.caso1.logica;

import tpi.caso1.modelo.Centro;
import tpi.caso1.modelo.Ruta;
import tpi.estructuras.ArbolAVL;
import tpi.estructuras.ListaEnlazada;

import java.util.Comparator;

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
    // Rutas
    // ------------------------------------------------------------------

    /**
     * Registra una ruta nueva entre dos centros. Complejidad O(log V + grado).
     *
     * Se crea un único objeto Ruta. Se agrega a la lista de adyacencia del origen y,
     * si es bidireccional, ese mismo objeto se agrega también a la lista del destino.
     * Así, deshabilitarla o cambiarle el tiempo afecta a los dos sentidos a la vez.
     *
     * Cada lista se mantiene ordenada por el id del vecino, para que los recorridos
     * y los desempates de Dijkstra den siempre el mismo resultado.
     *
     * Se permiten dos rutas de un solo sentido opuestas (A hacia B y B hacia A),
     * porque es un caso real. Lo que no se permite es repetir un mismo sentido.
     *
     * @param origen        id del centro donde empieza la ruta
     * @param destino       id del centro donde termina la ruta
     * @param tiempo        minutos, mayor o igual a 0
     * @param bidireccional true si se puede recorrer en los dos sentidos
     * @throws OperacionInvalidaException si algún centro no existe, el tiempo es negativo,
     *                                    los dos centros son el mismo o la ruta ya existe
     */
    public void altaRuta(String origen, String destino, int tiempo, boolean bidireccional) {
        Centro desde = consultarCentro(origen);
        Centro hasta = consultarCentro(destino);
        validarTiempo(tiempo);
        if (desde == hasta) {
            throw new OperacionInvalidaException("Una ruta no puede unir un centro consigo mismo");
        }

        // Duplicado: ya se puede ir de origen a destino, o la nueva es bidireccional
        // y ya se puede ir de destino a origen (la nueva cubriría ese sentido otra vez).
        boolean repiteIda = buscarRuta(desde, hasta) != null;
        boolean repiteVuelta = bidireccional && buscarRuta(hasta, desde) != null;
        if (repiteIda || repiteVuelta) {
            throw new OperacionInvalidaException(
                    "Ya existe una ruta entre " + desde.getId() + " y " + hasta.getId());
        }

        Ruta ruta = new Ruta(desde, hasta, tiempo, bidireccional);
        desde.getRutas().agregarOrdenado(ruta, porIdDelVecino(desde));
        if (bidireccional) {
            hasta.getRutas().agregarOrdenado(ruta, porIdDelVecino(hasta));
        }
    }

    /**
     * Cambia el tiempo de una ruta. Si es bidireccional, el cambio vale para los dos
     * sentidos, porque es un único objeto. Complejidad O(log V + grado).
     *
     * @throws OperacionInvalidaException si algún centro no existe, la ruta no existe
     *                                    o el tiempo es negativo
     */
    public void modificarTiempoRuta(String origen, String destino, int nuevoTiempo) {
        Ruta ruta = rutaExistente(origen, destino);
        validarTiempo(nuevoTiempo);
        ruta.setTiempo(nuevoTiempo);
    }

    /**
     * Da de baja temporalmente una ruta: sigue registrada, pero los algoritmos la ignoran
     * hasta que se la vuelva a habilitar. Complejidad O(log V + grado).
     *
     * @return true si la ruta pasó de activa a deshabilitada; false si ya estaba deshabilitada
     * @throws OperacionInvalidaException si algún centro no existe o la ruta no existe
     */
    public boolean deshabilitarRuta(String origen, String destino) {
        Ruta ruta = rutaExistente(origen, destino);
        if (!ruta.isActiva()) {
            return false;
        }
        ruta.setActiva(false);
        return true;
    }

    /**
     * Vuelve a habilitar una ruta dada de baja temporalmente. Complejidad O(log V + grado).
     *
     * @return true si la ruta pasó de deshabilitada a activa; false si ya estaba activa
     * @throws OperacionInvalidaException si algún centro no existe o la ruta no existe
     */
    public boolean habilitarRuta(String origen, String destino) {
        Ruta ruta = rutaExistente(origen, destino);
        if (ruta.isActiva()) {
            return false;
        }
        ruta.setActiva(true);
        return true;
    }

    /**
     * Elimina una ruta definitivamente. Se la quita de la lista del origen y, si es
     * bidireccional, también de la del destino. Complejidad O(log V + grado).
     *
     * Una ruta bidireccional se puede indicar en cualquiera de los dos sentidos:
     * eliminarRuta("B", "A") borra la ruta A con B.
     *
     * @throws OperacionInvalidaException si algún centro no existe o la ruta no existe
     */
    public void eliminarRuta(String origen, String destino) {
        Ruta ruta = rutaExistente(origen, destino);
        ruta.getOrigen().getRutas().eliminar(ruta);
        if (ruta.isBidireccional()) {
            ruta.getDestino().getRutas().eliminar(ruta);
        }
    }

    /**
     * Deshabilita todas las rutas que tocan a un centro, tanto las que salen como las que
     * llegan, para dejarlo incomunicado. Las rutas no se borran: se pueden volver a habilitar.
     *
     * Se recorren todos los centros porque una ruta de un solo sentido que llega al centro
     * está guardada solamente en la lista de su origen.
     *
     * Solo se cuentan las rutas que pasan de activa a deshabilitada. Una bidireccional
     * aparece en dos listas, pero la segunda vez ya está deshabilitada y no se cuenta de nuevo.
     *
     * Complejidad O(V + E).
     *
     * @return cantidad de rutas que se deshabilitaron
     * @throws OperacionInvalidaException si el id no es válido o el centro no existe
     */
    public int aislarCentro(String id) {
        Centro centro = consultarCentro(id);
        int deshabilitadas = 0;
        for (Centro otro : centros.valoresEnOrden()) {
            for (Ruta ruta : otro.getRutas()) {
                boolean tocaAlCentro = ruta.getOrigen() == centro || ruta.getDestino() == centro;
                if (tocaAlCentro && ruta.isActiva()) {
                    ruta.setActiva(false);
                    deshabilitadas++;
                }
            }
        }
        return deshabilitadas;
    }

    /**
     * Busca la ruta que permite ir de origen a destino, esté activa o no.
     * Una ruta bidireccional se encuentra en los dos sentidos. Complejidad O(log V + grado).
     *
     * @return la ruta, o null si no hay ninguna que vaya de origen a destino
     * @throws OperacionInvalidaException si algún centro no existe
     */
    public Ruta buscarRuta(String origen, String destino) {
        return buscarRuta(consultarCentro(origen), consultarCentro(destino));
    }

    /**
     * Devuelve todas las rutas, cada una una sola vez, ordenadas por id del origen y
     * después por id del destino. Complejidad O(V + E).
     *
     * Una ruta bidireccional está en dos listas; para no repetirla, se la agrega solo
     * cuando se está recorriendo la lista de su origen.
     */
    public ListaEnlazada<Ruta> listarRutas() {
        ListaEnlazada<Ruta> rutas = new ListaEnlazada<>();
        for (Centro centro : centros.valoresEnOrden()) {
            for (Ruta ruta : centro.getRutas()) {
                if (ruta.getOrigen() == centro) {
                    rutas.agregarAlFinal(ruta);
                }
            }
        }
        return rutas;
    }

    /**
     * Devuelve la cantidad de rutas registradas, activas o no. Complejidad O(V + E),
     * porque se cuentan recorriendo la red en lugar de llevar un contador aparte que
     * habría que mantener en cada alta y baja.
     */
    public int cantidadRutas() {
        return listarRutas().tamanio();
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
     * Comprueba que un tiempo no sea negativo. Dijkstra necesita pesos no negativos.
     *
     * @throws OperacionInvalidaException si el tiempo es menor que 0
     */
    private static void validarTiempo(int tiempo) {
        if (tiempo < 0) {
            throw new OperacionInvalidaException("El tiempo debe ser un entero mayor o igual a 0");
        }
    }

    /**
     * Busca una ruta que debe existir, para las operaciones que la modifican.
     *
     * @throws OperacionInvalidaException si algún centro no existe o no hay ruta de origen a destino
     */
    private Ruta rutaExistente(String origen, String destino) {
        Centro desde = consultarCentro(origen);
        Centro hasta = consultarCentro(destino);
        Ruta ruta = buscarRuta(desde, hasta);
        if (ruta == null) {
            throw new OperacionInvalidaException(
                    "No existe una ruta de " + desde.getId() + " a " + hasta.getId());
        }
        return ruta;
    }

    /**
     * Busca en la lista de adyacencia de desde una ruta que lleve a hasta, esté activa o no.
     * Alcanza con mirar la lista de desde: toda ruta por la que se puede salir de un centro
     * está en la lista de ese centro.
     *
     * @return la ruta, o null si no hay
     */
    private Ruta buscarRuta(Centro desde, Centro hasta) {
        return desde.getRutas().buscar(ruta -> ruta.conecta(desde, hasta));
    }

    /**
     * Criterio para ordenar la lista de adyacencia de un centro: por el id del vecino,
     * es decir, del extremo de la ruta que no es ese centro.
     */
    private static Comparator<Ruta> porIdDelVecino(Centro centro) {
        return (r1, r2) -> r1.otroExtremo(centro).getId().compareTo(r2.otroExtremo(centro).getId());
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
