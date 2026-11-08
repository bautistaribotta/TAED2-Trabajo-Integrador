package tpi.caso1.logica;

import tpi.caso1.modelo.Centro;
import tpi.caso1.modelo.Ruta;
import tpi.estructuras.ListaEnlazada;
import tpi.estructuras.MonticuloMinimo;
import tpi.estructuras.Pila;

/**
 * Cálculo del camino de menor tiempo total entre dos centros.
 *
 * Idea del algoritmo: se mantiene, para cada centro, el menor tiempo conocido desde el
 * origen. En cada paso se toma el centro no procesado con menor tiempo (el heap lo da en
 * O(log n)) y se lo da por definitivo: como los tiempos no son negativos, ningún camino
 * que pase por centros más lejanos puede mejorarlo. Después se intenta mejorar a sus
 * vecinos pasando por él ("relajar" las rutas).
 *
 * Condición: los tiempos tienen que ser mayores o iguales a 0. RedLogistica lo garantiza
 * al rechazar tiempos negativos. Con pesos negativos haría falta otro algoritmo
 * (Bellman-Ford).
 *
 * Variante "perezosa": cuando se mejora el tiempo de un centro no se actualiza su entrada
 * en el heap, sino que se inserta una entrada nueva. Las entradas viejas se descartan al
 * extraerlas, porque el centro ya figura como procesado. Así el heap no necesita la
 * operación de "disminuir clave".
 *
 * Complejidad: O((V + E) log E), que es O((V + E) log V). Memoria O(V + E).
 */
public class Dijkstra {

    /**
     * Entrada del heap: un centro con el tiempo con que se lo alcanzó cuando se insertó.
     * Un mismo centro puede tener varias entradas; vale la de menor tiempo.
     */
    private static final class Candidato {
        final Centro centro;
        final int distancia;

        Candidato(Centro centro, int distancia) {
            this.centro = centro;
            this.distancia = distancia;
        }
    }

    private Dijkstra() {
        // Clase de utilidad: no se crean objetos, se usa calcular directamente.
    }

    /**
     * Calcula el camino de menor tiempo total de origen a destino.
     *
     * Validaciones previas, en este orden: origen inexistente, destino inexistente y
     * origen igual al destino. Ninguna lanza excepción: se informan en el estado del resultado.
     *
     * @param red     red sobre la que se calcula
     * @param origen  id del centro de partida
     * @param destino id del centro de llegada
     * @return el resultado con su estado, el camino y los costos
     * @throws OperacionInvalidaException si alguno de los ids está vacío o contiene punto y coma
     */
    public static ResultadoCamino calcular(RedLogistica red, String origen, String destino) {
        String idOrigen = RedLogistica.normalizarId(origen);
        String idDestino = RedLogistica.normalizarId(destino);

        // 1. Validaciones previas
        if (!red.existeCentro(idOrigen)) {
            return ResultadoCamino.sinCamino(ResultadoCamino.Estado.ORIGEN_INEXISTENTE, idOrigen, idDestino, 0);
        }
        if (!red.existeCentro(idDestino)) {
            return ResultadoCamino.sinCamino(ResultadoCamino.Estado.DESTINO_INEXISTENTE, idOrigen, idDestino, 0);
        }
        Centro inicio = red.consultarCentro(idOrigen);
        Centro fin = red.consultarCentro(idDestino);
        if (inicio == fin) {
            ListaEnlazada<Centro> camino = new ListaEnlazada<>();
            ListaEnlazada<Integer> costos = new ListaEnlazada<>();
            camino.agregarAlFinal(inicio);
            costos.agregarAlFinal(0);
            return new ResultadoCamino(ResultadoCamino.Estado.MISMO_CENTRO, idOrigen, idDestino, camino, costos, 0, 0);
        }

        // 2. Preparación: cada centro recibe un índice de 0 a n-1 para usar arreglos.
        // Se reindexa en cada consulta, así no importan los huecos que dejan las bajas.
        ListaEnlazada<Centro> todos = red.listarCentros();
        int n = todos.tamanio();
        int i = 0;
        for (Centro centro : todos) {
            centro.setIndice(i);
            i++;
        }

        int[] distancia = new int[n];        // menor tiempo conocido desde el origen
        Centro[] anterior = new Centro[n];   // desde qué centro se llega con ese tiempo
        boolean[] procesado = new boolean[n];// true cuando su distancia ya es definitiva
        for (int k = 0; k < n; k++) {
            distancia[k] = Integer.MAX_VALUE;  // "infinito": todavía no se lo alcanzó
        }

        // Menor distancia primero; si empatan, menor id. Así el resultado es reproducible.
        MonticuloMinimo<Candidato> heap = new MonticuloMinimo<>((a, b) -> {
            if (a.distancia != b.distancia) {
                return Integer.compare(a.distancia, b.distancia);
            }
            return a.centro.getId().compareTo(b.centro.getId());
        });

        // 3. Algoritmo
        distancia[inicio.getIndice()] = 0;
        heap.insertar(new Candidato(inicio, 0));
        int centrosProcesados = 0;

        while (!heap.estaVacio()) {
            Candidato actual = heap.extraerMinimo();
            Centro u = actual.centro;
            if (procesado[u.getIndice()]) {
                continue;   // entrada vieja: ya se procesó con un tiempo menor
            }
            procesado[u.getIndice()] = true;
            centrosProcesados++;
            if (u == fin) {
                break;      // el destino ya es definitivo: no hace falta seguir
            }

            for (Ruta ruta : u.getRutas()) {
                if (!ruta.permiteSalirDesde(u)) {
                    continue;   // deshabilitada, o de un solo sentido en contramano
                }
                Centro v = ruta.otroExtremo(u);
                // Se suma en long para que un tiempo muy grande no desborde el int.
                long nueva = (long) distancia[u.getIndice()] + ruta.getTiempo();
                // Estrictamente menor: ante dos caminos de igual costo queda el primero encontrado.
                if (nueva < distancia[v.getIndice()]) {
                    distancia[v.getIndice()] = (int) nueva;
                    anterior[v.getIndice()] = u;
                    heap.insertar(new Candidato(v, (int) nueva));
                }
            }
        }

        if (distancia[fin.getIndice()] == Integer.MAX_VALUE) {
            return ResultadoCamino.sinCamino(ResultadoCamino.Estado.SIN_CAMINO, idOrigen, idDestino, centrosProcesados);
        }

        // 4. Reconstrucción: desde el destino se sigue "anterior" hacia atrás. El camino sale
        // invertido, por eso se apila y al desapilar queda en orden origen -> destino.
        Pila<Centro> pila = new Pila<>();
        Centro paso = fin;
        while (paso != null) {
            pila.apilar(paso);
            paso = anterior[paso.getIndice()];
        }
        ListaEnlazada<Centro> camino = new ListaEnlazada<>();
        ListaEnlazada<Integer> costos = new ListaEnlazada<>();
        while (!pila.estaVacia()) {
            Centro centro = pila.desapilar();
            camino.agregarAlFinal(centro);
            costos.agregarAlFinal(distancia[centro.getIndice()]);
        }

        return new ResultadoCamino(ResultadoCamino.Estado.ENCONTRADO, idOrigen, idDestino,
                camino, costos, distancia[fin.getIndice()], centrosProcesados);
    }
}
