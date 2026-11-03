# Plan de implementación — Caso 1: Distribución de insumos entre centros logísticos

Este documento es la guía de trabajo del equipo para programar el Caso 1. Define las decisiones ya tomadas, la arquitectura, la API de cada clase (para poder trabajar en paralelo), las fases con sus tareas y los criterios para dar cada parte por terminada.

Antes de programar, leer también `AGENTS.md` (reglas de commits e idioma) y el enunciado en `README.md`.

---

## 0. Decisiones tomadas

| Tema | Decisión |
|---|---|
| Lenguaje | Java 25 (LTS), aplicación de consola |
| IDE | IntelliJ IDEA |
| Proyecto | Maven (IntelliJ lo abre igual en todas las máquinas) |
| Pruebas | **Sin pruebas unitarias** (exceden el contenido de la materia). Se verifica ejecutando el programa: opción 7 del menú y los casos a verificar de cada fase |
| Persistencia | **Solo en memoria**. Los datos iniciales se cargan al iniciar usando las mismas operaciones de alta |
| Red de centros y rutas | **Grafo ponderado** representado con **listas de adyacencia** |
| Índice de centros por id | **Árbol AVL** (clave = id del centro). Es además el conjunto de vértices del grafo |
| Camino mínimo | **Dijkstra** con **montículo (heap) de mínimos** propio |
| Exploración de la red | **BFS** (con cola propia) y **DFS** (recursivo); el usuario elige |
| Estructuras centrales | Implementadas por el equipo: `ListaEnlazada`, `Cola`, `Pila`, `MonticuloMinimo`, `ArbolAVL` |

### Qué se puede y qué no se puede usar de la biblioteca de Java

- **Permitido:** `String`, `StringBuilder`, `Scanner`, `Math`, clases envoltorio (`Integer`, etc.), excepciones, e **interfaces** como `Comparable`, `Comparator`, `Iterable`, `Iterator`, `java.util.function.*`.
- **Prohibido en `estructuras`, `modelo` y `logica`:** `ArrayList`, `LinkedList`, `HashMap`, `TreeMap`, `PriorityQueue`, `ArrayDeque`, `Stack`, `Collections.sort`, o cualquier librería de grafos. Esas son justamente las estructuras que tenemos que implementar.

---

## 1. Requerimientos funcionales a cubrir

| RF | Enunciado (resumido) | Dónde se resuelve |
|---|---|---|
| RF1 | Registrar, modificar y consultar centros por id único; rechazar duplicados | `RedLogistica` + `ArbolAVL` (Fase 4) |
| RF2 | Registrar rutas con tiempos; dar de baja temporalmente una conexión | `RedLogistica` + `Ruta.activa` (Fase 5) |
| RF3 | Recorrido de menor tiempo total entre dos centros, con orden de visita y costo acumulado | `Dijkstra` (Fase 6) |
| RF4 | Informar: no existe recorrido, origen o destino no registrado, origen y destino coinciden | `ResultadoCamino.Estado` (Fase 6) |
| RF5 | Recorrido de exploración desde un centro, indicando el criterio y diferenciándolo del camino mínimo | `Exploracion` BFS/DFS (Fase 7) |
| Pruebas | A→F, A→G, B→E, A→A, centro inexistente; aislar G y repetir búsqueda hacia G | Fase 9 |

Extras que agregamos porque los pide la prueba o mejoran la demo: baja de centro, modificar tiempo de ruta, eliminar ruta, aislar centro, mostrar la red, listar centros ordenados.

---

## 2. Arquitectura

### 2.1 Paquetes y archivos

```
pom.xml
src/main/java/tpi/
├── Main.java                          ← punto de entrada; abre el menú del Caso 1
├── estructuras/                       ← genéricas, reutilizables en el Caso 2 (heap → Huffman)
│   ├── ListaEnlazada.java
│   ├── Cola.java
│   ├── Pila.java
│   ├── MonticuloMinimo.java
│   └── ArbolAVL.java                  ← con clase interna NodoAVL
└── caso1/
    ├── modelo/
    │   ├── Centro.java                ← vértice del grafo
    │   └── Ruta.java                  ← arista ponderada
    ├── logica/
    │   ├── RedLogistica.java          ← grafo + reglas de negocio (validaciones)
    │   ├── Dijkstra.java
    │   ├── Exploracion.java           ← BFS y DFS
    │   ├── ResultadoCamino.java
    │   ├── ResultadoExploracion.java
    │   ├── OperacionInvalidaException.java
    │   └── DatosIniciales.java        ← carga los 7 centros y 10 rutas del enunciado
    └── consola/
        ├── MenuCaso1.java             ← menú principal y submenús
        ├── Entrada.java               ← lectura validada desde teclado
        └── Vista.java                 ← formateo de salidas y dibujo ASCII de la red
```

### 2.2 Regla de capas

```
consola  ──usa──▶  logica  ──usa──▶  modelo  ──usa──▶  estructuras
```

