package tpi.consola;

import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Lectura validada de datos desde el teclado, compartida por los menús de los dos casos.
 *
 * Envuelve un único Scanner sobre System.in. Cada método insiste hasta que el usuario
 * ingresa un valor con el formato correcto (por ejemplo, un número donde se pide un número),
 * así el resto del programa recibe siempre datos bien formados.
 *
 * Tiene que haber un solo objeto Entrada en todo el programa: si cada menú creara su
 * propio Scanner sobre System.in, uno podría quedarse con texto que ya leyó del teclado
 * y que en realidad le correspondía al otro. Por eso lo crea MenuPrincipal y se lo pasa
 * a cada menú.
 *
 * Solo controla el formato. Las reglas de negocio (id duplicado, tiempo negativo,
 * archivo inexistente, etc.) las valida la lógica de cada caso.
 */
public class Entrada {

    /**
     * Se lanza cuando ya no hay más entrada para leer, por ejemplo si se cierra la consola
     * o si la entrada viene de un archivo que se terminó. El menú la usa para salir en orden.
     */
    public static class EntradaTerminadaException extends RuntimeException {
        public EntradaTerminadaException() {
            super("No hay más datos de entrada");
        }
    }

    private final Scanner scanner;

    public Entrada() {
        this.scanner = new Scanner(System.in, codificacionDeLaConsola());
    }

    /**
     * Codificación con la que llegan los caracteres del teclado. Java la informa en la
     * propiedad stdin.encoding; si no está, se usa la del sistema. Sin esto, letras como
     * la ó de "Depósito" podrían leerse mal en la consola de Windows.
     */
    private static Charset codificacionDeLaConsola() {
        String nombre = System.getProperty("stdin.encoding");
        if (nombre != null && Charset.isSupported(nombre)) {
            return Charset.forName(nombre);
        }
        return Charset.defaultCharset();
    }

    /**
     * Muestra el mensaje y lee una línea, sin espacios en los extremos.
     *
     * @throws EntradaTerminadaException si no hay más líneas para leer
     */
    private String leerLinea(String mensaje) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) {
            throw new EntradaTerminadaException();
        }
        return scanner.nextLine().trim();
    }

    /** Lee un texto no vacío. Si el usuario solo presiona Enter, vuelve a pedirlo. */
    public String leerTexto(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("  El valor no puede estar vacío. Intente de nuevo.");
        }
    }

    /**
     * Lee cualquier número entero, incluso negativo. Se usa cuando quien decide si el
     * valor es válido es la lógica (por ejemplo, el tiempo de una ruta).
     */
    public int leerEntero(String mensaje) {
        return leerEntero(mensaje, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Lee un número entero entre min y max, ambos incluidos. Si se ingresan letras o un
     * número fuera de rango, informa el problema y vuelve a pedirlo.
     */
    public int leerEntero(String mensaje, int min, int max) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("  Debe ser un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                // parseInt falla con letras, con texto vacío y con números que no entran en un int.
                System.out.println("  Debe ingresar un número entero.");
            }
        }
    }

    /** Lee una respuesta de sí o no. Acepta s, si, sí, n y no, en mayúsculas o minúsculas. */
    public boolean leerSiNo(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje + " (s/n): ").toLowerCase();
            if (texto.equals("s") || texto.equals("si") || texto.equals("sí")) {
                return true;
            }
            if (texto.equals("n") || texto.equals("no")) {
                return false;
            }
            System.out.println("  Responda s o n.");
        }
    }

    /** Espera a que el usuario presione Enter, para que alcance a leer la salida. */
    public void pausa() {
        leerLinea("\nPresione Enter para continuar...");
    }
}
