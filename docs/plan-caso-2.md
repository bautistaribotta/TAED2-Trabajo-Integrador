# Plan de implementación — Caso 2: Archivo histórico de mensajes de sensores

Este documento es la guía de trabajo del equipo para programar el Caso 2. Define las decisiones tomadas, la arquitectura, el formato del archivo comprimido, la API de cada clase, las fases con sus tareas y los criterios para dar cada parte por terminada. Sigue la misma estructura que `docs/plan-caso-1.md`.

Antes de programar, leer también `AGENTS.md` (reglas de commits e idioma) y el enunciado en `README.md`.

---

## 0. Decisiones tomadas

| Tema | Decisión |
|---|---|
| Lenguaje y proyecto | Igual que el Caso 1: Java 25, Maven, el mismo `pom.xml` y el mismo jar |
| Pruebas | **Sin pruebas unitarias**. Se verifica ejecutando el programa: opción "Ejecutar pruebas obligatorias" del menú y los casos a verificar de cada fase |
| Algoritmo de compresión | **Codificación de Huffman** sobre bytes (alfabeto de 256 símbolos, 0 a 255) |
| Conteo de frecuencias | **Arreglo `long[256]` indexado por el valor del byte** |
| Selección de los dos nodos de menor frecuencia | **`MonticuloMinimo`** del Caso 1 (se reutiliza tal cual) |
| Desempate reproducible | Comparar por frecuencia y, si empatan, por el **menor símbolo** que contiene cada subárbol |
| Información para reconstruir | El **árbol de Huffman serializado en preorden**, como bits, dentro del mismo archivo |
| Bits reales | `EscritorBits` y `LectorBits` propios: empaquetan 8 bits por byte (nunca se guardan los caracteres "0" y "1") |
| Integridad | Firma, versión, tamaño original, cantidad de símbolos, suma de verificación **Adler-32 implementada por el equipo** y validación estricta de la longitud |
| Procesamiento | El archivo se lee **completo en memoria** (`byte[]`). Se descomprime todo en memoria y recién se escribe a disco si todas las validaciones pasan |
| Menú | Se agrega un **menú principal** que permite elegir el Caso 1 o el Caso 2 |
| Estructuras centrales nuevas | Implementadas por el equipo: `ArbolHuffman`, `EscritorBits`, `LectorBits` |

### Qué se puede y qué no se puede usar de la biblioteca de Java

Las mismas reglas del Caso 1, más estas:

- **Permitido:** `java.nio.file.Files` y `Path` para leer y escribir archivos completos (`readAllBytes`, `write`, `exists`, `size`), `System.nanoTime()` para medir tiempos, y `java.util.Random` con semilla fija para generar el archivo de prueba T5 (su algoritmo está fijado por la especificación de Java, así que con la misma semilla da siempre los mismos bytes).
- **Prohibido en `estructuras`, `modelo` y `logica`:** además de las colecciones del Caso 1, `java.util.zip` (`Deflater`, `GZIPOutputStream`, `CRC32`, `Adler32`), `BitSet` y `ByteArrayOutputStream`. Son justamente lo que tenemos que resolver nosotros: compresión, manejo de bits, suma de verificación y armado del arreglo de salida.

---

## 1. Requerimientos funcionales a cubrir

| RF | Enunciado (resumido) | Dónde se resuelve |
|---|---|---|
| RF1 | Leer un archivo y contar las apariciones de cada valor de byte | `TablaFrecuencias` (Fase 2) |
| RF2 | Producir un archivo comprimido con un formato documentado | `CompresorHuffman` + sección 4 de este plan (Fases 3 y 4) |
| RF3 | Restaurar el original de forma exacta y verificar igualdad byte a byte | `CompresorHuffman.descomprimir` + `GestorArchivos.comparar` (Fases 5 y 6) |
| RF4 | Informar tamaño original, tamaño comprimido, porcentaje de reducción o aumento y tiempo de proceso | `ResultadoCompresion` y `ResultadoDescompresion` (Fase 6) |
| RF5 | Resolver archivos vacíos, con un único símbolo, sin repeticiones y binarios | Casos especiales de la Fase 2 y pruebas T1, T2, T3 y T5 (Fase 8) |
| RF6 | Rechazar archivos dañados o con formato no reconocido sin informar falsamente un éxito | `ArchivoDanadoException` y validaciones de la Fase 5; prueba T6 |
| Pruebas | Archivos T1 a T6 | `ArchivosDePrueba` y opción de pruebas del menú (Fase 8) |

---

## 2. Arquitectura

### 2.1 Paquetes y archivos

Los archivos marcados con (nuevo) se crean en este caso; el resto ya existe.

