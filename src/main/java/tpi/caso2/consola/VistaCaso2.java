package tpi.caso2.consola;

import tpi.caso2.logica.ResultadoComparacion;
import tpi.caso2.logica.ResultadoCompresion;
import tpi.caso2.logica.ResultadoDescompresion;
import tpi.caso2.modelo.TablaCodigos;
import tpi.caso2.modelo.TablaFrecuencias;
import tpi.estructuras.ArbolHuffman;

import java.util.Locale;

/**
 * Formateo de las salidas del Caso 2. Cada método arma un texto y lo devuelve; el menú se
 * encarga de imprimirlo. No calcula nada: solo muestra lo que ya trae cada resultado.
 *
 * Los números se muestran con el formato argentino: punto para los miles y coma para los
 * decimales (10.000 bytes, 87,29 %).
 */
public final class VistaCaso2 {

    private static final Locale ARGENTINA = Locale.of("es", "AR");

    /** El árbol solo se dibuja si tiene hasta esta cantidad de hojas; si no, ocupa demasiado. */
    private static final int HOJAS_MAXIMAS_PARA_DIBUJAR = 32;

    private VistaCaso2() {
        // Clase de utilidad: no se crean objetos.
    }

    /** Resultado de comprimir un archivo. */
    public static String compresion(String rutaEntrada, String rutaSalida, ResultadoCompresion r) {
        StringBuilder texto = new StringBuilder();
        texto.append("Compresión de ").append(rutaEntrada).append('\n');
        texto.append("  Tamaño original:     ").append(bytes(r.getTamanioOriginal())).append('\n');
        texto.append("  Tamaño comprimido:   ").append(bytes(r.getTamanioComprimido()))
                .append("  (cabecera ").append(r.getBytesCabecera())
                .append(" B + árbol ").append(numero(r.getBitsArbol()))
                .append(" bits + datos ").append(numero(r.getBitsDatos()))
                .append(" bits + relleno ").append(r.getBitsRelleno()).append(" bits)\n");
        texto.append("  Resultado:           ").append(variacion(r.getPorcentaje())).append('\n');
        texto.append("  Símbolos distintos:  ").append(r.getSimbolosDistintos()).append('\n');
        if (r.getTamanioOriginal() > 0) {
            texto.append("  Bits por símbolo:    ").append(decimal(r.getBitsPorSimbolo()))
                    .append(" en promedio (el original usa 8)\n");
        }
        texto.append("  Tiempo de proceso:   ").append(milisegundos(r.getNanosegundos()))
                .append(" (sin contar lectura y escritura en disco)\n");
        if (rutaSalida != null) {
            texto.append("  Archivo generado:    ").append(rutaSalida).append('\n');
        }
        return texto.toString();
    }

    /** Resultado de descomprimir un archivo. */
    public static String descompresion(String rutaEntrada, String rutaSalida, ResultadoDescompresion r) {
        return "Descompresión de " + rutaEntrada + '\n'
                + "  Tamaño comprimido:   " + bytes(r.getTamanioComprimido()) + '\n'
                + "  Tamaño restaurado:   " + bytes(r.getTamanioOriginal()) + '\n'
                + "  Símbolos distintos:  " + r.getSimbolosDistintos() + '\n'
                + "  Validaciones:        cabecera, árbol, longitud, relleno y suma Adler-32 correctos\n"
                + "  Tiempo de proceso:   " + milisegundos(r.getNanosegundos())
                + " (sin contar lectura y escritura en disco)\n"
                + (rutaSalida != null ? "  Archivo generado:    " + rutaSalida + '\n' : "");
    }

    /** Mensaje de un archivo comprimido rechazado. */
    public static String rechazo(String motivo) {
        return "No se pudo descomprimir: " + motivo + ".\nNo se generó ningún archivo.";
    }

