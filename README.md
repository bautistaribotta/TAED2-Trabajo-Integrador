# TPI — Taller de Algoritmos y Estructuras de Datos II

| Integrantes | Profesor | Estado |
|---|---|---|
| Ramiro Contreras, Juan Bautista Ribotta | Pedro Requena | Casos 1 y 2 implementados |

Este README tiene dos partes: primero la documentación del proyecto (cómo compilar, ejecutar y usar el programa) y después el [enunciado original](#trabajo-práctico-integrador--algoritmos-y-estructuras-de-datos-ii) del trabajo.

Los planes de implementación, con las decisiones de diseño, la API de cada clase y el pseudocódigo, están en [`docs/plan-caso-1.md`](docs/plan-caso-1.md) y [`docs/plan-caso-2.md`](docs/plan-caso-2.md).

## Instalación y ejecución

El programa es una aplicación de consola en Java, sin dependencias externas. Lo único que hace falta es el **JDK 25** y, para la opción A, **Maven**. Hay tres formas de compilarlo y ejecutarlo; alcanza con una.

### 1. Instalar el JDK 25

Si `java -version` ya muestra la versión 25, este paso no hace falta.

- **Windows:** `winget install EclipseAdoptium.Temurin.25.JDK`, o descargar el instalador `.msi` de https://adoptium.net (Temurin 25). Al instalarlo, dejar marcadas las opciones *Add to PATH* y *Set JAVA_HOME*.
- **macOS:** `brew install --cask temurin@25`, o el instalador `.pkg` de https://adoptium.net.
- **Linux:** el paquete de Temurin 25 de https://adoptium.net (tiene repositorios para apt y dnf), o el JDK 25 de la distribución si ya lo ofrece.

Después de instalarlo, **abrir una consola nueva** y verificar:

```bash
java -version
javac -version
```

Las dos tienen que mostrar la versión 25.

### 2. Compilar y ejecutar

Todos los comandos se ejecutan desde la carpeta del proyecto, la que tiene el `pom.xml`.

**Opción A: con Maven.** Requiere Maven 3.9 o posterior (verificar con `mvn -version`). Si no está instalado: en Windows, descargar el `.zip` *Binary* de https://maven.apache.org/download.cgi, descomprimirlo y agregar su carpeta `bin` al `PATH`; en macOS, `brew install maven`; en Linux, `sudo apt install maven` o el equivalente.

```bash
mvn package
java -jar target/tpi-taed2-1.0-SNAPSHOT.jar
```

La primera vez, Maven necesita conexión a internet para descargar sus plugins de compilación; después funciona sin conexión.

**Opción B: solo con el JDK, sin Maven.** Compila las clases en la carpeta `out` y ejecuta el programa.

En Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out tpi.Main
```

En Windows (cmd):

```bat
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
java -cp out tpi.Main
```

En macOS o Linux:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out tpi.Main
```

**Opción C: con IntelliJ IDEA.** No hace falta instalar Maven, porque IntelliJ trae el suyo, y el JDK se puede descargar desde el mismo programa.

1. *File → Open* y elegir la carpeta del proyecto. IntelliJ lo reconoce como proyecto Maven por el `pom.xml`.
2. En *File → Project Structure → SDK*, elegir JDK 25. Si no está instalado, usar *Download JDK* y elegir Eclipse Temurin 25.
3. Abrir `src/main/java/tpi/Main.java` y ejecutarla con el botón verde junto a `main`.

### Si algo falla

- **`java` o `mvn` no se reconoce como comando:** la consola se abrió antes de instalarlo, o la carpeta `bin` no está en el `PATH`. Abrir una consola nueva.
- **`invalid target release: 25` o `release version 25 not supported`:** se está usando un JDK más viejo. Revisar `java -version` y, con Maven, la línea *Java version* de `mvn -version`, que tiene que decir 25. Si no, apuntar la variable `JAVA_HOME` a la carpeta del JDK 25.
- **Los acentos se ven mal en la consola de Windows:** ejecutar `chcp 65001` antes de iniciar el programa, o usar Windows Terminal.

### Archivos que genera el programa

El Caso 2 crea la carpeta `pruebas-caso2/` dentro de la carpeta desde la que se ejecuta el programa, con los archivos de prueba y sus versiones comprimidas. Se puede borrar sin problemas: la opción 6 la vuelve a generar.

Al iniciar se muestra un menú principal para elegir el caso:

```
===== TPI - TALLER DE ALGORITMOS Y ESTRUCTURAS DE DATOS II =====
 1. Caso 1: Distribución de insumos entre centros logísticos
 2. Caso 2: Archivo histórico de mensajes de sensores
 0. Salir
```

Desde cada caso, la opción 0 vuelve a este menú. Los cambios hechos en la red del Caso 1 se conservan mientras el programa esté abierto.

## Estructura del código

El código está separado en capas. Cada capa solo usa a la que está debajo:

```
consola  ──usa──▶  logica  ──usa──▶  modelo  ──usa──▶  estructuras
```

```
src/main/java/tpi/
├── Main.java                         punto de entrada: abre el menú principal
├── consola/                          compartido por los dos casos
│   ├── MenuPrincipal.java            elegir Caso 1, Caso 2 o salir
│   └── Entrada.java                  lectura validada desde el teclado (un solo Scanner)
├── estructuras/                      implementadas por el equipo, sin colecciones de Java
│   ├── ListaEnlazada.java            listas de adyacencia y resultados
│   ├── Cola.java                     BFS
│   ├── Pila.java                     reconstrucción del camino de Dijkstra
│   ├── MonticuloMinimo.java          Dijkstra y construcción del árbol de Huffman
│   ├── ArbolAVL.java                 índice de centros por id
│   ├── ArbolHuffman.java             códigos, serialización en preorden y decodificación
│   ├── EscritorBits.java             empaqueta bits sueltos en bytes
│   └── LectorBits.java               lee bits sueltos de un arreglo de bytes
├── caso1/
│   ├── modelo/
│   │   ├── Centro.java               vértice del grafo, con su lista de adyacencia
│   │   └── Ruta.java                 arista ponderada (minutos), bidireccional o de un sentido
│   ├── logica/                       nunca imprime: informa con excepciones u objetos Resultado
│   │   ├── RedLogistica.java         grafo + validaciones (altas, bajas, modificaciones)
│   │   ├── Dijkstra.java             camino de menor tiempo total
│   │   ├── Exploracion.java          recorridos BFS y DFS
│   │   ├── ResultadoCamino.java
│   │   ├── ResultadoExploracion.java
│   │   ├── OperacionInvalidaException.java
│   │   └── DatosIniciales.java       7 centros y 10 rutas del enunciado
│   └── consola/
│       ├── MenuCaso1.java            menú del caso y submenús
│       └── Vista.java                formato de las salidas y dibujo de la red
└── caso2/
    ├── modelo/
    │   ├── TablaFrecuencias.java     apariciones de cada byte (arreglo de 256)
    │   └── TablaCodigos.java         código de Huffman de cada byte
    ├── logica/                       trabaja en memoria; solo GestorArchivos toca el disco
    │   ├── CompresorHuffman.java     comprimir y descomprimir (byte[] → byte[])
    │   ├── FormatoHuf.java           formato del archivo comprimido y su cabecera
    │   ├── SumaAdler32.java          suma de verificación
    │   ├── GestorArchivos.java       leer, escribir y comparar archivos
    │   ├── ArchivosDePrueba.java     contenido de T1 a T6
    │   ├── ResultadoCompresion.java
    │   ├── ResultadoDescompresion.java
    │   ├── ResultadoComparacion.java
    │   ├── ArchivoDanadoException.java
    │   └── OperacionArchivoException.java
    └── consola/
        ├── MenuCaso2.java            menú del caso
        └── VistaCaso2.java           formato de las salidas
```

## Caso 1: distribución de insumos entre centros logísticos

### Uso del programa

Al iniciar, la red se carga automáticamente con los datos del enunciado. El menú del Caso 1 ofrece:

| Opción | Qué hace |
|---|---|
| 1. Gestión de centros | Alta, modificar descripción, consultar por id, listar ordenados, baja y ver el árbol AVL del índice |
| 2. Gestión de rutas | Alta (bidireccional o de un sentido), modificar tiempo, deshabilitar (baja temporal), habilitar, eliminar, aislar un centro y listar |
| 3. Camino mínimo (Dijkstra) | Recorrido de menor tiempo total entre dos centros, con el orden de visita y el costo acumulado |
| 4. Explorar la red (BFS / DFS) | Recorrido de exploración desde un centro, indicando el criterio de visita |
| 5. Mostrar la red | Lista de adyacencia, matriz de tiempos y resumen |
| 6. Restaurar datos iniciales | Descarta los cambios y vuelve a cargar los datos del enunciado |
| 7. Ejecutar pruebas obligatorias | Corre las pruebas del enunciado sobre una red nueva, sin tocar la red en uso |
| 8. Ejecutar pruebas adicionales | Corre 13 pruebas de casos límite y validaciones, y compara cada resultado con el esperado |

Los identificadores se normalizan: se les quitan los espacios de los extremos y se pasan a mayúsculas, así que `a` y `A` son el mismo centro. Si se ingresa un dato inválido (letras donde va un número, un id duplicado, un tiempo negativo o mayor a 1.000.000 minutos), el programa informa el problema y sigue funcionando.

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

La opción 8 agrega 13 pruebas de casos límite, todas correctas:
- una ruta de un solo sentido, que se puede usar para ir pero no para volver;
- deshabilitar y volver a habilitar una ruta (A → F pasa de 18 a 19 min y vuelve a 18);
- un id duplicado, y un id escrito con minúsculas y espacios;
- tiempos de −1, 0, 1.000.000 y 1.000.001;
- rutas de un centro a sí mismo, duplicadas o hacia un centro inexistente;
- la baja de un centro con rutas;
- BFS en una red desconectada.

**Extensiones opcionales del enunciado implementadas:** vías de un solo sentido, actualización de tiempos e índice autobalanceado de centros (árbol AVL).

### Formato de los archivos

El Caso 1 no lee ni escribe archivos: los datos se guardan solo en memoria y se cargan al iniciar el programa.

### Limitaciones conocidas

- **Solo en memoria:** los cambios hechos desde el menú se pierden al cerrar el programa.
- **DFS recursivo:** con miles de centros conectados en línea, la recursión podría desbordar la pila de llamadas de Java. La alternativa sería una versión iterativa con la `Pila` propia.
- **Baja y aislamiento de un centro en O(V + E):** hay que recorrer todos los centros, porque una ruta de un solo sentido que llega a un centro está guardada solo en la lista de su origen.
- **El id de un centro no se modifica:** es la clave del árbol AVL. Para cambiarlo hay que dar de baja el centro y volver a darlo de alta.
- **Contar las rutas cuesta O(V + E):** se cuentan recorriendo la red en lugar de llevar un contador aparte.
- **La matriz de tiempos se muestra hasta 20 centros:** con más no entra en pantalla, y se muestran solo la lista de adyacencia y el resumen.
- **Tiempo máximo por ruta: 1.000.000 minutos:** así la suma de un camino no supera el máximo de un `int`, que alcanzaría recién con más de 2.147 tramos de tiempo máximo.
- **Caracteres de la consola:** las flechas y líneas se dibujan con ASCII (`->`, `+-`) para que se vean bien en cualquier consola de Windows.

## Caso 2: archivo histórico de mensajes de sensores

Comprime y descomprime archivos cualesquiera (texto o binarios, cualquier byte de 0 a 255) con **codificación de Huffman**, sin perder información. Cada archivo comprimido es autónomo: lleva adentro todo lo necesario para reconstruir el original.

### Uso del programa

| Opción | Qué hace |
|---|---|
| 1. Comprimir un archivo | Pide el archivo y dónde guardar el `.huf` (por defecto, el mismo nombre con `.huf` agregado). Informa tamaño original, tamaño comprimido desglosado, porcentaje de reducción o aumento y tiempo de proceso |
| 2. Descomprimir un archivo | Descomprime en memoria y valida todo; solo si el archivo es válido pregunta dónde guardar el resultado (por defecto, sin `.huf` y con `.restaurado`). Opcionalmente lo compara con el original |
| 3. Ida y vuelta | Comprime, guarda el `.huf`, lo vuelve a leer del disco, lo descomprime, guarda el `.restaurado` y lo compara byte a byte con el original |
| 4. Frecuencias, códigos y árbol | Tabla de cada byte con su frecuencia, código y longitud; longitud promedio; dibujo del árbol si tiene hasta 32 hojas |
| 5. Comparar dos archivos | Indica si son iguales byte a byte o la posición del primer byte distinto |
| 6. Generar archivos de prueba | Escribe T1 a T6 en una carpeta (por defecto `pruebas-caso2/`, que no se sube al repositorio) |
| 7. Ejecutar pruebas obligatorias | Comprime, descomprime y compara T1 a T5 en memoria, e intenta descomprimir T6 esperando el rechazo |
| 8. Ejecutar pruebas adicionales | Corre 14 pruebas con archivos dañados de distintas formas y archivos válidos poco comunes |

Las rutas pueden ser relativas a la carpeta desde donde se ejecuta el programa o absolutas, con o sin comillas. Antes de sobrescribir un archivo se pide confirmación, y la salida nunca puede ser el mismo archivo que la entrada.

### Datos de prueba

Los genera la opción 6. No se suben al repositorio porque se obtienen siempre iguales:

| Archivo | Contenido |
|---|---|
| `T1-vacio.bin` | 0 bytes |
| `T2-simbolo-unico.bin` | 10.000 veces el byte 65 (`A`) |
| `T3-todos-los-bytes.bin` | los valores 0 a 255, una vez cada uno |
| `T4-muestra.txt` | la muestra de mensajes del enunciado en UTF-8, sin salto de línea final (39 bytes) |
| `T5-binario.bin` | 102.400 bytes: 6.400 registros binarios de 16 bytes que simulan reportes de sensores, generados con semilla fija |
| `T6-truncado.huf` | T5 comprimido y cortado a la mitad |

Resultados de la opción 7:

| Prueba | Original | Comprimido | Resultado | Restaurado | Tiempo (comp. + desc.) |
|---|---|---|---|---|---|
| T1 | 0 B | 19 B | no aplica (original vacío) | igual | 2,676 + 0,439 ms |
| T2 | 10.000 B | 1.271 B | reducción del 87,29 % | igual | 1,404 + 1,353 ms |
| T3 | 256 B | 595 B | aumento del 132,42 % | igual | 1,250 + 0,310 ms |
| T4 | 39 B | 42 B | aumento del 7,69 % | igual | 0,085 + 0,026 ms |
| T5 | 102.400 B | 77.048 B | reducción del 24,76 % | igual | 10,515 + 5,494 ms |
| T6 | 38.524 B | — | rechazado: "El archivo está truncado: faltan datos comprimidos" | sin archivo de salida | — |

Los tiempos son de una ejecución y cambian un poco en cada corrida; miden solo el proceso en memoria. T1 tarda más que T4 porque es la primera prueba e incluye el arranque de la máquina virtual de Java.

Los tamaños de T1 a T4 coinciden con los calculados a mano en el plan. En T4 los códigos son `R` 00, `O` 01, `|` 10, `K` 111, `E` 1100, `A` 11010, `L` 110110 y `T` 110111: 105 bits de datos, 2,69 bits por símbolo en lugar de 8.

La opción 8 agrega 14 pruebas, todas correctas:
- **Archivos dañados**, además del truncado de T6:
  - firma cambiada, o un archivo que no es comprimido;
  - un archivo más corto que la cabecera;
  - versión cambiada;
  - tamaño original alterado;
  - árbol dañado;
  - un bit cambiado en los datos (en T4 solo lo detecta Adler-32);
  - un byte de más al final;
  - el relleno distinto de cero;
  - un byte de menos.
- **Archivos válidos poco comunes:** un solo byte, dos símbolos y 100.000 bytes aleatorios.

**Extensión opcional del enunciado implementada:** el estudio del efecto de la distribución de frecuencias. T2, T3, T4, T5 y el archivo aleatorio cubren distribuciones muy distintas, y el informe analiza cómo comprime cada una.

### Formato del archivo comprimido

El archivo comprimido (`.huf`) tiene una cabecera fija de 19 bytes y después un flujo de bits. Los enteros se guardan en big-endian.

| Posición | Tamaño | Campo | Valores válidos |
|---|---|---|---|
| 0 | 4 bytes | Firma | `T` `H` `U` `F` |
| 4 | 1 byte | Versión del formato | 1 |
| 5 | 8 bytes | Tamaño original en bytes | 0 a 2.147.483.639 |
| 13 | 4 bytes | Suma Adler-32 del contenido original | cualquiera |
| 17 | 2 bytes | Cantidad de símbolos distintos (n) | 0 a 256; 0 solo si el original está vacío |
| 19 | variable | Árbol de Huffman en preorden y, a continuación, los datos codificados | — |
| final | 0 a 7 bits | Relleno con ceros hasta completar el último byte | todos 0 |

- **Árbol en preorden:** cada hoja se escribe como un bit `1` seguido de los 8 bits del símbolo, y cada nodo interno como un bit `0` seguido de su subárbol izquierdo y su subárbol derecho. Ocupa 10n − 1 bits.
- **Datos:** el código de cada byte del original, en orden; el primer bit de cada byte es el más significativo. Si hay un solo símbolo distinto, su código es `0`.
- **Validaciones al descomprimir:** largo mínimo, firma, versión, tamaño original, cantidad de símbolos, bits suficientes, árbol válido (n hojas, sin símbolos repetidos), datos completos, sin bytes de más, relleno en cero y suma Adler-32. Si alguna falla, se informa el motivo y no se genera ningún archivo.

### Limitaciones conocidas

- **Todo en memoria:** el archivo se lee completo, así que el máximo es de unos 2 GB (el tamaño máximo de un arreglo de Java).
- **Los archivos chicos crecen:** la cabecera de 19 bytes y el árbol son un costo fijo (T1, T4).
- **Sin repeticiones no hay ganancia:** con los 256 bytes equiprobables, cada uno sigue ocupando 8 bits, más el árbol (T3).
- **Un símbolo cuesta al menos 1 bit:** la reducción máxima es del 87,5 %, aunque el archivo sea totalmente repetitivo (T2). Huffman mira la frecuencia de cada byte suelto, no las secuencias repetidas; para eso harían falta técnicas como LZ77, que no son parte de la materia.
- **Tiempo informado:** mide solo el proceso en memoria, sin la lectura ni la escritura en disco.
- **Adler-32:** detecta errores accidentales, no modificaciones intencionales, y es algo más débil en archivos de pocos cientos de bytes. En esos casos un daño suele romper antes el árbol, la longitud o el relleno.

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