```
src/main/java/tpi/
├── Main.java                          ← ahora abre el menú principal
├── consola/                           (nuevo paquete, compartido por los dos casos)
│   ├── MenuPrincipal.java             (nuevo) elegir Caso 1, Caso 2 o salir
│   └── Entrada.java                   ← se MUEVE desde caso1/consola (un solo Scanner para todo el programa)
├── estructuras/
│   ├── ListaEnlazada.java, Cola.java, Pila.java, ArbolAVL.java
│   ├── MonticuloMinimo.java           ← se reutiliza para construir el árbol de Huffman
│   ├── ArbolHuffman.java              (nuevo) con clase interna NodoHuffman
│   ├── EscritorBits.java              (nuevo)
│   └── LectorBits.java                (nuevo)
├── caso1/ ...                         ← solo cambia el import de Entrada
└── caso2/
    ├── modelo/
    │   ├── TablaFrecuencias.java      (nuevo) long[256] + totales
    │   └── TablaCodigos.java          (nuevo) código y longitud de cada símbolo
    ├── logica/
    │   ├── CompresorHuffman.java      (nuevo) comprimir y descomprimir en memoria (byte[] → byte[])
    │   ├── FormatoHuf.java            (nuevo) constantes del formato, escritura y lectura de la cabecera
    │   ├── SumaAdler32.java           (nuevo) suma de verificación
    │   ├── GestorArchivos.java        (nuevo) leer, escribir, comparar archivos y medir tiempos
    │   ├── ArchivosDePrueba.java      (nuevo) genera el contenido de T1 a T6
    │   ├── ResultadoCompresion.java   (nuevo)
    │   ├── ResultadoDescompresion.java (nuevo)
    │   ├── ResultadoComparacion.java  (nuevo)
    │   ├── ArchivoDanadoException.java (nuevo) formato no reconocido o archivo dañado
    │   └── OperacionArchivoException.java (nuevo) errores de disco: no existe, no se puede leer o escribir
    └── consola/
        ├── MenuCaso2.java             (nuevo)
        └── VistaCaso2.java            (nuevo) formateo de resultados, tablas de frecuencias y códigos
```

`Entrada` se mueve a `tpi.consola` porque los dos menús deben compartir **un único** `Scanner` sobre `System.in`: si cada menú creara el suyo, el primero podría quedarse con datos del búfer que le corresponden al segundo. `MenuPrincipal` crea la `Entrada` y se la pasa a `MenuCaso1` y a `MenuCaso2` por el constructor.

### 2.2 Regla de capas

```
consola  ──usa──▶  logica  ──usa──▶  modelo  ──usa──▶  estructuras
```

- `consola` pide rutas, llama a `logica` y muestra resultados. No calcula nada.
- `logica` nunca imprime ni lee del teclado. Informa con `ArchivoDanadoException`, `OperacionArchivoException` o con objetos `Resultado...`.
- `CompresorHuffman` trabaja **solo en memoria** (`byte[]` → `byte[]`), sin tocar el disco. El disco lo maneja `GestorArchivos`. Así se puede comprimir y descomprimir sin archivos, y la medición del tiempo de proceso no incluye la lectura ni la escritura en disco.
- `estructuras` no sabe nada de sensores ni del formato `.huf`.

### 2.3 Modelo de datos

```
TablaFrecuencias                    ArbolHuffman
 ├── frecuencias : long[256]  ───▶   └── raiz : NodoHuffman
 ├── total : long                          ├── frecuencia : long
 └── distintos : int                       ├── simbolo : int        (solo hojas, 0..255)
                                           ├── menorSimbolo : int   (para desempatar)
                                           ├── izquierdo  → bit 0
                                           └── derecho    → bit 1
                                                   │
                                                   ▼
                                    TablaCodigos
                                     ├── bits : long[256]       (código, alineado a la derecha)
                                     └── longitudes : int[256]  (0 = el símbolo no aparece)
```

- **Por qué un arreglo y no un AVL para las frecuencias:** el alfabeto es fijo y chico (256 valores) y el propio byte sirve de índice. Contar es O(1) por byte, sin comparaciones. Un AVL de símbolos tardaría O(log 256) por byte y ocuparía más memoria por nodo. El AVL conviene cuando las claves son muchas y no se conocen de antemano, que no es este caso.
- **Por qué un heap:** Huffman extrae dos veces el mínimo y vuelve a insertar, n − 1 veces. Con heap cada paso es O(log n) y la construcción es O(n log n). Con una lista desordenada cada extracción es O(n), y el total es O(n²). Con una lista ordenada, extraer es O(1) pero insertar es O(n), y el total también es O(n²). Con n ≤ 256 la diferencia es chica, pero el heap es la estructura correcta y además ya la tenemos.
- **Por qué `long` en las frecuencias:** la suma total y el cálculo de bits de salida (frecuencia × longitud del código) pueden superar `int` en archivos grandes.
- **Por qué un `long` alcanza para cada código:** un archivo en memoria tiene como mucho unos 2^31 bytes. Para que un código tenga longitud d, la suma de frecuencias tiene que ser al menos el número de Fibonacci F(d + 2), y F(47) ya supera 2^31; por eso ningún código pasa de 45 bits, que entran en los 64 de un `long`.

---

## 3. API de cada clase (contrato para trabajar en paralelo)

### 3.1 `estructuras`