- `consola` **solo** lee datos, llama a `logica` y muestra resultados. No valida reglas de negocio ni calcula nada.
- `logica` **nunca** imprime por pantalla ni lee del teclado. Informa errores con `OperacionInvalidaException` o devolviendo objetos `Resultado...`. Así la lógica se puede usar y verificar sin depender de la consola.
- `estructuras` no conoce nada del dominio (no sabe qué es un centro).

### 2.3 Modelo de datos

```
RedLogistica
 └── centros : ArbolAVL<String, Centro>        (índice por id, O(log n))
                  │
                  ▼
               Centro  ── id, descripcion
                  └── rutas : ListaEnlazada<Ruta>   (lista de adyacencia)
                                  │
                                  ▼
                                Ruta ── origen, destino, tiempo, bidireccional, activa
```

- Los **vértices** del grafo son los `Centro` guardados en el AVL. No hay una segunda lista de vértices: el AVL es la única fuente de verdad.
- Las **aristas** son objetos `Ruta`.
  - Una ruta **bidireccional** es **un único objeto** que está en la lista de adyacencia de **ambos** extremos. Así, deshabilitarla o cambiarle el tiempo afecta los dos sentidos a la vez.
  - Una ruta **de un solo sentido** está solo en la lista del origen.
- Cada lista de adyacencia se mantiene **ordenada por id del vecino**. Esto hace que BFS, DFS y los desempates de Dijkstra sean **reproducibles**: siempre dan el mismo resultado.

---

## 3. API de cada clase (contrato para trabajar en paralelo)

Las firmas son el acuerdo entre integrantes. Si alguien necesita cambiar una, lo avisa antes.

### 3.1 `estructuras`

```java
public class ListaEnlazada<T> implements Iterable<T> {
    void agregarAlFinal(T dato);
    void agregarOrdenado(T dato, Comparator<T> criterio);  // inserta manteniendo el orden
    boolean eliminar(T dato);                              // por identidad (==) o equals; true si lo encontró
    T buscar(Predicate<T> condicion);                      // primer elemento que cumple, o null
    T obtener(int indice);                                 // para Vista
    int tamanio();
    boolean estaVacia();
    Iterator<T> iterator();                                // permite usar for-each
}

public class Cola<T> {          // FIFO, implementada con nodos enlazados (frente y fin)
    void encolar(T dato);       // O(1)
    T desencolar();             // O(1); lanza excepción si está vacía
    T verFrente();
    boolean estaVacia();
    int tamanio();
}

public class Pila<T> {          // LIFO, nodos enlazados
    void apilar(T dato);        // O(1)
    T desapilar();              // O(1); lanza excepción si está vacía
    T verTope();
    boolean estaVacia();
    int tamanio();
}

public class MonticuloMinimo<T> {                 // heap binario sobre arreglo
    MonticuloMinimo(Comparator<T> criterio);      // capacidad inicial 16, se duplica al llenarse
    void insertar(T dato);                        // O(log n), "flotar" (sift-up)
    T extraerMinimo();                            // O(log n), "hundir" (sift-down); excepción si vacío
    T verMinimo();
    boolean estaVacio();
    int tamanio();
}

public class ArbolAVL<K extends Comparable<K>, V> {
    boolean insertar(K clave, V valor);   // false si la clave ya existe (no la pisa)
    V buscar(K clave);                    // null si no existe
    boolean contiene(K clave);
    boolean eliminar(K clave);            // false si no existía
    ListaEnlazada<V> valoresEnOrden();    // recorrido inorden → ordenados por clave
    int tamanio();
    int altura();                         // árbol vacío = 0, una hoja = 1
    boolean estaVacio();
    String dibujar();                     // dibujo ASCII del árbol (para depurar y para el informe)
    boolean esValido();                   // verifica orden de ABB, |factor de balance| <= 1 y alturas guardadas (para comprobar el árbol durante el desarrollo)
}
```

### 3.2 `caso1.modelo`

```java
public class Centro {
    Centro(String id, String descripcion);
    String getId();                       // inmutable
    String getDescripcion();
    void setDescripcion(String descripcion);
    ListaEnlazada<Ruta> getRutas();       // lista de adyacencia (rutas que tocan a este centro)
    // uso interno de los algoritmos (visibilidad de paquete o público documentado):
    int getIndice(); void setIndice(int i);
}

public class Ruta {
    Ruta(Centro origen, Centro destino, int tiempo, boolean bidireccional);  // nace activa
    Centro getOrigen(); Centro getDestino();
    int getTiempo(); void setTiempo(int minutos);
    boolean isBidireccional();
    boolean isActiva(); void setActiva(boolean activa);
    boolean permiteSalirDesde(Centro c);  // activa && (c == origen || (bidireccional && c == destino))
    Centro otroExtremo(Centro c);         // el extremo que no es c
    boolean conecta(Centro desde, Centro hacia); // ¿se puede ir de 'desde' a 'hacia' por esta ruta (ignorando si está activa)?
}
```