    /** Resultado de comparar dos archivos byte a byte. */
    public static String comparacion(String rutaA, String rutaB, ResultadoComparacion c) {
        StringBuilder texto = new StringBuilder();
        texto.append("Comparación byte a byte\n");
        texto.append("  A: ").append(rutaA).append(" (").append(bytes(c.getTamanioA())).append(")\n");
        texto.append("  B: ").append(rutaB).append(" (").append(bytes(c.getTamanioB())).append(")\n");
        if (c.sonIguales()) {
            texto.append("  Verificación byte a byte: IGUALES\n");
        } else {
            texto.append("  Verificación byte a byte: DISTINTOS. Primer byte distinto en la posición ")
                    .append(numero(c.getPrimeraDiferencia())).append(" (contando desde 0)");
            if (c.getTamanioA() != c.getTamanioB()) {
                texto.append("; además tienen distinto tamaño");
            }
            texto.append('\n');
        }
        return texto.toString();
    }

    /**
     * Análisis de un archivo: tabla de frecuencias y códigos ordenada por símbolo, longitud
     * promedio y, si el árbol es chico, su dibujo.
     */
    public static String analisis(String ruta, ResultadoCompresion r) {
        StringBuilder texto = new StringBuilder();
        texto.append("Análisis de ").append(ruta).append(" (").append(bytes(r.getTamanioOriginal())).append(")\n");
        if (r.getTamanioOriginal() == 0) {
            texto.append("  El archivo está vacío: no hay símbolos ni árbol.\n");
            return texto.toString();
        }
        TablaFrecuencias f = r.getFrecuencias();
        TablaCodigos c = r.getCodigos();
        texto.append(String.format("  %-9s %12s %9s  %-8s %s%n", "Símbolo", "Frecuencia", "%", "Longitud", "Código"));
        for (int s = 0; s < ArbolHuffman.SIMBOLOS; s++) {
            if (f.frecuencia(s) > 0) {
                double porcentaje = f.frecuencia(s) * 100.0 / f.total();
                texto.append(String.format(ARGENTINA, "  %-9s %,12d %8.2f%%  %-8d %s%n",
                        ArbolHuffman.nombreSimbolo(s), f.frecuencia(s), porcentaje, c.longitud(s), c.comoTexto(s)));
            }
        }
        texto.append("  Símbolos distintos: ").append(f.distintos()).append('\n');
        texto.append("  Longitud promedio:  ").append(decimal(r.getBitsPorSimbolo()))
                .append(" bits por símbolo (el original usa 8)\n");
        texto.append("  Datos codificados:  ").append(numero(r.getBitsDatos())).append(" bits, contra ")
                .append(numero(r.getTamanioOriginal() * 8)).append(" del original\n");
        if (r.getArbol().cantidadHojas() <= HOJAS_MAXIMAS_PARA_DIBUJAR) {
            texto.append("\nÁrbol de Huffman (0 = izquierda, 1 = derecha; entre paréntesis, la suma de frecuencias):\n");
            texto.append(r.getArbol().dibujar());
        } else {
            texto.append("\n(El árbol tiene ").append(r.getArbol().cantidadHojas())
                    .append(" hojas: no se dibuja porque ocuparía demasiado.)\n");
        }
        return texto.toString();
    }

    // ------------------------------------------------------------------
    // Formatos de números
    // ------------------------------------------------------------------

    /** Cantidad de bytes con separador de miles: 10.000 B. */
    public static String bytes(long cantidad) {
        return numero(cantidad) + " B";
    }

    public static String numero(long valor) {
        return String.format(ARGENTINA, "%,d", valor);
    }

    public static String decimal(double valor) {
        return String.format(ARGENTINA, "%.2f", valor);
    }

    public static String milisegundos(long nanosegundos) {
        return String.format(ARGENTINA, "%.3f ms", nanosegundos / 1_000_000.0);
    }

    /** "reducción del 87,29 %", "aumento del 7,69 %" o "no aplica" si el original está vacío. */
    public static String variacion(double porcentaje) {
        if (Double.isNaN(porcentaje)) {
            return "no aplica (el original está vacío)";
        }
        if (porcentaje >= 0) {
            return "reducción del " + decimal(porcentaje) + " %";
        }
        return "aumento del " + decimal(-porcentaje) + " %";
    }
}
