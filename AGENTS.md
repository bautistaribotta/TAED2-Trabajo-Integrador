# AGENTS.md

Reglas para cualquier agente de IA (Claude, Copilot, Codex, etc.) que trabaje en este repositorio.

## Idioma

- **Siempre** responder en español.

## Commits y sus mensajes

- **Nunca** usar emojis.
- **Nunca** agregar el co-autor de Claude ni de ningún otro agente (`Co-Authored-By`, "Generated with...", etc.).
- **Siempre** redactar los mensajes en español.
- **Siempre** iniciar los mensajes con letra mayúscula.
- **Siempre** redactar los mensajes en primera persona del singular (ej: "Agrego", "Inicializo", "Corrijo"; nunca "Agrega", "Inicializa", "Corrige").

## Proyecto

- Trabajo Práctico Integrador de Taller de Algoritmos y Estructuras de Datos II (ver `README.md`).
- Lenguaje: Java, aplicación de consola. IDE de referencia: IntelliJ IDEA.
- Versión de Java: **JDK 25 (LTS)**. Antes de compilar o ejecutar, verificar la versión con `java -version`; si JDK 25 no está instalado, recomendar instalarlo (por ejemplo Eclipse Temurin 25 desde https://adoptium.net, con `winget install EclipseAdoptium.Temurin.25.JDK` o desde IntelliJ en *File → Project Structure → SDK → Download JDK*). No usar características de versiones posteriores a Java 25.
- Las estructuras centrales (árbol AVL, grafo, heap, cola, pila, árbol de Huffman, etc.) deben ser **implementadas por el equipo**; no usar bibliotecas que ya resuelvan su lógica (por ejemplo `java.util.PriorityQueue`, `TreeMap` o librerías de grafos).
- Mantener separadas las capas: estructuras de datos, lógica de negocio e interfaz de consola.