### 3.3 `caso1.logica`

```java
public class OperacionInvalidaException extends RuntimeException {
    OperacionInvalidaException(String mensaje);  // el mensaje es el que se muestra al usuario
}

public class RedLogistica {
    // --- Centros (RF1) ---
    void altaCentro(String id, String descripcion);
    void modificarCentro(String id, String nuevaDescripcion);
    Centro consultarCentro(String id);            // excepción si no existe
    boolean existeCentro(String id);
    ListaEnlazada<Centro> listarCentros();        // ordenados por id (inorden del AVL)
    void bajaCentro(String id);                   // también elimina todas sus rutas
    int cantidadCentros();

    // --- Rutas (RF2) ---
    void altaRuta(String origen, String destino, int tiempo, boolean bidireccional);
    void modificarTiempoRuta(String origen, String destino, int nuevoTiempo);
    void deshabilitarRuta(String origen, String destino);   // baja temporal
    void habilitarRuta(String origen, String destino);
    void eliminarRuta(String origen, String destino);       // baja definitiva
    int aislarCentro(String id);                  // deshabilita todas sus rutas; devuelve cuántas
    Ruta buscarRuta(String origen, String destino); // null si no hay ruta que permita ir de origen a destino
    ListaEnlazada<Ruta> listarRutas();            // cada ruta una sola vez (sin duplicar las bidireccionales)
    int cantidadRutas();

    static String normalizarId(String id);        // trim + mayúsculas; usado por todos los métodos
}

public class ResultadoCamino {
    enum Estado { ENCONTRADO, MISMO_CENTRO, SIN_CAMINO, ORIGEN_INEXISTENTE, DESTINO_INEXISTENTE }
    Estado getEstado();
    ListaEnlazada<Centro> getCamino();            // en orden origen → destino (vacía si no hay)
    ListaEnlazada<Integer> getCostosAcumulados(); // costo acumulado al llegar a cada centro del camino
    int getCostoTotal();                          // -1 si no hay camino
    int getCentrosProcesados();                   // cuántos centros extrajo Dijkstra del heap (para comparar)
}

public class Dijkstra {
    static ResultadoCamino calcular(RedLogistica red, String origen, String destino);
}

public class ResultadoExploracion {
    enum Criterio { BFS, DFS }
    Criterio getCriterio();
    boolean isOrigenExiste();
    ListaEnlazada<Centro> getOrdenDeVisita();
    ListaEnlazada<Integer> getNiveles();          // BFS: cantidad de tramos desde el origen; DFS: profundidad
    ListaEnlazada<Centro> getNoAlcanzados();      // centros a los que no se llega desde el origen
}

public class Exploracion {
    static ResultadoExploracion bfs(RedLogistica red, String origen);
    static ResultadoExploracion dfs(RedLogistica red, String origen);
}

public class DatosIniciales {
    static void cargar(RedLogistica red);         // 7 centros + 10 rutas bidireccionales del enunciado
}
```

---

## 4. Reglas de validación (todas viven en `RedLogistica`)

| Regla | Mensaje sugerido |
|---|---|
| El id se normaliza: se le quitan espacios y se pasa a mayúsculas (`" a "` → `"A"`) | — |
| Id vacío o con `;` | "El identificador no puede estar vacío" |
| Id duplicado en el alta | "Ya existe un centro con id X" |
| Descripción vacía | "La descripción no puede estar vacía" |
| Centro inexistente (modificar, consultar, baja, rutas) | "No existe un centro con id X" |
| Tiempo negativo | "El tiempo debe ser un entero mayor o igual a 0" |
| Ruta de un centro a sí mismo | "Una ruta no puede unir un centro consigo mismo" |
| Ruta duplicada: ya existe `buscarRuta(origen, destino)`, o es bidireccional y ya existe `buscarRuta(destino, origen)` | "Ya existe una ruta entre X e Y" |
| Ruta inexistente (modificar, habilitar, deshabilitar, eliminar) | "No existe una ruta de X a Y" |
| Deshabilitar una ruta ya deshabilitada (o habilitar una activa) | Informar sin error: "La ruta ya estaba deshabilitada" |

Nota: se permiten dos rutas de un solo sentido opuestas (A→B y B→A con tiempos distintos), porque es un caso real. Lo que no se permite es duplicar el mismo sentido.

---

## 5. Fases de trabajo

Cada fase tiene tareas concretas y una **definición de terminado**. Una fase está terminada cuando su código compila, sus casos a verificar se comportan como se espera y respeta la regla de capas.

Los **casos a verificar** se prueban a mano: ejecutando el programa o con un `main` temporal. No se escriben pruebas unitarias ni se sube código de prueba al repositorio.

### Fase 0 — Preparar el proyecto