```java
public class EscritorBits {
    EscritorBits(int capacidadEnBytes);          // arreglo de tamaño fijo, calculado de antemano
    void escribirBit(int bit);                   // 0 o 1; el primer bit va en la posición más significativa del byte
    void escribirBits(long valor, int cantidad); // los 'cantidad' bits menos significativos de valor, del más alto al más bajo
    void escribirByte(int valor);                // 8 bits
    long bitsEscritos();
    byte[] obtenerBytes();                       // completa el último byte con ceros y devuelve el arreglo
}

public class LectorBits {
    LectorBits(byte[] datos, int desdeByte);     // empieza a leer en esa posición
    int leerBit();                               // lanza IllegalStateException si no quedan bits
    int leerByte();                              // 8 bits, valor 0..255
    long bitsRestantes();
    int bitsDeRellenoPendientes();               // bits que faltan para terminar el byte actual
}

public class ArbolHuffman {
    static ArbolHuffman construir(long[] frecuencias);  // usa MonticuloMinimo; árbol vacío si todas son 0
    boolean estaVacio();
    boolean esHojaUnica();                       // un solo símbolo distinto
    int cantidadHojas();
    int altura();
    void generarCodigos(long[] bits, int[] longitudes); // llena los arreglos recorriendo el árbol
    void escribirEnPreorden(EscritorBits salida);       // serialización (sección 4.2)
    static ArbolHuffman leerEnPreorden(LectorBits entrada, int hojasEsperadas); // excepción si es inválido
    int decodificarSimbolo(LectorBits entrada); // baja desde la raíz siguiendo bits hasta una hoja
    String dibujar();                            // dibujo ASCII, para el menú y el informe
    long bitsSerializado();                      // 10·hojas − 1 (0 si está vacío)
}
```

### 3.2 `caso2.modelo`

```java
public class TablaFrecuencias {
    static TablaFrecuencias contar(byte[] datos);   // recorre una vez: frecuencias[b & 0xFF]++
    long frecuencia(int simbolo);
    long[] comoArreglo();                         // copia, para no exponer el interno
    long total();
    int distintos();
}

public class TablaCodigos {
    static TablaCodigos desde(ArbolHuffman arbol);
    long bits(int simbolo);
    int longitud(int simbolo);
    String comoTexto(int simbolo);                // "11010", solo para mostrar en pantalla
    long bitsDeDatos(TablaFrecuencias f);         // suma de frecuencia × longitud
}
```

### 3.3 `caso2.logica`

```java
public class ArchivoDanadoException extends RuntimeException {
    ArchivoDanadoException(String mensaje);       // el mensaje explica qué validación falló
}

public class OperacionArchivoException extends RuntimeException {
    OperacionArchivoException(String mensaje, Throwable causa);
}

public final class SumaAdler32 {
    static long calcular(byte[] datos);           // valor de 32 bits en un long sin signo
}

public final class FormatoHuf {
    static final byte[] FIRMA = {'T', 'H', 'U', 'F'};
    static final int VERSION = 1;
    static final int TAMANIO_CABECERA = 19;       // bytes fijos antes de los bits
    // escribe y lee la cabecera fija; leerCabecera valida firma, versión y rangos
}

public final class CompresorHuffman {
    static ResultadoCompresion comprimir(byte[] original);
    static ResultadoDescompresion descomprimir(byte[] comprimido);  // lanza ArchivoDanadoException
}

public class ResultadoCompresion {
    byte[] getDatos();                            // archivo .huf completo
    long getTamanioOriginal(); long getTamanioComprimido();
    int getBytesCabecera();                       // fijos: 19
    long getBitsArbol(); long getBitsDatos();
    int getSimbolosDistintos();
    TablaFrecuencias getFrecuencias(); TablaCodigos getCodigos(); ArbolHuffman getArbol();
    double getPorcentaje();                       // positivo = reducción, negativo = aumento; NaN si el original está vacío
    long getNanosegundos();                       // tiempo de proceso en memoria
}

public class ResultadoDescompresion {
    byte[] getDatos();
    long getTamanioOriginal(); long getTamanioComprimido();
    long getNanosegundos();
}

public class ResultadoComparacion {
    boolean sonIguales();
    long getTamanioA(); long getTamanioB();
    long getPrimeraDiferencia();                  // posición del primer byte distinto, -1 si son iguales
}

public final class GestorArchivos {
    static byte[] leer(String ruta);              // OperacionArchivoException si no existe o no se puede leer
    static void escribir(String ruta, byte[] datos);
    static boolean existe(String ruta);
    static ResultadoComparacion comparar(byte[] a, byte[] b);
    static ResultadoComparacion comparar(String rutaA, String rutaB);
}

public final class ArchivosDePrueba {
    static byte[] t1Vacio();                      // 0 bytes
    static byte[] t2SimboloUnico();               // 10.000 veces el byte 65
    static byte[] t3TodosLosBytes();              // 0, 1, ..., 255
    static byte[] t4MuestraMensajes();            // "OK|OK|OK|ERROR|OK|OK|ALERTA|OK|OK|ERROR" en UTF-8
    static byte[] t5Binario();                    // 102.400 bytes de registros binarios, semilla fija
    static byte[] t6Truncado();                   // T5 comprimido y cortado a la mitad
    static void generarEn(String carpeta);        // escribe T1..T6 en disco
}
```

---

## 4. Formato del archivo comprimido (`.huf`)

Esta sección es la documentación del formato que pide el enunciado; se copia al README y al informe.

### 4.1 Disposición

Todos los enteros se guardan en **big-endian** (el byte más significativo primero).

