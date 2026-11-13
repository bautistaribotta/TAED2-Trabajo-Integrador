package tpi.caso2.logica;

/**
 * Resultado de una descompresión exitosa: el contenido original reconstruido y las medidas.
 *
 * Solo existe si pasaron todas las validaciones; si el archivo estaba dañado se lanza
 * ArchivoDanadoException y no se crea ningún resultado.
 */
public class ResultadoDescompresion {

    private final byte[] datos;
    private final long tamanioComprimido;
    private final int simbolosDistintos;
    private final long nanosegundos;

    ResultadoDescompresion(byte[] datos, long tamanioComprimido, int simbolosDistintos, long nanosegundos) {
        this.datos = datos;
        this.tamanioComprimido = tamanioComprimido;
        this.simbolosDistintos = simbolosDistintos;
        this.nanosegundos = nanosegundos;
    }

    /** El contenido original, byte por byte. */
    public byte[] getDatos() {
        return datos;
    }

    public long getTamanioOriginal() {
        return datos.length;
    }

    public long getTamanioComprimido() {
        return tamanioComprimido;
    }

    public int getSimbolosDistintos() {
        return simbolosDistintos;
    }

    /** Tiempo del proceso en memoria, sin contar la lectura ni la escritura en disco. */
    public long getNanosegundos() {
        return nanosegundos;
    }
}
