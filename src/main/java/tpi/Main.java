package tpi;

import tpi.caso1.consola.MenuCaso1;

/**
 * Punto de entrada del Trabajo Práctico Integrador.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("TPI - Taller de Algoritmos y Estructuras de Datos II");
        System.out.println("Caso 1: Distribución de insumos entre centros logísticos");
        new MenuCaso1().iniciar();
    }
}