| Posición | Tamaño | Campo | Valores válidos |
|---|---|---|---|
| 0 | 4 bytes | Firma | `T` `H` `U` `F` (0x54 0x48 0x55 0x46) |
| 4 | 1 byte | Versión del formato | 1 |
| 5 | 8 bytes | Tamaño original en bytes | 0 a 2.147.483.639 |
| 13 | 4 bytes | Suma Adler-32 del contenido original | cualquiera |
| 17 | 2 bytes | Cantidad de símbolos distintos (n) | 0 a 256 |
| 19 | variable | Flujo de bits: primero el árbol en preorden y a continuación los datos codificados | — |
| final | 0 a 7 bits | Relleno con ceros hasta completar el último byte | todos 0 |

- Si n = 0 (archivo vacío), no hay árbol ni datos: el archivo comprimido mide exactamente 19 bytes.
- El árbol y los datos van en **un solo flujo de bits continuo**, sin relleno en el medio.
- La longitud total del archivo debe ser exactamente `19 + techo((bitsArbol + bitsDatos) / 8)`. No se aceptan bytes de más ni de menos.

### 4.2 Serialización del árbol en preorden

```
escribir(nodo):
    si nodo es hoja:  escribir bit 1; escribir 8 bits con el símbolo
    si no:            escribir bit 0; escribir(nodo.izquierdo); escribir(nodo.derecho)
```

- Un árbol con n hojas tiene n − 1 nodos internos, así que ocupa **n · 9 + (n − 1) = 10n − 1 bits**.
- Ejemplo: un árbol con raíz interna, `O` a la izquierda y `K` a la derecha se escribe `0 1 01001111 1 01001011`.

**Por qué guardar el árbol y no la tabla de frecuencias** (comparativa para el informe):

| Alternativa | Tamaño para n símbolos | T4 (n = 8) | T3 (n = 256) | Observación |
|---|---|---|---|---|
| Frecuencias: n × (1 byte de símbolo + 4 bytes de frecuencia) | 5n bytes | 40 bytes | 1.280 bytes | El descompresor debe reconstruir el árbol con **exactamente** el mismo desempate |
| Frecuencias de los 256 valores, fijas | 1.024 bytes | 1.024 bytes | 1.024 bytes | Simple, pero inaceptable para archivos chicos |
| **Árbol en preorden (elegida)** | 10n − 1 bits | 79 bits ≈ 10 bytes | 2.559 bits ≈ 320 bytes | Es lo mínimo que se necesita: la forma del árbol y qué símbolo hay en cada hoja |
| Longitudes de códigos canónicos | n bytes + n símbolos | 16 bytes | 256 bytes | Más compacta para n grande, pero agrega el concepto de código canónico, que no se vio en la materia |

### 4.3 Codificación de los datos

- Por cada byte del original, en orden, se escriben los bits de su código, del primero al último. El primer bit de cada byte de salida es el más significativo.
- **Caso de un único símbolo distinto** (T2): el árbol es una sola hoja y no tendría camino. Se le asigna por convención el código `0` (1 bit). Al descomprimir, si la raíz es una hoja, cada bit leído debe ser 0 y produce ese símbolo.

### 4.4 Validaciones al descomprimir (en este orden)

Si cualquiera falla, se lanza `ArchivoDanadoException` con el mensaje indicado y **no se escribe ningún archivo**.

| # | Validación | Mensaje |
|---|---|---|
| 1 | El archivo mide al menos 19 bytes | "El archivo es demasiado corto para ser un archivo comprimido" |
| 2 | La firma es `THUF` | "Formato no reconocido: el archivo no fue generado por este programa" |
| 3 | La versión es 1 | "Versión de formato no soportada: X" |
| 4 | Tamaño original entre 0 y 2.147.483.639 | "Tamaño original inválido en la cabecera" |
| 5 | n entre 0 y 256; n = 0 si y solo si el tamaño original es 0 | "Cantidad de símbolos inválida en la cabecera" |
| 6 | Hay bits suficientes: tamaño original ≤ bits disponibles (cada símbolo ocupa al menos 1 bit). Se controla **antes** de reservar memoria, para que una cabecera dañada no pida un arreglo gigante | "El archivo está truncado o dañado" |
| 7 | El árbol se lee sin quedarse sin bits, tiene exactamente n hojas, no repite símbolos y su profundidad no pasa de 255 | "El árbol de códigos está dañado" |
| 8 | Se decodifican exactamente "tamaño original" símbolos sin quedarse sin bits | "El archivo está truncado: faltan datos comprimidos" |
| 9 | Lo que sobra es solo el relleno del último byte (menos de 8 bits) y son todos ceros | "El archivo tiene datos de más o un relleno inválido" |
| 10 | La suma Adler-32 de lo reconstruido coincide con la de la cabecera | "La suma de verificación no coincide: el archivo está dañado" |

La validación 10 es la que detecta un bit cambiado en el medio de los datos que, por casualidad, igual se decodifica a la cantidad correcta de símbolos.

### 4.5 Suma Adler-32

Se eligió porque es corta de implementar y explicar, y detecta prácticamente cualquier cambio accidental:

```
a = 1; b = 0
para cada byte x del original (como 0..255):
    a = (a + x) mod 65521
    b = (b + a) mod 65521
resultado = b · 65536 + a
```

---

## 5. Fases de trabajo

Cada fase tiene tareas concretas y una **definición de terminado**. Los casos a verificar se prueban a mano, con el programa o con un `main` temporal fuera del repositorio.

### Fase 0 — Preparar el proyecto

