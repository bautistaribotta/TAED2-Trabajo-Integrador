package tpi.caso1.logica;

/**
 * Carga los centros y rutas del enunciado usando las operaciones de alta de la red.
 *
 * Solo se cargan datos de entrada (7 centros y 10 rutas bidireccionales). No se carga
 * ningún resultado: los caminos y tiempos que muestra el programa se calculan siempre
 * al momento de la consulta.
 *
 * Como usa altaCentro y altaRuta, los datos pasan por las mismas validaciones que si los
 * ingresara el usuario desde el menú.
 */
public final class DatosIniciales {

    /** Centros del enunciado: id y descripción. */
    private static final String[][] CENTROS = {
            {"A", "Depósito central"},
            {"B", "Hospital Norte"},
            {"C", "Hospital Sur"},
            {"D", "Base Este"},
            {"E", "Centro Oeste"},
            {"F", "Centro Rural"},
            {"G", "Centro de apoyo"},
    };

    /** Rutas del enunciado: origen, destino y tiempo en minutos. Todas son bidireccionales. */
    private static final Object[][] RUTAS = {
            {"A", "B", 8},
            {"A", "C", 5},
            {"B", "C", 3},
            {"B", "D", 9},
            {"C", "D", 7},
            {"C", "E", 4},
            {"D", "F", 6},
            {"E", "F", 11},
            {"E", "G", 8},
            {"F", "G", 2},
    };

    private DatosIniciales() {
        // Clase de utilidad: no se crean objetos.
    }

    /**
     * Agrega a la red los 7 centros y las 10 rutas bidireccionales del enunciado.
     *
     * @param red red vacía donde se cargan los datos
     * @throws OperacionInvalidaException si la red ya tiene alguno de esos centros o rutas
     */
    public static void cargar(RedLogistica red) {
        for (String[] centro : CENTROS) {
            red.altaCentro(centro[0], centro[1]);
        }
        for (Object[] ruta : RUTAS) {
            red.altaRuta((String) ruta[0], (String) ruta[1], (Integer) ruta[2], true);
        }
    }
}