1. Crear el proyecto Maven en IntelliJ: *New Project → Maven*, JDK 25, `groupId = tpi`, `artifactId = tpi-taed2`.
2. En `pom.xml`: `maven.compiler.release = 25`, `project.build.sourceEncoding = UTF-8` y `maven-jar-plugin` con `tpi.Main` como clase principal.
3. Crear los paquetes de la sección 2.1, vacíos.
4. `Main.java` con un `main` que imprima un mensaje de bienvenida.
5. `.gitignore` con `target/`, `.idea/`, `*.iml` y `out/`.

**Terminado cuando:** `mvn package` compila sin errores y `Main` se ejecuta desde IntelliJ en las máquinas de todos.

### Fase 1 — Estructuras lineales y heap

**1.1 `ListaEnlazada<T>`** (lista simplemente enlazada con referencias a `cabeza` y `cola`)
- `agregarAlFinal`: O(1) usando la referencia a `cola`.
- `agregarOrdenado`: recorrer hasta el primer elemento mayor según el `Comparator` e insertar antes. Si hay empates, el nuevo va **después** de los iguales (inserción estable).
- `eliminar`: contemplar que el elemento sea la cabeza, la cola o el único de la lista; actualizar `cola`.
- `iterator()`: clase interna que recorre los nodos.

**1.2 `Cola<T>`**: nodos enlazados con `frente` y `fin`. `desencolar` sobre una cola vacía lanza `IllegalStateException`.

**1.3 `Pila<T>`**: nodos enlazados con `tope`.

**1.4 `MonticuloMinimo<T>`** (arreglo `Object[]` con casteo genérico)
- Para el índice `i`: padre = `(i - 1) / 2`, hijo izquierdo = `2i + 1`, hijo derecho = `2i + 2`.
- `insertar`: poner al final y **flotar** mientras sea menor que el padre.
- `extraerMinimo`: guardar la raíz, mover el último elemento a la raíz y **hundir** intercambiando con el hijo menor.
- Al llenarse, duplicar la capacidad (copiar a mano con un `for`, sin `Arrays.copyOf`).

**Casos a verificar:**
- Lista: agregar, eliminar el primero, el último y uno del medio; orden con `agregarOrdenado`; for-each sobre una lista vacía.
- Cola y pila: orden FIFO y LIFO; excepción al quitar de una vacía.
- Heap: insertar 1000 enteros aleatorios y extraer: deben salir ordenados. Probar con repetidos y con un solo elemento.

**Terminado cuando:** todos los casos anteriores se comportan como se espera.

### Fase 2 — Árbol AVL

**2.1 Nodo:** `clave`, `valor`, `izquierdo`, `derecho`, `altura` (int).

**2.2 Auxiliares:**
- `altura(nodo)`: 0 si el nodo es `null`.
- `factorBalance(nodo) = altura(izq) - altura(der)`.
- `actualizarAltura(nodo) = 1 + max(altura(izq), altura(der))`.

**2.3 Rotaciones** (cada una actualiza alturas, primero la del nodo que baja y luego la del que sube):

| Caso | Condición | Solución |
|---|---|---|
| LL | fb(nodo) = +2 y fb(izq) >= 0 | rotación simple a la derecha |
| RR | fb(nodo) = -2 y fb(der) <= 0 | rotación simple a la izquierda |
| LR | fb(nodo) = +2 y fb(izq) < 0 | rotar a la izquierda el hijo izquierdo y luego a la derecha el nodo |
| RL | fb(nodo) = -2 y fb(der) > 0 | rotar a la derecha el hijo derecho y luego a la izquierda el nodo |

**2.4 `insertar`** (recursivo):
```
insertar(nodo, clave, valor):
    si nodo es null: crear nodo; tamanio++; insertado = true; devolver nodo
    si clave < nodo.clave: nodo.izq = insertar(nodo.izq, ...)
    si clave > nodo.clave: nodo.der = insertar(nodo.der, ...)
    si clave == nodo.clave: insertado = false; devolver nodo   ← rechazo de duplicado
    actualizarAltura(nodo)
    devolver balancear(nodo)
```

**2.5 `eliminar`** (recursivo):
- Hoja: devolver `null`.
- Un solo hijo: devolver ese hijo.
- Dos hijos: buscar el **sucesor inorden** (el mínimo del subárbol derecho), copiar su clave y valor al nodo, y eliminar el sucesor del subárbol derecho.
- Al volver de la recursión: actualizar la altura y balancear en cada nivel.

**2.6 `valoresEnOrden`:** recorrido inorden (izquierdo, nodo, derecho) que va agregando a una `ListaEnlazada`.

**2.7 `dibujar`:** dibujo de costado o con indentación, por ejemplo:
```
        G
    F
        E
D
        C
    B
        A
```

**2.8 `esValido`:** verifica recursivamente que se cumpla el orden de ABB, que `|fb| <= 1` en cada nodo y que la altura guardada sea correcta.