1. Crear el paquete `tpi.consola` y mover `Entrada` ahí. Actualizar el import en `MenuCaso1` y `Vista` si lo usan.
2. `MenuCaso1` recibe la `Entrada` por constructor en lugar de crearla.
3. Crear `MenuPrincipal`:
   ```
   ===== TPI — TALLER DE ALGORITMOS Y ESTRUCTURAS DE DATOS II =====
    1. Caso 1: Distribución de insumos entre centros logísticos
    2. Caso 2: Archivo histórico de mensajes de sensores
    0. Salir
   ```
   Al volver de un caso se regresa a este menú. La red del Caso 1 se conserva mientras el programa esté abierto.
4. `Main` abre `MenuPrincipal`.
5. Crear los paquetes de `caso2` vacíos.
6. Agregar `pruebas-caso2/` al `.gitignore` (ahí se generan los archivos de prueba; no se suben).

**Terminado cuando:** `mvn package` compila y el Caso 1 se comporta igual que antes, ahora entrando por el menú principal.

### Fase 1 — Manejo de bits

**1.1 `EscritorBits`**
- Atributos: `byte[] buffer`, `int byteActual` (acumulador), `int bitsEnAcumulador` (0 a 7), `int posicion`.
- `escribirBit(b)`: `byteActual = (byteActual << 1) | b`; si se juntaron 8 bits, guardar en `buffer[posicion++]` y reiniciar.
- `escribirBits(valor, cantidad)`: para `i` desde `cantidad − 1` hasta 0, `escribirBit((valor >>> i) & 1)`.
- `obtenerBytes()`: si quedaron bits pendientes, desplazar a la izquierda `8 − bitsEnAcumulador` (relleno con ceros) y guardar.
- La capacidad se calcula antes de crear el escritor (sección 4.1), así que no hace falta un arreglo que crezca.

**1.2 `LectorBits`**
- `leerBit()`: `(datos[posByte] >>> (7 − posBit)) & 1`; avanzar `posBit` y, al llegar a 8, pasar al byte siguiente.
- Si no quedan bits, lanzar `IllegalStateException`. `CompresorHuffman` la convierte en `ArchivoDanadoException` con el mensaje adecuado.

**Casos a verificar:** escribir los bits `1,0,1` y obtener un byte `10100000` (0xA0); escribir 8 bits exactos y obtener un byte sin relleno; escribir `0x1FF` con 9 bits y obtener 2 bytes `11111111 10000000`; leer lo escrito y obtener los mismos bits; leer más allá del final lanza la excepción.

### Fase 2 — Frecuencias y árbol de Huffman

**2.1 `TablaFrecuencias.contar`:** un `for` sobre los bytes, `frecuencias[b & 0xFF]++`. El `& 0xFF` es imprescindible: en Java `byte` tiene signo y el byte 200 se lee como −56.

**2.2 `ArbolHuffman.construir`:**
```
heap = MonticuloMinimo con comparador (frecuencia, menorSimbolo)
para s de 0 a 255: si frecuencias[s] > 0: heap.insertar(hoja(s, frecuencias[s]))
si heap vacío: devolver árbol vacío
si heap tiene 1 elemento: devolver árbol con esa hoja como raíz      ← caso T2
mientras heap.tamanio() > 1:
    x = heap.extraerMinimo()          ← va a la izquierda (bit 0)
    y = heap.extraerMinimo()          ← va a la derecha  (bit 1)
    heap.insertar(interno(x, y, frecuencia = x.f + y.f, menorSimbolo = min(x.ms, y.ms)))
raiz = heap.extraerMinimo()
```

**2.3 Desempate reproducible** (pregunta de diseño del enunciado): el comparador nunca devuelve "iguales" entre dos nodos distintos, porque dos subárboles del heap nunca comparten símbolos y por lo tanto tienen `menorSimbolo` distinto. Así, el orden de extracción queda totalmente determinado por los datos: la misma entrada produce siempre el mismo árbol, los mismos códigos y el mismo archivo comprimido, byte por byte, en cualquier máquina.

**2.4 `generarCodigos`** (recursivo, acumulando el código en un `long`):
```
generar(nodo, codigo, longitud):
    si nodo es hoja:
        bits[nodo.simbolo] = codigo
        longitudes[nodo.simbolo] = (longitud == 0) ? 1 : longitud   ← hoja única: código "0"
    si no:
        generar(nodo.izq, codigo << 1,       longitud + 1)
        generar(nodo.der, (codigo << 1) | 1, longitud + 1)
```

**2.5 `dibujar`:** dibujo de costado como el del AVL, mostrando en las hojas el símbolo (como carácter si es imprimible, si no en hexadecimal: `0x0A`) y la frecuencia.

**Casos a verificar:**
- Frecuencias de T4: `O` 9, `|` 9, `K` 7, `R` 7, `E` 3, `A` 2, `L` 1, `T` 1; total 39; 8 distintos.
- Árbol de T4: los códigos deben coincidir con la tabla de la sección 6.3.
- Ningún código es prefijo de otro (recorrer los pares).
- 256 símbolos con frecuencia 1: todos los códigos miden 8 bits.
- Construir dos veces con las mismas frecuencias da exactamente los mismos códigos.

### Fase 3 — Serialización del árbol y suma de verificación

