# Branching & Commit Conventions

Aplica a ambos repositorios: `NutritionApp` (Android) y `nutrition-backend`.

## Branches

| Branch | Uso |
|---|---|
| `main` | Rama protegida. Solo recibe merges de PRs aprobados. Siempre compilable y con tests verdes. |
| `feature/<task-id>` | Trabajo de una tarea nueva (ej. `feature/PH01-T03`). |
| `fix/<task-id>` | Corrección de un defecto (ej. `fix/PH06-T02`). |
| `chore/<task-id>` | Infraestructura, docs, CI, sin cambio de comportamiento. |

- Una rama = una tarea del plan. No mezclar tareas en la misma rama.
- No escribir directamente sobre `main`.

## Commits

Formato (Conventional Commits con task ID):

```
<tipo>(<alcance>): <descripcion> [<task-id>]

[opcional: cuerpo con contexto y decisiones]
```

- `tipo`: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci`, `perf`
- `alcance`: módulo o área (`auth`, `diary`, `nutrition-engine`, `admin`, `ci`, ...)
- El **task ID va siempre al final** de la primera línea: `feat(auth): login flow [PH02-T01]`
- Mensajes en inglés, primera línea ≤ 72 caracteres.
- Un commit = una unidad de cambio verificable. Si no compila o falla un test relevante, no commitear.

## Merge gate

Un PR se fusiona a `main` solo cuando:

1. CI verde (build + tests + lint del repositorio).
2. Revisión aprobada por un agente distinto al autor.
3. `docs/PROJECT_STATUS.md` y `docs/CHANGELOG.md` actualizados en el mismo PR.
4. Sin secretos en el diff (revisar `.env`, tokens, credenciales).

Después de mergear, la rama se borra.

## Reglas duras

- Nunca `git push --force` sobre `main`.
- Nunca borrar ramas con trabajo no mergeado sin confirmación del propietario.
- `git reset --hard` / `git clean -fd` solo para limpiar trabajo propio, nunca el ajeno.