**Casos a verificar:**
- Insertar A..G en orden: altura 3, raíz D, `esValido()` verdadero.
- Insertar duplicado: devuelve `false` y el tamaño no cambia.
- Forzar cada rotación: LL con 3, 2, 1; RR con 1, 2, 3; LR con 3, 1, 2; RL con 1, 3, 2.
- Eliminar una hoja, un nodo con un hijo, un nodo con dos hijos y la raíz; después de cada baja `esValido()` debe seguir verdadero.
- Insertar 10.000 claves ordenadas: la altura debe ser como mucho 1,44·log2(n), unos 19. Comparar en el informe con un ABB sin balancear (altura 10.000).

**Terminado cuando:** todos los casos anteriores se comportan como se espera y `esValido()` da verdadero después de cada operación.

### Fase 3 — Modelo

1. `Centro`: id inmutable, descripción modificable y lista de adyacencia vacía al crearse.
2. `Ruta`: implementar `permiteSalirDesde`, `otroExtremo` y `conecta` según las firmas de la sección 3.2.
3. `toString()` en ambas clases, para depurar (por ejemplo, `A - Depósito central`, `A <-> B (8 min)`, `D -> F (6 min) [DESHABILITADA]`).

**Casos a verificar:** con una ruta bidireccional, se puede salir desde los dos extremos; con una dirigida, solo desde el origen; con una ruta inactiva, `permiteSalirDesde` devuelve `false`.

### Fase 4 — CRUD de centros en `RedLogistica` (RF1)

1. Atributo `ArbolAVL<String, Centro> centros`.
2. `normalizarId`: `trim().toUpperCase()`, validando que no quede vacío ni contenga `;`.
3. `altaCentro`: validar, crear el `Centro` y `centros.insertar(id, centro)`. Si devuelve `false`, lanzar excepción por duplicado.
4. `modificarCentro`: buscar en el AVL y cambiar la descripción. **El id no se modifica**: es la clave del árbol, y cambiarlo rompería el orden. Para "cambiar el id" hay que dar de baja y volver a dar de alta.
5. `consultarCentro` y `existeCentro`: `centros.buscar(id)`.
6. `listarCentros`: `centros.valoresEnOrden()`.
7. `bajaCentro`:
   - recorrer **todos** los centros y quitar de cada lista de adyacencia las rutas que tengan al centro como origen o destino (así se limpian también las rutas dirigidas que llegan a él);
   - `centros.eliminar(id)`.
   - Complejidad O(V + E), queda documentada.

**Casos a verificar:** alta normal; id duplicado (también con `"a"` contra `"A"`); modificar uno existente y uno inexistente; consultar uno inexistente; listar después de dar de alta G, A, D (debe salir A, D, G); baja de un centro con rutas (los vecinos ya no deben tener rutas hacia él).

### Fase 5 — Rutas en `RedLogistica` (RF2)

1. `altaRuta(origen, destino, tiempo, bidireccional)`:
   - validar que existan ambos centros, que el tiempo sea >= 0, que origen y destino sean distintos y que no haya duplicado (sección 4);
   - crear una sola `Ruta`;
   - agregarla ordenada a `origen.getRutas()`, con un comparador por el id de `ruta.otroExtremo(origen)`;
   - si es bidireccional, agregar **el mismo objeto** ordenado a `destino.getRutas()`.
2. `buscarRuta(o, d)`: recorrer `o.getRutas()` buscando una ruta con `conecta(o, d)`.
3. `modificarTiempoRuta`: buscar y hacer `setTiempo`, validando que sea >= 0.
4. `deshabilitarRuta` / `habilitarRuta`: buscar y hacer `setActiva(false/true)`.
5. `eliminarRuta`: quitar la ruta de la lista del origen y, si es bidireccional, también de la del destino.
6. `aislarCentro(id)`: deshabilitar todas las rutas de su lista. Además, recorrer los demás centros para deshabilitar las rutas dirigidas que **llegan** a él. Devuelve la cantidad de rutas deshabilitadas: solo se cuentan las que pasan de activa a inactiva, así una bidireccional, que aparece en las dos listas, no se cuenta dos veces.
7. `listarRutas`: recorrer los centros en orden y agregar cada ruta una sola vez. Criterio: agregarla solo cuando el centro que se está recorriendo es su `origen`.

**Casos a verificar:** alta bidireccional (aparece en las dos listas y es el mismo objeto); alta dirigida (solo en el origen); rechazos (tiempo negativo, mismo centro, duplicado, centro inexistente); deshabilitar y volver a habilitar; eliminar; aislar G con los datos iniciales devuelve 2 y G queda sin rutas activas.

### Fase 6 — Camino mínimo con Dijkstra (RF3 y RF4)

**6.1 Validaciones previas** (en este orden):
1. Si el origen no existe, devolver `ORIGEN_INEXISTENTE`.
2. Si el destino no existe, devolver `DESTINO_INEXISTENTE`.
3. Si son el mismo centro, devolver `MISMO_CENTRO`, con camino `[origen]` y costo 0.

