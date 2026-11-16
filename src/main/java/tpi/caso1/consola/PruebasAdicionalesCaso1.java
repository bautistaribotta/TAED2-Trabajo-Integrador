package tpi.caso1.consola;

import tpi.caso1.logica.DatosIniciales;
import tpi.caso1.logica.Dijkstra;
import tpi.caso1.logica.Exploracion;
import tpi.caso1.logica.OperacionInvalidaException;
import tpi.caso1.logica.RedLogistica;
import tpi.caso1.logica.ResultadoCamino;
import tpi.caso1.logica.ResultadoExploracion;
import tpi.caso1.modelo.Centro;

/**
 * Pruebas adicionales del Caso 1: casos límite y reglas de validación que no están entre
 * las pruebas obligatorias del enunciado.
 *
 * Cada prueba trabaja sobre una red nueva con los datos iniciales, así no depende de las
 * demás ni de lo que haya cambiado el usuario. Para cada una se muestra qué se hizo, qué
 * se esperaba y qué devolvió realmente el programa. Lo esperado se escribió antes de
 * ejecutar (calculado a mano sobre la red del enunciado); lo obtenido sale de la ejecución.
 */
final class PruebasAdicionalesCaso1 {

    private int numero;
    private int correctas;

    /** Ejecuta todas las pruebas e imprime el resultado de cada una y un resumen. */
    void ejecutar() {
        System.out.println("===== PRUEBAS ADICIONALES DEL CASO 1 =====");
        System.out.println("Cada prueba usa una red nueva con los datos iniciales. La red en uso no se modifica.");

        // 1. Ruta de un solo sentido: se puede ir pero no volver por ella.
        RedLogistica red = redNueva();
        red.altaCentro("H", "Puesto sanitario");
        red.altaRuta("A", "H", 4, false);
        String ida = camino(red, "A", "H");
        String vuelta = camino(red, "H", "A");
        verificar("Ruta de un solo sentido A -> H (4 min)",
                "A -> H: A -> H, 4 min | H -> A: sin recorrido",
                "A -> H: " + ida + " | H -> A: " + vuelta);

        // 2. Deshabilitar una ruta cambia el camino; habilitarla lo devuelve.
        red = redNueva();
        boolean primera = red.deshabilitarRuta("C", "D");
        String sinCD = camino(red, "A", "F");
        boolean segunda = red.deshabilitarRuta("C", "D");
        red.habilitarRuta("C", "D");
        String conCD = camino(red, "A", "F");
        verificar("Deshabilitar C-D, repetir A -> F y volver a habilitarla",
                "deshabilitada: A -> C -> E -> G -> F, 19 min | segunda vez: ya estaba deshabilitada | "
                        + "habilitada: A -> C -> D -> F, 18 min",
                "deshabilitada: " + sinCD + " | segunda vez: "
                        + (primera && !segunda ? "ya estaba deshabilitada" : "se volvió a deshabilitar")
                        + " | habilitada: " + conCD);

        // 3. Identificador duplicado.
        verificar("Alta de un centro con id duplicado (A)",
                "rechazo: Ya existe un centro con id A",
                error(() -> redNueva().altaCentro("A", "Otro depósito")));

        // 4. Identificador con minúsculas y espacios: se normaliza.
        red = redNueva();
        String duplicadoMinuscula = error(red::altaCentro, " a ", "Otro depósito");
        Centro consultado = red.consultarCentro("  b ");
        String caminoMinusculas = camino(red, " a", "f ");
        verificar("Id con minúsculas y espacios (\" a \", \"  b \", \" a\" -> \"f \")",
                "alta de \" a \": rechazo por duplicado | \"  b \" encuentra B - Hospital Norte | "
                        + "camino: A -> C -> D -> F, 18 min",
                "alta de \" a \": " + (duplicadoMinuscula.startsWith("rechazo: Ya existe") ? "rechazo por duplicado" : duplicadoMinuscula)
                        + " | \"  b \" encuentra " + consultado.getId() + " - " + consultado.getDescripcion()
                        + " | camino: " + caminoMinusculas);

        // 5 y 6. Tiempos fuera de rango.
        verificar("Alta de ruta A -> D con tiempo negativo (-1)",
                "rechazo: El tiempo debe ser un entero mayor o igual a 0",
                error(() -> redNueva().altaRuta("A", "D", -1, true)));
        verificar("Alta de ruta A -> D con tiempo 1.000.001",
                "rechazo: El tiempo no puede superar 1.000.000 minutos",
                error(() -> redNueva().altaRuta("A", "D", 1_000_001, true)));

        // 7. El tiempo máximo sí se acepta, y Dijkstra no lo elige porque hay caminos mejores.
        red = redNueva();
        red.altaRuta("A", "D", 1_000_000, true);
        verificar("Alta de ruta A -> D con el tiempo máximo (1.000.000) y camino A -> D",
                "aceptada | A -> C -> D, 12 min",
                "aceptada | " + camino(red, "A", "D"));

        // 8. Ruta de tiempo 0: Dijkstra admite pesos iguales a 0.
        red = redNueva();
        red.altaRuta("A", "F", 0, false);
        verificar("Ruta A -> F de un solo sentido con tiempo 0",
                "A -> F: A -> F, 0 min | F -> A: F -> D -> C -> A, 18 min",
                "A -> F: " + camino(red, "A", "F") + " | F -> A: " + camino(red, "F", "A"));

        // 9 a 11. Otras validaciones de rutas.
        verificar("Ruta de un centro a sí mismo (A -> A)",
                "rechazo: Una ruta no puede unir un centro consigo mismo",
                error(() -> redNueva().altaRuta("A", "A", 5, true)));
        verificar("Ruta duplicada (B -> A, que ya existe como A <-> B)",
                "rechazo: Ya existe una ruta entre B y A",
                error(() -> redNueva().altaRuta("B", "A", 3, true)));
        verificar("Ruta hacia un centro inexistente (A -> Z)",
                "rechazo: No existe un centro con id Z",
                error(() -> redNueva().altaRuta("A", "Z", 3, true)));

        // 12. Baja de un centro con rutas: se borran sus rutas y el camino se recalcula.
        red = redNueva();
        red.bajaCentro("C");
        verificar("Baja del centro C (tiene 4 rutas) y camino A -> E",
                "7 centros -> 6; 10 rutas -> 6 | A -> B -> D -> F -> G -> E, 33 min",
                "7 centros -> " + red.cantidadCentros() + "; 10 rutas -> " + red.cantidadRutas()
                        + " | " + camino(red, "A", "E"));

        // 13. Exploración en una red desconectada.
        red = redNueva();
        red.aislarCentro("G");
        ResultadoExploracion bfs = Exploracion.bfs(red, "A");
        verificar("BFS desde A con G aislado (red desconectada)",
                "visita A, B, C, D, E, F | no alcanzados: G",
                "visita " + ids(bfs.getOrdenDeVisita()) + " | no alcanzados: " + ids(bfs.getNoAlcanzados()));

        System.out.println();
        System.out.println(correctas + " de " + numero + " pruebas adicionales correctas.");
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static RedLogistica redNueva() {
        RedLogistica red = new RedLogistica();
        DatosIniciales.cargar(red);
        return red;
    }

    /** Muestra una prueba y la cuenta como correcta si lo obtenido coincide con lo esperado. */
    private void verificar(String nombre, String esperado, String obtenido) {
        numero++;
        boolean ok = esperado.equals(obtenido);
        if (ok) {
            correctas++;
        }
        System.out.println();
        System.out.println("[" + numero + "] " + nombre);
        System.out.println("    Esperado: " + esperado);
        System.out.println("    Obtenido: " + obtenido);
        System.out.println("    " + (ok ? "CORRECTA" : "INCORRECTA"));
    }

    /** Camino mínimo en texto: "A -> C -> D -> F, 18 min", o el motivo si no hay camino. */
    private static String camino(RedLogistica red, String origen, String destino) {
        ResultadoCamino r = Dijkstra.calcular(red, origen, destino);
        return switch (r.getEstado()) {
            case ENCONTRADO, MISMO_CENTRO -> ids(r.getCamino()).replace(", ", " -> ") + ", " + r.getCostoTotal() + " min";
            case SIN_CAMINO -> "sin recorrido";
            case ORIGEN_INEXISTENTE -> "origen inexistente";
            case DESTINO_INEXISTENTE -> "destino inexistente";
        };
    }

    private static String ids(Iterable<Centro> centros) {
        StringBuilder texto = new StringBuilder();
        for (Centro c : centros) {
            if (!texto.isEmpty()) {
                texto.append(", ");
            }
            texto.append(c.getId());
        }
        return texto.isEmpty() ? "ninguno" : texto.toString();
    }

    /** Ejecuta una operación que debería rechazarse y devuelve "rechazo: mensaje", o "aceptada". */
    private static String error(Runnable operacion) {
        try {
            operacion.run();
            return "aceptada";
        } catch (OperacionInvalidaException e) {
            return "rechazo: " + e.getMessage();
        }
    }

    private interface Alta {
        void aplicar(String id, String descripcion);
    }

    private static String error(Alta alta, String id, String descripcion) {
        return error(() -> alta.aplicar(id, descripcion));
    }
}
