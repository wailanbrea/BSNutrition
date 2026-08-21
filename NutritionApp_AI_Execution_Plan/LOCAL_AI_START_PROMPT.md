# Prompt — Start/Continue Local AI Session

Eres el agente senior de implementación de este proyecto.

No debes replantear todo el sistema. Debes continuar exactamente desde el estado registrado.

## Procedimiento obligatorio

1. Lee `README_FIRST.md`.
2. Lee `PROJECT_STATUS.md`.
3. Lee `00_AI_OPERATING_PROTOCOL.md`.
4. Abre la fase actual indicada en `PROJECT_STATUS.md`.
5. Lee únicamente los documentos indicados por esa fase.
6. Inspecciona únicamente el código directamente relacionado con la tarea actual.
7. Ejecuta la primera tarea pendiente no bloqueada.
8. Ejecuta sus pruebas/quality gates.
9. Solo marca `[x]` si cumple criterios de aceptación.
10. Actualiza checklist de fase, `PROJECT_STATUS.md` y `CHANGELOG.md`.
11. Si tomaste una decisión de largo plazo, actualiza `DECISIONS.md`.
12. Deja una sola acción siguiente exacta en `PROJECT_STATUS.md`.

## Restricciones

- No cargues todo el repositorio si no es imprescindible.
- No hagas refactors fuera de alcance.
- No cambies arquitectura silenciosamente.
- No introduzcas secretos en Android.
- Android = Kotlin + Compose.
- Backend = Laravel + MySQL.
- Diario = offline-first.
- IA = detección/estimación; nutrición autoritativa desde Nutrition Engine.
- Prefiere dependencias estables y mantenibles.

Antes de tocar código informa brevemente:
- fase actual;
- tarea actual;
- archivos que vas a inspeccionar;
- pruebas que determinarán que está completa.

Después ejecuta la tarea.
