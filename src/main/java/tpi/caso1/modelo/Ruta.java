package tpi.caso1.modelo;

/**
 * Ruta entre dos centros: arista ponderada por tiempo en minutos, bidireccional o de un sentido.
 *
 * Una ruta bidireccional es un único objeto que está en la lista de adyacencia de sus
 * dos extremos. Por eso, al deshabilitarla o cambiarle el tiempo, el cambio vale para
 * los dos sentidos sin tener que actualizar nada más:
 *
 *   Centro A: rutas --+
 *                     +--> [ A <-> B (8 min) ]
 *   Centro B: rutas --+
 *
 * Una ruta de un solo sentido está únicamente en la lista de su origen.
 *
 * Esta clase solo guarda datos y responde preguntas sobre la ruta. Las validaciones
 * (tiempo negativo, ruta duplicada, etc.) las hace RedLogistica.
 *
 * No redefine equals: dos rutas son la misma solo si son el mismo objeto. Eso es lo
 * que permite quitar de las dos listas exactamente la ruta que se quiere eliminar.
 */
public class Ruta {

    private final Centro origen;
    private final Centro destino;
    private int tiempo;                    // peso de la arista, en minutos
    private final boolean bidireccional;
    private boolean activa;                // false mientras está dada de baja temporalmente

    /**
     * Crea una ruta activa.
     *
     * @param origen        centro donde empieza la ruta
     * @param destino       centro donde termina la ruta
     * @param tiempo        minutos que tarda el recorrido
     * @param bidireccional true si se puede recorrer en los dos sentidos
     */
    public Ruta(Centro origen, Centro destino, int tiempo, boolean bidireccional) {
        this.origen = origen;
        this.destino = destino;
        this.tiempo = tiempo;
        this.bidireccional = bidireccional;
        this.activa = true;
    }

    public Centro getOrigen() {
        return origen;
    }

    public Centro getDestino() {
        return destino;
    }

    public int getTiempo() {
        return tiempo;
    }

    public void setTiempo(int minutos) {
        this.tiempo = minutos;
    }

    public boolean isBidireccional() {
        return bidireccional;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    /**
     * Indica si hoy se puede usar esta ruta para salir del centro dado.
     * Es la pregunta que hacen Dijkstra, BFS y DFS antes de avanzar por una ruta:
     * descarta las rutas deshabilitadas y las que irían en contramano.
     *
     * @return true si la ruta está activa y se puede recorrer partiendo de c
     */
    public boolean permiteSalirDesde(Centro c) {
        return activa && (c == origen || (bidireccional && c == destino));
    }

    /**
     * Devuelve el extremo de la ruta que no es c. Una ruta bidireccional se guarda
     * igual en los dos centros, así que este método dice quién es el vecino
     * sin importar desde qué lado se la mira.
     *
     * @throws IllegalArgumentException si c no es un extremo de esta ruta
     */
    public Centro otroExtremo(Centro c) {
        if (c == origen) {
            return destino;
        }
        if (c == destino) {
            return origen;
        }
        throw new IllegalArgumentException("El centro " + c.getId() + " no es un extremo de la ruta " + this);
    }

    /**
     * Indica si la ruta une los dos centros en ese sentido, esté activa o no.
     * Sirve para encontrar una ruta deshabilitada, por ejemplo para volver a habilitarla.
     *
     * @return true si por esta ruta se va de desde a hacia
     */
    public boolean conecta(Centro desde, Centro hacia) {
        boolean sentidoDirecto = desde == origen && hacia == destino;
        boolean sentidoInverso = bidireccional && desde == destino && hacia == origen;
        return sentidoDirecto || sentidoInverso;
    }

    /** Representación en texto, por ejemplo "A <-> B (8 min)" o "D -> F (6 min) [DESHABILITADA]". */
    @Override
    public String toString() {
        String flecha = bidireccional ? " <-> " : " -> ";
        String texto = origen.getId() + flecha + destino.getId() + " (" + tiempo + " min)";
        return activa ? texto : texto + " [DESHABILITADA]";
    }
}