**6.2 Preparación:**
- `ListaEnlazada<Centro> todos = red.listarCentros()` y a cada uno asignarle `setIndice(i)`, de 0 a n-1. Se reindexa en cada consulta, así que no importan los huecos que dejan las bajas.
- Arreglos de tamaño n: `int[] distancia` (todos en `Integer.MAX_VALUE`), `Centro[] anterior` (todos en `null`) y `boolean[] procesado`.

**6.3 Algoritmo** (variante "perezosa": no se actualizan claves dentro del heap, se insertan entradas nuevas y se descartan las viejas):
```
distancia[origen] = 0
heap.insertar((origen, 0))                     // entrada = (centro, distancia)
mientras heap no vacío:
    (u, d) = heap.extraerMinimo()
    si procesado[u]: continuar                 // entrada vieja, se descarta
    procesado[u] = true; centrosProcesados++
    si u == destino: cortar                    // ya es definitiva
    para cada ruta en u.getRutas():
        si !ruta.permiteSalirDesde(u): continuar   // inactiva o en contramano
        v = ruta.otroExtremo(u)
        nueva = distancia[u] + ruta.getTiempo()
        si nueva < distancia[v]:               // estrictamente menor → desempate estable
            distancia[v] = nueva
            anterior[v] = u
            heap.insertar((v, nueva))
si distancia[destino] == MAX: devolver SIN_CAMINO
```
- Comparador del heap: primero por distancia y, si empatan, por id del centro (para que sea reproducible).
- Como se compara con `<` estricto, ante dos caminos de igual costo se queda el primero que se encontró.

**6.4 Reconstrucción del camino:** desde `destino`, seguir `anterior` apilando en una `Pila` hasta llegar a `null`. Al desapilar quedan en orden origen → destino, y para cada centro se agrega `distancia[centro]` a `costosAcumulados`.

**6.5 Complejidad:** O((V + E) log E), que es O((V + E) log V). Memoria O(V + E). Se documenta en el informe.

**6.6 Por qué Dijkstra y no BFS o DFS** (para el informe y la defensa):
- Dijkstra exige **pesos no negativos**, y el enunciado lo garantiza.
- BFS minimiza **cantidad de tramos**, no el tiempo. Contraejemplo con los datos del caso, de A a D:
  - BFS llega a D por A → B → D: 2 tramos, **17 min**.
  - Dijkstra encuentra A → C → D: 2 tramos, **12 min**.

**Casos a verificar:** los de la Fase 9, más un grafo con una ruta dirigida que obligue a dar la vuelta, y uno con una ruta deshabilitada que se ignore.

### Fase 7 — Exploración BFS y DFS (RF5)

**7.1 BFS** (criterio: **por anchura**, nivel por nivel; los vecinos se visitan en orden alfabético de id):
```
si origen no existe: resultado con origenExiste = false
reindexar; visitado[] = false
cola.encolar(origen); visitado[origen] = true; nivel[origen] = 0
mientras cola no vacía:
    u = cola.desencolar(); agregar u a ordenDeVisita (con su nivel)
    para cada ruta en u.getRutas() (ya están ordenadas por vecino):
        si ruta.permiteSalirDesde(u):
            v = ruta.otroExtremo(u)
            si !visitado[v]: visitado[v] = true; nivel[v] = nivel[u] + 1; cola.encolar(v)
noAlcanzados = centros con visitado == false
```

**7.2 DFS** (criterio: **en profundidad**, se avanza lo más posible por el primer vecino alfabético antes de retroceder). Es recursivo:
```
dfs(u, profundidad):
    visitado[u] = true; agregar u a ordenDeVisita (con su profundidad)
    para cada ruta en u.getRutas():
        si ruta.permiteSalirDesde(u):
            v = ruta.otroExtremo(u)
            si !visitado[v]: dfs(v, profundidad + 1)
```
- Nota para el informe: con miles de centros en línea, la recursión podría desbordar la pila de llamadas. La alternativa es la versión iterativa con la `Pila` propia.

**7.3 Complejidad:** O(V + E) los dos.

**7.4 Diferencia con el camino mínimo:** hay que mostrarla en la salida. La pantalla de exploración termina con la aclaración: *"Este recorrido muestra qué centros son alcanzables y en qué orden se descubren; no calcula tiempos mínimos. Para eso usar la opción Camino mínimo (Dijkstra)."*

**Resultados esperados con los datos iniciales, desde A:**
- BFS: A(0), B(1), C(1), D(2), E(2), F(3), G(3).
- DFS: A, B, C, D, F, E, G.
  - A va a B; B, a C (su primer vecino no visitado: A ya está, sigue C).
  - C va a D (A, B visitados; D antes que E); D, a F (B, C visitados); F, a E (D visitado; E antes que G).
  - E va a G (C, F visitados). Después de G se retrocede y no queda nada sin visitar.

