# TPI — Taller de Algoritmos y Estructuras de Datos II

| Integrantes | Profesor | Estado |
|---|---|---|
| Ramiro Contreras, Juan Bautista Ribotta | Pedro Requena | Caso 1 implementado; Caso 2 pendiente |

Este README tiene dos partes: primero la documentación del proyecto (cómo compilar, ejecutar y usar el programa) y después el [enunciado original](#trabajo-práctico-integrador--algoritmos-y-estructuras-de-datos-ii) del trabajo.

El plan de implementación del Caso 1, con las decisiones de diseño, la API de cada clase y el pseudocódigo, está en [`docs/plan-caso-1.md`](docs/plan-caso-1.md).

## Requisitos

- **JDK 25 (LTS)**. Por ejemplo, Eclipse Temurin 25 (`winget install EclipseAdoptium.Temurin.25.JDK` o desde https://adoptium.net). Verificar con `java -version`.
- **Maven 3.9 o posterior**, o el Maven que trae IntelliJ IDEA. Verificar con `mvn -version`.
- No hay dependencias externas: el `pom.xml` solo usa los plugins estándar de compilación y empaquetado.

## Cómo compilar y ejecutar

Desde la carpeta del proyecto:

```bash
mvn package
java -jar target/tpi-taed2-1.0-SNAPSHOT.jar
```

Desde IntelliJ IDEA: *File → Open* y elegir la carpeta del proyecto (lo reconoce por el `pom.xml`). En *File → Project Structure → SDK* elegir JDK 25 y ejecutar la clase `tpi.Main`.

## Caso 1: distribución de insumos entre centros logísticos

### Estructura del código

El código está separado en capas. Cada capa solo usa a la que está debajo:

```
consola  ──usa──▶  logica  ──usa──▶  modelo  ──usa──▶  estructuras
```

```
src/main/java/tpi/
├── Main.java                         punto de entrada: abre el menú del Caso 1
├── estructuras/                      implementadas por el equipo, sin colecciones de Java
│   ├── ListaEnlazada.java            listas de adyacencia y resultados
│   ├── Cola.java                     BFS
│   ├── Pila.java                     reconstrucción del camino de Dijkstra
│   ├── MonticuloMinimo.java          Dijkstra (elegir el centro más cercano)
│   └── ArbolAVL.java                 índice de centros por id
└── caso1/
    ├── modelo/
    │   ├── Centro.java               vértice del grafo, con su lista de adyacencia
    │   └── Ruta.java                 arista ponderada (minutos), bidireccional o de un sentido
    ├── logica/                       nunca imprime: informa con excepciones u objetos Resultado
    │   ├── RedLogistica.java         grafo + validaciones (altas, bajas, modificaciones)
    │   ├── Dijkstra.java             camino de menor tiempo total
    │   ├── Exploracion.java          recorridos BFS y DFS
    │   ├── ResultadoCamino.java
    │   ├── ResultadoExploracion.java
    │   ├── OperacionInvalidaException.java
    │   └── DatosIniciales.java       7 centros y 10 rutas del enunciado
    └── consola/
        ├── MenuCaso1.java            menú principal y submenús
        ├── Entrada.java              lectura validada desde el teclado
        └── Vista.java                formato de las salidas y dibujo de la red
```

### Uso del programa

Al iniciar, la red se carga automáticamente con los datos del enunciado. El menú principal ofrece:

| Opción | Qué hace |
|---|---|
| 1. Gestión de centros | Alta, modificar descripción, consultar por id, listar ordenados, baja y ver el árbol AVL del índice |
| 2. Gestión de rutas | Alta (bidireccional o de un sentido), modificar tiempo, deshabilitar (baja temporal), habilitar, eliminar, aislar un centro y listar |
| 3. Camino mínimo (Dijkstra) | Recorrido de menor tiempo total entre dos centros, con el orden de visita y el costo acumulado |
| 4. Explorar la red (BFS / DFS) | Recorrido de exploración desde un centro, indicando el criterio de visita |
| 5. Mostrar la red | Lista de adyacencia, matriz de tiempos y resumen |
| 6. Restaurar datos iniciales | Descarta los cambios y vuelve a cargar los datos del enunciado |
| 7. Ejecutar pruebas obligatorias | Corre las pruebas del enunciado sobre una red nueva, sin tocar la red en uso |

Los identificadores se normalizan: se les quitan los espacios de los extremos y se pasan a mayúsculas, así que `a` y `A` son el mismo centro. Si se ingresa un dato inválido (letras donde va un número, un id duplicado, un tiempo negativo), el programa informa el problema y sigue funcionando.

### Datos de prueba

Son los del enunciado: 7 centros (A a G) y 10 rutas bidireccionales, con tiempos en minutos. Los carga `DatosIniciales` usando las mismas operaciones de alta que el menú. No se carga ningún resultado: todos se calculan en el momento.

La opción 7 ejecuta, en este orden: A → F, A → G, B → E, A → A, A → Z (destino inexistente), Z → A (origen inexistente), aislar G, A → G y A → F. Estos son los resultados que muestra al ejecutarla:

| Consulta | Resultado | Camino | Tiempo |
|---|---|---|---|
| A → F | Encontrado | A → C → D → F | 18 min |
| A → G | Encontrado | A → C → E → G | 17 min |
| B → E | Encontrado | B → C → E | 7 min |
| A → A | Mismo centro | A | 0 min |
| A → Z | Destino no registrado | — | — |
| Z → A | Origen no registrado | — | — |
| Aislar G | Deshabilita 2 rutas (E–G y F–G) | — | — |
| A → G con G aislado | Sin recorrido | — | — |
| A → F con G aislado | Encontrado (no cambia) | A → C → D → F | 18 min |

Recorridos de exploración desde A (opción 4): BFS visita A, B, C, D, E, F, G y DFS visita A, B, C, D, F, E, G.

### Formato de los archivos

El Caso 1 no lee ni escribe archivos: los datos se guardan solo en memoria y se cargan al iniciar el programa.

### Limitaciones conocidas

- **Solo en memoria:** los cambios hechos desde el menú se pierden al cerrar el programa.
- **DFS recursivo:** con miles de centros conectados en línea, la recursión podría desbordar la pila de llamadas de Java. La alternativa sería una versión iterativa con la `Pila` propia.
- **Baja y aislamiento de un centro en O(V + E):** hay que recorrer todos los centros, porque una ruta de un solo sentido que llega a un centro está guardada solo en la lista de su origen.
- **El id de un centro no se modifica:** es la clave del árbol AVL. Para cambiarlo hay que dar de baja el centro y volver a darlo de alta.
- **Contar las rutas cuesta O(V + E):** se cuentan recorriendo la red en lugar de llevar un contador aparte.
- **La matriz de tiempos se muestra hasta 20 centros:** con más no entra en pantalla, y se muestran solo la lista de adyacencia y el resumen.
- **Tiempos enteros en minutos y no negativos:** es la condición que necesita Dijkstra.
- **Caracteres de la consola:** las flechas y líneas se dibujan con ASCII (`->`, `+-`) para que se vean bien en cualquier consola de Windows.

---

# Trabajo Práctico Integrador — Algoritmos y Estructuras de Datos II

**De un problema real a una solución algorítmica**

| Asignatura | Modalidad | Implementación sugerida |
|---|---|---|
| Algoritmos y Estructuras de Datos II | Individual o equipos de hasta 3 integrantes | Java, aplicación de consola y archivos locales |

## Propósito

En el desarrollo de software, el problema no llega acompañado por el nombre del algoritmo que debe utilizarse. El objetivo de este trabajo es interpretar requerimientos reales, identificar las operaciones necesarias, elegir estructuras de datos apropiadas y justificar las decisiones antes de programar.

Se presentan dos situaciones independientes. No se exige incorporar todas las técnicas estudiadas en cada caso. La elección deberá surgir del análisis, aunque se espera que el conjunto de las soluciones permita discutir árboles balanceados, grafos y compresión de datos.

## Objetivos de aprendizaje

- Modelar problemas mediante entidades, relaciones, entradas, salidas y restricciones.
- Comparar estructuras de datos considerando complejidad temporal, memoria y facilidad de mantenimiento.
- Implementar y probar soluciones correctas, incluidos casos límite y errores de entrada.
- Justificar el uso —o el descarte— de algoritmos vistos en clase.

## Reglas generales

Este trabajo práctico debe resolverse utilizando los contenidos estudiados en la asignatura Taller de Algoritmos y Estructuras de Datos II: **árboles binarios, árboles AVL, algoritmo de Huffman, grafos, recorridos de grafos y algoritmo de Dijkstra** para el camino mínimo. Cada equipo deberá analizar los casos y seleccionar y justificar las estructuras y algoritmos pertinentes; no es obligatorio aplicar todos en cada problema.

- El informe comenzará con una **propuesta sin código**.
- Las estructuras centrales deberán ser **implementadas por el equipo**, sin utilizar bibliotecas que ya resuelvan su lógica.

---

## CASO 1 · Distribución de insumos entre centros logísticos

### Situación real

Una empresa regional abastece hospitales y centros de salud. Dispone de un depósito central y varios puntos de entrega conectados por rutas terrestres. Cada trayecto posee un tiempo estimado de circulación, que puede variar entre rutas. El coordinador necesita decidir cómo llegar desde un origen a un destino con el menor tiempo total posible, y consultar rápidamente información de centros registrados.

En algunos sectores hay rutas de un solo sentido. Los tiempos son siempre no negativos. Una ruta puede estar cortada, y no necesariamente todos los centros están conectados. Se busca una solución que funcione aunque el número de centros crezca.

### Datos iniciales para el prototipo

Centros:

| Id | Descripción |
|---|---|
| A | Depósito central |
| B | Hospital Norte |
| C | Hospital Sur |
| D | Base Este |
| E | Centro Oeste |
| F | Centro Rural |
| G | Centro de apoyo |

Para la primera versión, las conexiones son **bidireccionales**; el tiempo está expresado en **minutos**.

| Ruta | Tiempo |
|---|---|
| A–B | 8 |
| A–C | 5 |
| B–C | 3 |
| B–D | 9 |
| C–D | 7 |
| C–E | 4 |
| D–F | 6 |
| E–F | 11 |
| E–G | 8 |
| F–G | 2 |

### Requerimientos funcionales

- Registrar, modificar y consultar centros a partir de un identificador único; rechazar identificadores duplicados.
- Registrar rutas con sus tiempos; permitir dar de baja temporalmente una conexión.
- Consultar el recorrido con menor tiempo total entre dos centros, mostrando el orden de visita y el costo acumulado.
- Informar cuando no existe un recorrido, cuando el origen o destino no está registrado, o cuando ambos coinciden.
- Mostrar un recorrido de exploración de la red desde un centro elegido, especificando el criterio de visita y diferenciándolo de la búsqueda del camino mínimo.

### Preguntas de diseño (antes de programar)

- ¿Cómo representarían los centros y las conexiones? Dibujen el modelo elegido y expliquen qué representa cada elemento.
- ¿Qué estructura usarían para mantener un índice de centros si son frecuentes las altas, bajas y búsquedas por identificador? Comparen al menos dos alternativas.
- ¿Un recorrido por anchura o profundidad garantiza el menor tiempo cuando las rutas tienen diferentes costos? Justifiquen con un contraejemplo.
- ¿Qué algoritmo elegirían para minimizar el tiempo y qué condiciones deben cumplirse sobre los pesos?
- ¿Cómo cambia su diseño si las rutas son dirigidas o si el sistema contiene miles de centros?

### Pruebas mínimas obligatorias

Ejecutar al menos:

- A→F
- A→G
- B→E
- A→A
- Una consulta con centro inexistente

Luego **aislar G** quitando sus rutas y repetir una búsqueda hacia G. Documentar costo, secuencia obtenida y decisiones tomadas. Las respuestas deben surgir de la ejecución, no de una tabla precargada.

---

## CASO 2 · Archivo histórico de mensajes de sensores

### Situación real

Una empresa de monitoreo conserva miles de archivos con mensajes de equipos instalados en campo. Los archivos contienen reportes de estado repetitivos y se almacenan durante años. El volumen total aumenta cada mes y el responsable técnico solicita un programa que permita reducir el espacio ocupado sin perder un solo byte de información, y que pueda recuperar posteriormente el contenido original.

El programa deberá trabajar sobre **archivos binarios arbitrarios**, no solo sobre textos legibles. Cada archivo comprimido debe ser **autónomo**: otra ejecución del programa debe poder reconstruir el original usando únicamente el archivo generado.

### Muestra de mensajes para analizar

Archivo de ejemplo (texto UTF-8, sin salto de línea final):

```
OK|OK|OK|ERROR|OK|OK|ALERTA|OK|OK|ERROR
```

Esta muestra sirve para examinar frecuencias; la implementación final debe admitir cualquier byte entre 0 y 255.

### Requerimientos funcionales

- Leer un archivo de entrada y contar las apariciones de cada valor de byte.
- Producir un archivo comprimido con un formato documentado que permita su posterior interpretación.
- Restaurar el archivo original de forma exacta y verificar igualdad byte a byte.
- Informar tamaño original, tamaño comprimido, porcentaje de reducción o aumento, y tiempo de proceso.
- Resolver correctamente archivos vacíos, archivos con un único símbolo distinto, archivos sin repeticiones y archivos binarios.
- Rechazar archivos comprimidos dañados o con formato no reconocido sin informar falsamente una descompresión exitosa.

### Preguntas de diseño (antes de programar)

- ¿Qué propiedad de los mensajes podría aprovecharse para reducir su tamaño sin perder información?
- ¿Qué información mínima debe conservarse para reconstruir el archivo en una ejecución posterior?
- ¿Cómo representarían los símbolos y sus frecuencias? ¿Qué estructura facilitaría seleccionar repetidamente elementos de menor frecuencia?
- ¿Cómo transformarían códigos de longitud variable en una secuencia real de bits, evitando guardar los caracteres «0» y «1» como texto?
- ¿El tamaño comprimido siempre será menor? ¿Cómo influye la cabecera en archivos pequeños?
- ¿Cómo garantizarían resultados reproducibles cuando dos símbolos tienen igual frecuencia?

### Conjunto mínimo de archivos de prueba

| Archivo | Contenido / característica | Qué observar |
|---|---|---|
| T1 | 0 bytes | Formato y reconstrucción vacía |
| T2 | 10.000 repeticiones del byte 65 | Caso de símbolo único |
| T3 | Los valores 0 a 255, una vez cada uno | Distribución uniforme |
| T4 | Muestra de mensajes del caso | Costo de cabecera y frecuencias |
| T5 | Archivo binario de al menos 100 KB | Integridad y medición de tamaño |
| T6 | Archivo comprimido truncado | Detección de error |

---

## Proceso de trabajo requerido para ambos casos

1. **Comprensión:** identificar actores, necesidad, entradas, salidas, supuestos, restricciones y casos límite.
2. **Selección:** proponer y comparar al menos dos alternativas de representación o estructura; elegir una con fundamento.
3. **Diseño:** entregar diagrama de datos y pseudocódigo de las operaciones centrales; anticipar su complejidad.
4. **Desarrollo:** implementar la solución en Java separando estructuras, lógica de negocio e interfaz de consola.
5. **Validación:** ejecutar pruebas reproducibles, registrar resultados y analizar errores y desempeño.

## Entregables

- Informe PDF con análisis, comparativa, decisiones justificadas, diagramas, pseudocódigo, complejidad y resultados de pruebas.
- Proyecto Java completo, compilable y ejecutable, con código fuente organizado.
- README con instrucciones de ejecución, formato de los archivos, datos de prueba y limitaciones conocidas.
- Demostración de ambos casos, incluyendo al menos un caso límite por sistema.

## Evaluación (100 puntos)

| Criterio | Puntaje |
|---|---|
| Análisis del problema y modelado | 20 |
| Selección y justificación de estructuras y algoritmos | 25 |
| Correctitud de la implementación | 25 |
| Calidad y cobertura de pruebas | 20 |
| Documentación, claridad y presentación | 10 |

## Extensiones opcionales (no obligatorias)

- **Logística:** comparar tiempos entre distintos algoritmos de consulta; añadir vías de un solo sentido; actualizar tiempos; estudiar un índice autobalanceado de centros.
- **Archivo:** procesamiento por bloques, comparación con el archivo original en distintos tipos de datos, o estudio del efecto de la distribución de frecuencias.

## Consigna de cierre

**Defensa técnica:** ¿qué estructura o algoritmo elegirían si el escenario cambiara de escala o de requisitos? Expliquen qué problema resuelve cada técnica y qué limitación impide utilizarla indiscriminadamente. Se evaluará especialmente el razonamiento que lleva a la elección, no solo la ejecución de un programa.
