package tpi.caso2.logica;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Lectura, escritura y comparación de archivos.
 *
 * Es la única clase del Caso 2 que toca el disco. Los archivos se leen y se escriben
 * enteros, de una sola vez, con java.nio.file.Files. Cualquier error de Java (IOException,
 * ruta inválida, etc.) se convierte en OperacionArchivoException con un mensaje que se
 * le puede mostrar al usuario.
 */
public final class GestorArchivos {

    private GestorArchivos() {
        // Clase de utilidad: no se crean objetos.
    }

    /**
     * Lee un archivo completo en memoria.
     *
     * @throws OperacionArchivoException si no existe, es una carpeta, no se puede leer o es
     *         más grande de lo que se puede manejar en memoria
     */
    public static byte[] leer(String ruta) {
        Path archivo = aRuta(ruta);
        if (Files.isDirectory(archivo)) {
            throw new OperacionArchivoException(ruta + " es una carpeta, no un archivo");
        }
        try {
            if (Files.size(archivo) > FormatoHuf.TAMANIO_MAXIMO) {
                throw new OperacionArchivoException("El archivo " + ruta + " es demasiado grande: el máximo es "
                        + FormatoHuf.TAMANIO_MAXIMO + " bytes");
            }
            return Files.readAllBytes(archivo);
        } catch (NoSuchFileException e) {
            throw new OperacionArchivoException("No existe el archivo " + ruta, e);
        } catch (AccessDeniedException e) {
            throw new OperacionArchivoException("No hay permiso para leer " + ruta, e);
        } catch (IOException e) {
            throw new OperacionArchivoException("No se pudo leer " + ruta + ": " + e.getMessage(), e);
        }
    }

    /**
     * Escribe el contenido en un archivo. Si ya existe, lo reemplaza (la consola pide
     * confirmación antes de llamar a este método).
     *
     * @throws OperacionArchivoException si no se pudo escribir
     */
    public static void escribir(String ruta, byte[] datos) {
        Path archivo = aRuta(ruta);
        if (Files.isDirectory(archivo)) {
            throw new OperacionArchivoException(ruta + " es una carpeta, no un archivo");
        }
        try {
            Files.write(archivo, datos);
        } catch (NoSuchFileException e) {
            throw new OperacionArchivoException("No existe la carpeta donde se quiere guardar " + ruta, e);
        } catch (AccessDeniedException e) {
            throw new OperacionArchivoException("No hay permiso para escribir " + ruta, e);
        } catch (IOException e) {
            throw new OperacionArchivoException("No se pudo escribir " + ruta + ": " + e.getMessage(), e);
        }
    }

    /** Crea una carpeta, y las carpetas que la contienen si faltan. No hace nada si ya existe. */
    public static void crearCarpeta(String ruta) {
        try {
            Files.createDirectories(aRuta(ruta));
        } catch (IOException e) {
            throw new OperacionArchivoException("No se pudo crear la carpeta " + ruta + ": " + e.getMessage(), e);
        }
    }

    /** true si existe un archivo (no una carpeta) en esa ruta. */
    public static boolean existe(String ruta) {
        Path archivo = aRuta(ruta);
        return Files.exists(archivo) && !Files.isDirectory(archivo);
    }

    /**
     * true si las dos rutas apuntan al mismo archivo, aunque estén escritas distinto
     * (por ejemplo "datos.txt" y "./datos.txt"). Sirve para no sobrescribir la entrada
     * con la salida.
     */
    public static boolean mismoArchivo(String rutaA, String rutaB) {
        Path a = aRuta(rutaA).toAbsolutePath().normalize();
        Path b = aRuta(rutaB).toAbsolutePath().normalize();
        try {
            if (Files.exists(a) && Files.exists(b)) {
                return Files.isSameFile(a, b);
            }
        } catch (IOException e) {
            // si no se puede determinar, se compara el texto de las rutas
        }
        return a.toString().equalsIgnoreCase(b.toString());
    }

    /** Ruta absoluta, para informarle al usuario dónde quedó un archivo. */
    public static String rutaAbsoluta(String ruta) {
        return aRuta(ruta).toAbsolutePath().normalize().toString();
    }

    /**
     * Compara dos contenidos byte a byte: primero los tamaños y después cada posición hasta
     * encontrar la primera diferencia. Complejidad O(N).
     */
    public static ResultadoComparacion comparar(byte[] a, byte[] b) {
        int menor = Math.min(a.length, b.length);
        for (int i = 0; i < menor; i++) {
            if (a[i] != b[i]) {
                return new ResultadoComparacion(a.length, b.length, i);
            }
        }
        long diferencia = a.length == b.length ? -1 : menor;
        return new ResultadoComparacion(a.length, b.length, diferencia);
    }

    /** Lee los dos archivos y los compara byte a byte. */
    public static ResultadoComparacion comparar(String rutaA, String rutaB) {
        return comparar(leer(rutaA), leer(rutaB));
    }

    private static Path aRuta(String ruta) {
        try {
            return Path.of(ruta);
        } catch (InvalidPathException e) {
            throw new OperacionArchivoException("La ruta " + ruta + " no es válida", e);
        }
    }
}