### Fase 8 — Interfaz de consola

**8.1 `Entrada`** (envuelve un único `Scanner(System.in)`):
- `leerTexto(mensaje)`: no acepta vacío, vuelve a pedir.
- `leerEntero(mensaje, min, max)`: si no es un número o está fuera de rango, informa y vuelve a pedir (captura `NumberFormatException`).
- `leerSiNo(mensaje)`: acepta s/n.
- `pausa()`: "Presione Enter para continuar".

**8.2 `MenuCaso1`**: un ciclo `do/while` con `switch`. Cada opción llama a `RedLogistica`, `Dijkstra` o `Exploracion` dentro de un `try/catch (OperacionInvalidaException e)` que imprime `e.getMessage()`. **El programa nunca debe terminar por una excepción.**

```
===== RED LOGÍSTICA — CASO 1 =====
 1. Gestión de centros
 2. Gestión de rutas
 3. Camino mínimo entre dos centros (Dijkstra)
 4. Explorar la red desde un centro (BFS / DFS)
 5. Mostrar la red
 6. Restaurar datos iniciales del enunciado
 7. Ejecutar pruebas obligatorias
 0. Salir

--- 1. Gestión de centros ---        --- 2. Gestión de rutas ---
 1. Alta de centro                    1. Alta de ruta (bidireccional / un sentido)
 2. Modificar descripción             2. Modificar tiempo
 3. Consultar centro por id           3. Deshabilitar ruta (baja temporal)
 4. Listar centros (ordenados)        4. Habilitar ruta
 5. Baja de centro                    5. Eliminar ruta
 6. Ver árbol AVL del índice          6. Aislar centro
 0. Volver                            7. Listar rutas
                                      0. Volver
```

- Al iniciar el programa se cargan los datos iniciales automáticamente. La opción 6 descarta los cambios y vuelve a cargarlos en una red nueva.
- "Consultar centro" muestra id, descripción y sus rutas (activas e inactivas) con sus tiempos.

**8.3 `Vista`** (métodos estáticos que devuelven `String`; el menú se encarga de imprimirlos):

- **Camino mínimo:**
  ```
  Camino mínimo de A a F (Dijkstra)
    A  (0 min)
    └─ 5 min → C  (5 min)
    └─ 7 min → D  (12 min)
    └─ 6 min → F  (18 min)
  Tiempo total: 18 minutos — 3 tramos — centros procesados: 7
  ```
  - y los mensajes de cada `Estado`: "No existe un recorrido de A a G", "El centro Z no está registrado", "El origen y el destino coinciden (A): tiempo 0".
- **Exploración:** criterio usado, orden de visita con nivel o profundidad, lista de no alcanzados y la aclaración de 7.4.
- **Mostrar la red:**
  - **lista de adyacencia:**
    ```
    A: → B (8)  → C (5)
    ...
    G: → E (8) [X]  → F (2) [X]   ← [X] = deshabilitada
    ```
  - **matriz de tiempos** (`.` si no hay ruta directa, `X` si está deshabilitada):
    ```
         A   B   C   D   E   F   G
    A    .   8   5   .   .   .   .
    B    8   .   3   9   .   .   .
    ...
    ```
  - resumen: cantidad de centros, rutas, rutas deshabilitadas y altura del AVL.

**Terminado cuando:** se pueden ejecutar todos los requerimientos desde el menú, se ingresan datos inválidos (letras donde va un número, ids vacíos) y el programa sigue funcionando.

### Fase 9 — Datos iniciales y pruebas obligatorias

**9.1 `DatosIniciales.cargar(red)`:** 7 llamadas a `altaCentro` y 10 a `altaRuta(..., true)` con los datos del enunciado. **No se cargan resultados**: solo datos de entrada.

**9.2 Opción 7 del menú:** crea una `RedLogistica` **nueva** (para no tocar la que está usando el usuario), le carga los datos iniciales y ejecuta en orden, mostrando la salida real de `Dijkstra`:
1. A → F
2. A → G
3. B → E
4. A → A
5. A → Z (centro inexistente)
6. `aislarCentro("G")` e informar cuántas rutas se deshabilitaron
7. A → G (después de aislar)

**9.3 Resultados esperados.** La salida de la opción 7 tiene que coincidir con estos valores, **calculados a mano** a partir del enunciado. Si alguno no coincide, hay un error en la implementación:

| Consulta | Estado esperado | Camino esperado | Tiempo |
|---|---|---|---|
| A → F | ENCONTRADO | A → C → D → F | 18 |
| A → G | ENCONTRADO | A → C → E → G | 17 |
| B → E | ENCONTRADO | B → C → E | 7 |
| A → A | MISMO_CENTRO | A | 0 |
| A → Z | DESTINO_INEXISTENTE | — | — |
| Z → A | ORIGEN_INEXISTENTE | — | — |
| aislar G y luego A → G | SIN_CAMINO | — | — |
| aislar G y luego A → F | ENCONTRADO (no cambia) | A → C → D → F | 18 |