1. `escribirEnPreorden` según la sección 4.2.
2. `leerEnPreorden` (recursivo con profundidad máxima 255):
   ```
   leer(profundidad):
       si profundidad > 255: error
       bit = leerBit()
       si bit == 1: s = leerByte(); si s ya apareció: error; contarHoja(); devolver hoja(s)
       izq = leer(profundidad + 1); der = leer(profundidad + 1)
       devolver interno(izq, der)
   al terminar: si hojas leídas != hojasEsperadas: error
   ```
   El límite de profundidad evita que un archivo dañado provoque un desbordamiento de la pila de llamadas.
3. `SumaAdler32.calcular` según la sección 4.5.

**Casos a verificar:** escribir y volver a leer el árbol de T4 da los mismos códigos; el árbol de T4 ocupa 79 bits; Adler-32 de los bytes de "Wikipedia" da 0x11E60398 (valor de referencia conocido); Adler-32 de un arreglo vacío da 1.

### Fase 4 — Compresión (RF1 y RF2)

```
comprimir(original):
    t0 = nanoTime()
    frec = TablaFrecuencias.contar(original)
    arbol = ArbolHuffman.construir(frec.comoArreglo())
    codigos = TablaCodigos.desde(arbol)
    bitsArbol = arbol.bitsSerializado()
    bitsDatos = codigos.bitsDeDatos(frec)
    tamanio = 19 + techo((bitsArbol + bitsDatos) / 8)
    escritor = new EscritorBits(tamanio)
    escribir cabecera: firma, versión, original.length, Adler-32, frec.distintos()
    arbol.escribirEnPreorden(escritor)
    para cada byte b del original: escritor.escribirBits(codigos.bits(b), codigos.longitud(b))
    t1 = nanoTime()
    devolver ResultadoCompresion(...)
```

- Si `tamanio` supera la capacidad de un arreglo de Java, se rechaza con un mensaje claro (límite documentado).
- `getPorcentaje() = (original − comprimido) / original × 100`. Si el original mide 0, devuelve `NaN` y la consola muestra "no aplica (archivo vacío)".

**Casos a verificar:** los tamaños de la sección 6.3 para T1 a T4.

### Fase 5 — Descompresión y detección de daños (RF3 y RF6)

```
descomprimir(datos):
    t0 = nanoTime()
    validar cabecera (validaciones 1 a 6 de la sección 4.4)
    si tamañoOriginal == 0: validar que no haya más bytes; devolver arreglo vacío
    lector = new LectorBits(datos, 19)
    arbol = ArbolHuffman.leerEnPreorden(lector, n)          (validación 7)
    salida = new byte[tamañoOriginal]
    para i de 0 a tamañoOriginal − 1:
        salida[i] = arbol.decodificarSimbolo(lector)        (validación 8)
    validar relleno y longitud exacta                       (validación 9)
    validar Adler-32(salida) == Adler-32 de la cabecera     (validación 10)
    t1 = nanoTime()
    devolver ResultadoDescompresion(...)
```

- `decodificarSimbolo`: desde la raíz, mientras el nodo no sea hoja, leer un bit e ir a la izquierda (0) o a la derecha (1). Si la raíz es una hoja (símbolo único), leer un bit que debe ser 0.
- Toda `IllegalStateException` del lector se transforma en `ArchivoDanadoException`. **Nunca** se informa éxito ni se escribe un archivo parcial.

**Casos a verificar** (además de T6):
- archivo de 10 bytes cualquiera (demasiado corto);
- intentar descomprimir T4 original (firma incorrecta);
- versión cambiada a 2;
- un byte cambiado en el medio de los datos (debe fallar alguna validación, normalmente la 10);
- un byte agregado al final (validación 9);
- el árbol dañado: cambiar el primer bit después de la cabecera.

### Fase 6 — Archivos, comparación y mediciones (RF3 y RF4)

1. `GestorArchivos.leer/escribir` con `Files.readAllBytes` y `Files.write`. Toda `IOException` se convierte en `OperacionArchivoException` con un mensaje legible ("No existe el archivo X", "No se pudo escribir X").
2. `comparar(a, b)`: primero los tamaños; después byte a byte hasta la primera diferencia. O(N).
3. Tiempo: se informa en milisegundos con 3 decimales. Se mide solo el proceso en memoria; se aclara en pantalla que no incluye la lectura ni la escritura en disco.
4. Reglas de la consola al escribir: no permitir que la salida sea el mismo archivo que la entrada, y pedir confirmación antes de sobrescribir un archivo existente.

### Fase 7 — Interfaz de consola

```
===== ARCHIVO DE MENSAJES — CASO 2 =====
 1. Comprimir un archivo
 2. Descomprimir un archivo
 3. Comprimir, restaurar y verificar (ida y vuelta)
 4. Ver frecuencias, códigos y árbol de un archivo
 5. Comparar dos archivos byte a byte
 6. Generar archivos de prueba T1 a T6
 7. Ejecutar pruebas obligatorias
 0. Volver
```

- **1. Comprimir:** pide la ruta de entrada y la de salida (por defecto, la de entrada con `.huf` agregado). Muestra:
  ```
  Compresión de pruebas-caso2/T4.txt
    Tamaño original:     39 bytes
    Tamaño comprimido:   42 bytes  (cabecera 19 + árbol 79 bits + datos 105 bits + relleno 0 bits)
    Resultado:           aumento del 7,69 %
    Símbolos distintos:  8
    Tiempo de proceso:   0,042 ms (sin contar lectura y escritura en disco)
  ```
