# AI Operating Protocol

## 1. Presupuesto de contexto para ~131k

Objetivo recomendado:

- máximo aproximado de 85k–90k tokens de contexto cargado;
- reservar ~25k para razonamiento, resultados de herramientas y cambios;
- mantener margen libre para depuración.

Si la tarea requiere mucho código:

1. inspeccionar primero árbol de directorios;
2. leer interfaces/modelos públicos;
3. abrir solo implementaciones relevantes;
4. evitar archivos generados/build/vendor;
5. resumir hallazgos duraderos en `PROJECT_STATUS.md`;
6. continuar con el mínimo contexto necesario.

## 2. Inicio obligatorio de sesión

Leer:

1. `README_FIRST.md`
2. `PROJECT_STATUS.md`
3. este archivo
4. fase actual
5. documentos que la fase referencia
6. código directamente afectado

Después identificar exactamente **una** tarea para ejecutar.

## 3. Estados de checklist

- `[ ]` pendiente
- `[-]` en progreso
- `[x]` completada
- `[!]` bloqueada
- `[~]` diferida deliberadamente

Normalmente solo debe existir una tarea `[-]`.

## 4. Regla de una tarea

No saltar a otra fase mientras exista una tarea actual ejecutable.

Si se bloquea:

- marcar `[!]`;
- escribir bloqueador exacto en `PROJECT_STATUS.md`;
- indicar qué acción manual hace falta;
- solo continuar con otra tarea si es independiente.

## 5. Antes de editar

- revisar implementación existente;
- confirmar que la función no existe;
- identificar módulos/archivos afectados;
- identificar pruebas necesarias;
- verificar contratos API/BD;
- evitar refactors fuera de alcance.

## 6. Después de editar

### Android
Ejecutar según corresponda:
- build;
- unit tests;
- lint;
- Room migration tests;
- Compose UI tests.

### Backend
Ejecutar según corresponda:
- formatter/Pint;
- Pest/PHPUnit;
- migration tests;
- API feature tests;
- queue/job tests.

No marcar `[x]` si fallan pruebas relevantes.

## 7. Jerarquía de verdad

1. última instrucción explícita del propietario;
2. `DECISIONS.md`;
3. `README_FIRST.md`;
4. `PROJECT_STATUS.md`;
5. fase actual;
6. documentos técnicos;
7. implementación existente;
8. comentarios/notas antiguas.

Un conflicto importante se documenta; no se resuelve silenciosamente.

## 8. Política de dependencias

Antes de agregar una librería:

- comprobar que Android/Laravel no lo resuelve nativamente;
- comprobar que ya no existe una dependencia equivalente;
- preferir estable/mantenida;
- centralizar versiones Android en `gradle/libs.versions.toml`;
- registrar decisiones que cambien arquitectura.

## 9. Prohibiciones

No:

- colocar secretos de servidor en APK;
- commitear `.env` productivo;
- hardcodear tokens;
- cambiar arquitectura sin ADR;
- introducir microservicios/Kafka/Kubernetes sin necesidad medida;
- usar IA como fuente autoritativa de calorías si existe dato nutricional;
- borrar datos/schemas manualmente sin migración;
- reescribir módulos no relacionados.

## 10. Regla offline

Flujo normal de escritura del diario:

`UI -> Room -> SyncQueue -> WorkManager -> API -> reconciliación`

La UI no debe depender de internet para registrar comida, agua o peso.

## 11. Regla IA

La IA detecta:

- alimento;
- preparación;
- estimación de cantidad;
- confianza.

Luego:

`AI detection -> Food Matching -> Nutrition Engine -> User confirmation -> Diary`

## 12. Fin de sesión

Antes de terminar:

- dejar el repo en estado compilable cuando sea posible;
- actualizar checklist de fase;
- actualizar `PROJECT_STATUS.md`;
- actualizar `CHANGELOG.md`;
- actualizar `DECISIONS.md` si aplica;
- escribir una sola acción siguiente concreta;
- no dejar tarea `[-]` sin nota de checkpoint.
