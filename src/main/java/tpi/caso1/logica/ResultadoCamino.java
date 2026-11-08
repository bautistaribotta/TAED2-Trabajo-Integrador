package tpi.caso1.logica;

import tpi.caso1.modelo.Centro;
import tpi.estructuras.ListaEnlazada;

/**
 * Resultado de una consulta de camino mínimo: estado, camino, costos acumulados y costo total.
 *
 * La lógica no imprime nada: Dijkstra devuelve este objeto y la consola decide cómo
 * mostrarlo. El estado indica qué pasó con la consulta, y así se cubren todos los casos
 * que pide el enunciado (camino encontrado, sin camino, centro no registrado y mismo centro)
 * sin usar excepciones para situaciones que son respuestas normales.
 *
 * Es inmutable: se crea una sola vez, dentro del paquete logica, y después solo se lee.
 */
public class ResultadoCamino {

    /** Qué pasó con la consulta. */
    public enum Estado {
        /** Hay camino: camino, costos y costo total tienen datos. */
        ENCONTRADO,
        /** El origen y el destino son el mismo centro: camino de un solo centro y costo 0. */
        MISMO_CENTRO,
        /** Los dos centros existen, pero no hay rutas activas que los unan. */
        SIN_CAMINO,
        /** El centro de origen no está registrado. */
        ORIGEN_INEXISTENTE,
        /** El centro de destino no está registrado. */
        DESTINO_INEXISTENTE
    }

    private final Estado estado;
    private final String idOrigen;                          // ya normalizado
    private final String idDestino;                         // ya normalizado
    private final ListaEnlazada<Centro> camino;             // origen ... destino; vacía si no hay
    private final ListaEnlazada<Integer> costosAcumulados;  // un valor por cada centro del camino
    private final int costoTotal;                           // -1 si no hay camino
    private final int centrosProcesados;                    // centros que Dijkstra sacó del heap

    /**
     * Solo lo usa Dijkstra, por eso tiene visibilidad de paquete.
     */
    ResultadoCamino(Estado estado, String idOrigen, String idDestino,
                    ListaEnlazada<Centro> camino, ListaEnlazada<Integer> costosAcumulados,
                    int costoTotal, int centrosProcesados) {
        this.estado = estado;
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.camino = camino;
        this.costosAcumulados = costosAcumulados;
        this.costoTotal = costoTotal;
        this.centrosProcesados = centrosProcesados;
    }

    /**
     * Crea un resultado sin camino (centro inexistente o sin recorrido posible).
     */
    static ResultadoCamino sinCamino(Estado estado, String idOrigen, String idDestino, int centrosProcesados) {
        return new ResultadoCamino(estado, idOrigen, idDestino,
                new ListaEnlazada<>(), new ListaEnlazada<>(), -1, centrosProcesados);
    }

    public Estado getEstado() {
        return estado;
    }

    /** Id del origen consultado, normalizado (por ejemplo "a" queda como "A"). */
    public String getIdOrigen() {
        return idOrigen;
    }

    /** Id del destino consultado, normalizado. */
    public String getIdDestino() {
        return idDestino;
    }

    /** Centros del camino en orden, del origen al destino. Vacía si no hay camino. */
    public ListaEnlazada<Centro> getCamino() {
        return camino;
    }

    /**
     * Costo acumulado al llegar a cada centro del camino, en el mismo orden que getCamino.
     * El primero siempre es 0 (el origen) y el último es el costo total.
     */
    public ListaEnlazada<Integer> getCostosAcumulados() {
        return costosAcumulados;
    }

    /** Tiempo total en minutos, o -1 si no hay camino. */
    public int getCostoTotal() {
        return costoTotal;
    }

    /**
     * Cantidad de centros que Dijkstra extrajo del heap y dio por definitivos.
     * Sirve para comparar cuánto trabajo hizo el algoritmo en cada consulta.
     */
    public int getCentrosProcesados() {
        return centrosProcesados;
    }
}
