package tpi.caso2.logica;

/**
 * Resultado de comparar dos contenidos byte a byte.
 */
public class ResultadoComparacion {

    private final long tamanioA;
    private final long tamanioB;
    private final long primeraDiferencia;

    ResultadoComparacion(long tamanioA, long tamanioB, long primeraDiferencia) {
        this.tamanioA = tamanioA;
        this.tamanioB = tamanioB;
        this.primeraDiferencia = primeraDiferencia;
    }

    /** true si tienen el mismo tamaño y todos sus bytes coinciden. */
    public boolean sonIguales() {
        return primeraDiferencia == -1;
    }

    public long getTamanioA() {
        return tamanioA;
    }

    public long getTamanioB() {
        return tamanioB;
    }

    /**
     * Posición (contando desde 0) del primer byte distinto, o -1 si son iguales. Si uno es
     * el principio del otro, la primera diferencia es donde termina el más corto.
     */
    public long getPrimeraDiferencia() {
        return primeraDiferencia;
    }
}
