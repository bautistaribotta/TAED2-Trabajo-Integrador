# Pendientes del trabajo

Estado al 16/11/2026. El código de los dos casos está completo y subido: el Caso 1 (Fases 0 a 10 de `docs/plan-caso-1.md`) y el Caso 2 (Fases 0 a 9 de `docs/plan-caso-2.md`). La opción 7 de cada caso pasa todas sus pruebas obligatorias. El informe en PDF y el ZIP de entrega ya están armados. Lo que queda es esto.

Cuando se termine un punto, marcarlo con `[x]` o borrarlo de esta lista.

## Entregables del enunciado

- [x] **Informe PDF.** El contenido está en `docs/Trabajo Practico Integrador.docx`:
  - propuesta sin código;
  - preguntas de diseño;
  - estructuras;
  - algoritmos;
  - complejidad;
  - resultados de las pruebas;
  - pseudocódigo de los dos casos.

  Ya están hechos:
  - [x] la portada (título y materia), el índice y la numeración de páginas;
  - [x] las pruebas adicionales de cada caso (opción 8 del menú y una tabla por caso en el Word);
  - [x] la columna de tiempo en los resultados del Caso 2;
  - [x] las extensiones opcionales implementadas, nombradas en cada caso;
  - [x] la sección final con la consigna de cierre (la agregó Ramiro);
  - [x] el PDF exportado y el ZIP de entrega armado (ver "ZIP de entrega" más abajo).
- [ ] **Demostración de ambos casos.** Va a ser un video, que graba Bautista. No hace falta código nuevo. El enunciado pide al menos un caso límite por sistema.
  - **Caso 1:**
    - mostrar la red (opción 5) y el árbol AVL (opción 1 → 6);
    - calcular el camino mínimo A → F;
    - explorar con BFS y con DFS;
    - mostrar los casos límite: A → A, un centro inexistente, y aislar G y buscar A → G;
    - correr la opción 7 (y, si hay tiempo, la 8).
  - **Caso 2:**
    - generar los archivos de prueba (opción 6);
    - correr las pruebas obligatorias (opción 7);
    - analizar T4 con la opción 4 (frecuencias, códigos y árbol);
    - como caso límite, mostrar el rechazo de `pruebas-caso2/T6-truncado.huf` con la opción 2.

## ZIP de entrega

> **Recordatorio:** si cambia el Word o el código, hay que volver a exportar el PDF y regenerar el ZIP. El ZIP se arma desde el último commit, así que antes hay que commitear los cambios.

El ZIP queda en `entrega/TPI-TAED2-Contreras-Ribotta.zip`. La carpeta `entrega/` está en `.gitignore`, así que no se sube. Lleva el código, `pom.xml`, el diagrama, el README sin la copia del enunciado ni los enlaces a los planes, y el informe en PDF. No lleva `AGENTS.md`, los planes, este archivo, el `.docx`, el script ni el historial de git.

Pasos, desde la raíz del repositorio y con LibreOffice y Python instalados:

1. Exportar el Word a PDF en una carpeta aparte:
   ```bash
   soffice --headless --convert-to pdf --outdir /tmp/pdf "docs/Trabajo Practico Integrador.docx"
   ```
2. Armar la carpeta y el ZIP:
   ```bash
   python docs/armar_entrega.py . entrega "/tmp/pdf/Trabajo Practico Integrador.pdf"
   ```
3. Comprobar que compila: `mvn -q package` dentro de `entrega/TPI-TAED2-Contreras-Ribotta`, y después borrar su carpeta `target/`.

Si se cambia el comienzo o el final del README, puede hacer falta ajustar el recorte que hace el script: falla con un `assert` si no encuentra el texto esperado.

## Pruebas manuales que todavía no se hicieron

- [ ] Escribir acentos desde una consola real en el Caso 1, por ejemplo dar de alta un centro con descripción "Depósito".
- [ ] Ejecutar `tpi.Main` desde IntelliJ IDEA, si se va a usar en la demostración.

## Opcional

- [ ] Extensiones del enunciado que no se hicieron:
  - comparar tiempos entre algoritmos de consulta (Caso 1);
  - procesar archivos por bloques (Caso 2). El estudio del efecto de la distribución de frecuencias sí está hecho.
- [ ] Revisar el contenido de T5. Hoy se reduce un 24,76 %, porque mezcla partes repetitivas con partes que cambian en cada registro. Si se quiere mostrar una reducción mayor en la demostración, se puede ajustar `ArchivosDePrueba.t5Binario()`. En ese caso hay que actualizar el resultado de T5 en el README y en el Word.

## Notas para quien siga

- Leer `AGENTS.md` antes de tocar nada: idioma, formato de commits y las reglas sobre estructuras propias y pruebas.
- El Word se generó agregando secciones con un script, sin Word instalado. El índice usa campos de Word: si cambian las páginas, se actualiza con clic derecho sobre el índice → Actualizar campos. Si se edita a mano, conviene revisar que las tablas y los bloques en Consolas sigan viéndose bien.
- Los resultados que aparecen en el README y en el Word salieron de ejecutar el programa. Si cambia el código, hay que volver a correr las opciones 7 y 8 de cada caso y actualizarlos.