Detalle para el informe: A → B tiene dos caminos de 8 min (directo, y A → C → B con 5 + 3). Con el desempate estricto se queda con el directo, A → B.

**9.4 Registro para el informe:** copiar la salida real de la opción 7 (o hacer una captura) y pegarla en la sección de resultados del informe, junto con las decisiones tomadas.

### Fase 10 — Cierre

1. Actualizar `README.md` con: requisitos (JDK 25), cómo compilar y ejecutar (`mvn package` y `java -jar target/tpi-taed2-1.0-SNAPSHOT.jar`, o ejecutar `tpi.Main` desde IntelliJ), datos de prueba y **limitaciones conocidas**:
   - solo en memoria;
   - DFS recursivo;
   - la baja de un centro es O(V + E);
   - el id no se modifica.
2. Revisar que ninguna clase de `estructuras`, `modelo` o `logica` importe colecciones de `java.util` (excepto las interfaces permitidas).
3. Repasar la sección "Material para el informe".

---

## 6. Dependencias entre fases y reparto sugerido (3 integrantes)

```
Fase 0 ─┬─▶ Fase 1 (Lista, Cola, Pila, Heap) ──┬──────────────────────────┐
        ├─▶ Fase 2 (AVL) ──────────────────────┤                          │
        └─▶ Fase 8.1 (Entrada) + borrador de   │                          │
            menús con opciones vacías          ▼                          ▼
                                    Fase 3 (Modelo) ─▶ Fase 4 ─▶ Fase 5 ─┬─▶ Fase 6 (Dijkstra)
                                                                         └─▶ Fase 7 (BFS/DFS)
                                                                                  │
                                                  Fase 8.2/8.3 (conectar menús) ◀─┘
                                                                                  │
                                                                  Fase 9 ─▶ Fase 10
```

| Etapa | Integrante 1 | Integrante 2 | Integrante 3 |
|---|---|---|---|
| A (en paralelo) | Fase 1: Lista, Cola, Pila, Heap | Fase 2: AVL | Fase 0 + Fase 8.1 + menús con opciones vacías |
| B | Fase 3: Modelo | Fase 4: CRUD de centros | `Vista`: dibujo de la red y matriz |
| C | Fase 6: Dijkstra | Fase 5: Rutas | Fase 7: BFS / DFS |
| D (todos) | Fase 9: pruebas obligatorias | Conectar los menús (8.2) | Fase 10: README + material del informe |

Como las firmas de la sección 3 están fijadas, cada uno puede programar contra la API de otro aunque esa parte todavía no esté terminada.

---

## 7. Forma de trabajo con Git

- Una rama por tarea: `feature/avl`, `feature/heap`, `feature/dijkstra`, etc. Nadie trabaja directo sobre `main`.
- Integrar a `main` con un Pull Request que **otro integrante** revise. El PR debe compilar (`mvn package`) y, cuando el menú ya exista, la opción 7 debe dar los resultados esperados de la Fase 9.
- Mensajes de commit según `AGENTS.md`: en español, con mayúscula inicial, primera persona del singular, sin emojis y sin co-autores de IA. Ejemplos:
  - `Agrego rotaciones simples y dobles al AVL`
  - `Corrijo la baja de un nodo con dos hijos`
- Antes de empezar una tarea, hacer `git pull` de `main` para partir de lo último.

---

## 8. Material para el informe que va surgiendo de cada fase

| Pregunta de diseño del enunciado | Respuesta que sale de este plan |
|---|---|
| ¿Cómo representar centros y conexiones? | Grafo ponderado con listas de adyacencia; diagrama de la sección 2.3. Comparar con una matriz de adyacencia: O(V²) de memoria, desperdiciada en una red poco densa |
| ¿Qué estructura para el índice de centros? | AVL frente a ABB común (degenera en lista con ids ordenados: A..G da altura 7, contra 3 del AVL), frente a lista o arreglo (O(n)) y frente a tabla hash (no está en la materia, no mantiene el orden y su peor caso es O(n)) |
| ¿BFS o DFS garantizan el menor tiempo? | No. Contraejemplo A → D de la sección 6.6 |
| ¿Qué algoritmo y con qué condición sobre los pesos? | Dijkstra; pesos no negativos. Con pesos negativos haría falta Bellman-Ford (mencionarlo como límite) |
| ¿Qué cambia con rutas dirigidas o miles de centros? | Dirigidas: ya está soportado con el atributo `bidireccional`. Miles de centros: AVL O(log n), Dijkstra con heap O((V+E) log V), lista de adyacencia en lugar de matriz y DFS iterativo |

Para cada fase, guardar: el pseudocódigo (ya está en este documento), la complejidad y las salidas reales de las pruebas.