- **2. Descomprimir:** pide el `.huf` y la ruta de salida (por defecto, quitando `.huf` y agregando `.restaurado`). Opcionalmente pide el original para comparar byte a byte. Si el archivo está dañado, muestra "No se pudo descomprimir: " seguido del motivo, y aclara que no se generó ningún archivo.
- **3. Ida y vuelta:** comprime, escribe el `.huf`, lo vuelve a leer **desde disco**, lo descomprime, escribe el restaurado y compara con el original. Termina con "Verificación byte a byte: IGUALES" o con la posición de la primera diferencia.
- **4. Análisis:** tabla con símbolo, frecuencia, código y longitud, ordenada por símbolo; longitud promedio en bits por símbolo, comparada con 8; dibujo del árbol solo si tiene hasta 32 hojas.
- **6. Generar:** escribe T1 a T6 en `pruebas-caso2/` (la crea si no existe) e informa la ruta absoluta.
- **7. Pruebas obligatorias:** ver Fase 8.
- Como en el Caso 1, cada opción va dentro de un `try/catch` y **el programa nunca termina por una excepción**.

### Fase 8 — Archivos de prueba y pruebas obligatorias

**8.1 Contenido de cada archivo** (generado por `ArchivosDePrueba`, no subido al repositorio):

| Archivo | Nombre en disco | Cómo se genera |
|---|---|---|
| T1 | `T1-vacio.bin` | arreglo de 0 bytes |
| T2 | `T2-simbolo-unico.bin` | 10.000 veces el byte 65 (`A`) |
| T3 | `T3-todos-los-bytes.bin` | los valores 0 a 255, una vez cada uno, en orden |
| T4 | `T4-muestra.txt` | `OK\|OK\|OK\|ERROR\|OK\|OK\|ALERTA\|OK\|OK\|ERROR` en UTF-8, sin salto de línea final (39 bytes) |
| T5 | `T5-binario.bin` | 102.400 bytes: 6.400 registros de 16 bytes que simulan reportes binarios de sensores (id de equipo entre 20, estado con predominio de "OK", temperatura con variaciones chicas, marca de tiempo creciente), con `Random(2025)` |
| T6 | `T6-truncado.huf` | T5 comprimido y cortado a la mitad de su longitud |

**8.2 Opción 7 del menú:** trabaja **en memoria** (no depende de que existan los archivos) y, para T1 a T5: comprime, descomprime, compara byte a byte y muestra una fila de la tabla. Para T6 intenta descomprimir y espera el rechazo. Termina con un resumen "6 de 6 pruebas correctas".

```
Prueba  Original   Comprimido  Resultado        Restaurado   Tiempo
T1           0 B        19 B   no aplica        IGUAL        0,010 ms
T2      10.000 B     1.271 B   reducción 87,29 %  IGUAL      ...
...
T6      (truncado)             RECHAZADO: El archivo está truncado: faltan datos comprimidos
```

**8.3 Resultados esperados**, calculados a mano. Si la salida no coincide, hay un error en la implementación:

| Prueba | Original | Árbol | Datos | Comprimido | Resultado esperado |
|---|---|---|---|---|---|
| T1 | 0 B | 0 bits | 0 bits | 19 B | aumento; porcentaje "no aplica"; restaurado vacío |
| T2 | 10.000 B | 9 bits | 10.000 bits | 19 + 1.252 = **1.271 B** | reducción del 87,29 % |
| T3 | 256 B | 2.559 bits | 2.048 bits | 19 + 576 = **595 B** | aumento del 132,42 % |
| T4 | 39 B | 79 bits | 105 bits | 19 + 23 = **42 B** | aumento del 7,69 % |
| T5 | 102.400 B | — | — | menor que el original | restaurado IGUAL byte a byte |
| T6 | — | — | — | — | RECHAZADO, sin archivo de salida |

**Códigos esperados de T4** (con el desempate de la sección 2.3; 0 = izquierda):

| Símbolo | Frecuencia | Código | Longitud | Bits aportados |
|---|---|---|---|---|
| `R` | 7 | `00` | 2 | 14 |
| `O` | 9 | `01` | 2 | 18 |
| `\|` | 9 | `10` | 2 | 18 |
| `K` | 7 | `111` | 3 | 21 |
| `E` | 3 | `1100` | 4 | 12 |
| `A` | 2 | `11010` | 5 | 10 |
| `L` | 1 | `110110` | 6 | 6 |
| `T` | 1 | `110111` | 6 | 6 |
| **Total** | **39** | | promedio 2,69 bits | **105** |

Paso a paso de la construcción (para el informe): L+T = 2; A+(LT) = 4 (A va primero porque, con frecuencia 2 empatada, su menor símbolo `A` es menor que `L`); E+4 = 7; (EALT)+K = 14 (empate en 7 con K y R: gana el subárbol cuyo menor símbolo es `A`, y luego K < R); R+O = 16; \|+14 = 23; 16+23 = 39.

