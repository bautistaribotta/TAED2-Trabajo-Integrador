# Pendientes del trabajo

Estado al 16/11/2026. El código de los dos casos está completo y subido: el Caso 1 (Fases 0 a 10 de `docs/plan-caso-1.md`) y el Caso 2 (Fases 0 a 9 de `docs/plan-caso-2.md`). La opción 7 de cada caso pasa todas sus pruebas obligatorias. Lo que queda es esto.

Cuando se termine un punto, marcarlo con `[x]` o borrarlo de esta lista.

## Entregables del enunciado

- [ ] **Informe PDF.** Es el único entregable que no está hecho. El contenido ya está completo en `docs/Trabajo Practico Integrador.docx`:
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
  - [x] la sección final con la consigna de cierre (la agregó Ramiro).

  Falta:
  - [ ] **Exportar el Word a PDF** cuando el contenido esté cerrado. Con LibreOffice instalado:
    ```bash
    soffice --headless --convert-to pdf "docs/Trabajo Practico Integrador.docx"
    ```
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
