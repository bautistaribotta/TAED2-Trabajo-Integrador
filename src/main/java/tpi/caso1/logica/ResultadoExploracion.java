package tpi.caso1.logica;

import tpi.caso1.modelo.Centro;
import tpi.estructuras.ListaEnlazada;

/**
 * Resultado de un recorrido de exploración: criterio, orden de visita, niveles y centros no alcanzados.
 *
 * A diferencia de ResultadoCamino, acá no hay tiempos: un recorrido de exploración dice
 * qué centros se pueden alcanzar desde el origen y en qué orden se descubren, pero no
 * calcula cuál es el camino más rápido.
 *
 * Es inmutable: lo crea Exploracion y después solo se lee.
 */
public class ResultadoExploracion {

    /** Criterio con que se recorrió la red. */
    public enum Criterio {
        /** Por anchura: nivel por nivel, primero los vecinos más cercanos en cantidad de tramos. */
        BFS,
        /** En profundidad: se avanza lo más posible por un camino antes de retroceder. */
        DFS
    }

    private final Criterio criterio;
    private final String idOrigen;                      // ya normalizado
    private final boolean origenExiste;
    private final ListaEnlazada<Centro> ordenDeVisita;
    private final ListaEnlazada<Integer> niveles;       // uno por cada centro de ordenDeVisita
    private final ListaEnlazada<Centro> noAlcanzados;

    /**
     * Solo lo usa Exploracion, por eso tiene visibilidad de paquete.
     */
    ResultadoExploracion(Criterio criterio, String idOrigen, boolean origenExiste,
                         ListaEnlazada<Centro> ordenDeVisita, ListaEnlazada<Integer> niveles,
                         ListaEnlazada<Centro> noAlcanzados) {
        this.criterio = criterio;
        this.idOrigen = idOrigen;
        this.origenExiste = origenExiste;
        this.ordenDeVisita = ordenDeVisita;
        this.niveles = niveles;
        this.noAlcanzados = noAlcanzados;
    }

    public Criterio getCriterio() {
        return criterio;
    }

    /** Id del origen consultado, normalizado. */
    public String getIdOrigen() {
        return idOrigen;
    }

    /** false si el centro de origen no está registrado; en ese caso las listas están vacías. */
    public boolean isOrigenExiste() {
        return origenExiste;
    }

    /** Centros en el orden en que el recorrido los visitó. El primero es el origen. */
    public ListaEnlazada<Centro> getOrdenDeVisita() {
        return ordenDeVisita;
    }

    /**
     * Para cada centro de getOrdenDeVisita, en el mismo orden:
     * en BFS, la cantidad de tramos desde el origen;
     * en DFS, la profundidad en el árbol de recorrido.
     */
    public ListaEnlazada<Integer> getNiveles() {
        return niveles;
    }

    /** Centros a los que no se puede llegar desde el origen, ordenados por id. */
    public ListaEnlazada<Centro> getNoAlcanzados() {
        return noAlcanzados;
    }
}