**8.4 Lectura de los resultados** (para el informe y la defensa):
- T1 y T4 muestran que **la cabecera domina en archivos chicos**: 19 bytes fijos más el árbol hacen que comprimir 39 bytes dé 42.
- T3 muestra que **sin repeticiones no hay nada que aprovechar**: con 256 símbolos equiprobables Huffman asigna 8 bits a cada uno (igual que el original), y además se paga el árbol.
- T2 muestra el **límite de Huffman**: cada símbolo cuesta al menos 1 bit, así que la reducción máxima es del 87,5 % aunque el archivo sea totalmente repetitivo. Técnicas como RLE o LZ77 lo harían mejor, pero no son parte de la materia.

**8.5 Registro para el informe:** copiar la salida real de la opción 7 y la del análisis de T4 (opción 4).

### Fase 9 — Cierre

1. README: sección del Caso 2 con cómo usarlo, el formato `.huf` (sección 4 completa), los archivos de prueba con sus resultados y las limitaciones conocidas:
   - el archivo se procesa completo en memoria (límite de unos 2 GB, el máximo de un arreglo de Java);
   - el tiempo informado no incluye la lectura ni la escritura en disco;
   - la cabecera fija de 19 bytes y el árbol hacen que los archivos chicos crezcan;
   - un símbolo cuesta al menos 1 bit (reducción máxima del 87,5 %);
   - Adler-32 detecta errores accidentales, no modificaciones intencionales.
2. Word: agregar las secciones "Codificación de Huffman", "Manejo de bits" y "Formato del archivo comprimido", con el mismo estilo que las del Caso 1.
3. Revisar que `estructuras`, `modelo` y `logica` no importen colecciones de `java.util` ni `java.util.zip`.
4. Repasar la sección 7 de este plan.

---

## 6. Dependencias entre fases

```
Fase 0 ─┬─▶ Fase 1 (bits) ──────┬──▶ Fase 3 (serialización + Adler-32) ─┐
        └─▶ Fase 2 (frecuencias ┘                                       ├─▶ Fase 4 (comprimir) ─▶ Fase 5 (descomprimir) ─┐
            + árbol + códigos) ─────────────────────────────────────────┘                                                │
                                                                Fase 6 (archivos y comparación) ─▶ Fase 7 (consola) ◀────┘
                                                                                                         │
                                                                                         Fase 8 ─▶ Fase 9
```

| Etapa | Integrante 1 | Integrante 2 |
|---|---|---|
| A (en paralelo) | Fase 1: `EscritorBits`, `LectorBits` | Fase 0 + Fase 2: menú principal, frecuencias, árbol y códigos |
| B | Fase 3: serialización y Adler-32 | Fase 6: `GestorArchivos` y comparación |
| C | Fase 4 y Fase 5: compresor | Fase 7: menú y `VistaCaso2` |
| D (los dos) | Fase 8: pruebas obligatorias | Fase 9: README, Word y revisión |

La forma de trabajo con Git es la misma de la sección 7 del plan del Caso 1.

---

## 7. Material para el informe

| Pregunta de diseño del enunciado | Respuesta que sale de este plan |
|---|---|
| ¿Qué propiedad de los mensajes podría aprovecharse? | La **frecuencia desigual** de los bytes: en los reportes repetitivos unos pocos valores (`O`, `K`, `\|`) aparecen mucho. Huffman les da códigos cortos y deja los largos para los raros. En T4 el promedio baja de 8 a 2,69 bits por símbolo |
| ¿Qué información mínima hay que conservar? | La forma del árbol con el símbolo de cada hoja (10n − 1 bits), el tamaño original (para saber cuándo parar, porque el relleno del último byte no se distingue de datos) y, para detectar daños, una suma de verificación. Comparativa con guardar frecuencias en la sección 4.2 |
| ¿Cómo representar símbolos y frecuencias? ¿Qué estructura para elegir los mínimos? | Arreglo `long[256]` indexado por byte (sección 2.3) y montículo de mínimos: O(n log n) frente a O(n²) con listas |
| ¿Cómo pasar de códigos de longitud variable a bits reales? | `EscritorBits`: acumula bits con desplazamientos y operaciones a nivel de bits, y vuelca un byte cada 8 bits. Escribir "0" y "1" como texto ocuparía 8 bits por bit: el archivo sería 8 veces más grande que el resultado de Huffman |
| ¿El tamaño comprimido siempre es menor? | No: T1 (+19 B), T3 (+132 %) y T4 (+7,7 %). Ningún compresor sin pérdida puede achicar todos los archivos (hay más archivos de n bytes que archivos de menos de n bytes). La cabecera es un costo fijo que pesa en los chicos |
| ¿Cómo garantizar resultados reproducibles con frecuencias iguales? | Desempate por menor símbolo del subárbol (sección 2.3): orden total, sin depender del orden interno del heap. Además, como el árbol viaja en el archivo, el descompresor ni siquiera necesita repetir la construcción |
| Complejidad | Contar O(N); árbol O(n log n) con n ≤ 256; codificar O(N); decodificar O(N · L), con L la longitud promedio del código; memoria O(N) |
| Defensa: ¿qué cambiaría con otra escala? | Archivos de varios GB: procesar por bloques con un árbol por bloque (extensión opcional del enunciado). Datos con secuencias repetidas largas: combinar con LZ77 (como hace Deflate), porque Huffman solo mira la frecuencia de cada byte suelto y no su contexto |
