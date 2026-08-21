# CHANGELOG

## Unreleased

### 2026-08-21 — PH01-T01
**Added**
- Inicialización de Laravel API en `nutrition-backend/` con Sanctum, Pest y Pint.
- Endpoint de verificación de salud `GET /api/v1/health`.
- Test de características `tests/Feature/HealthTest.php`.

**Changed**
- Configuración de prefijo `/api/v1` en `bootstrap/app.php`.
- Base de datos configurada a MySQL (`bsnutrition`).

**Tests**
- `php ./vendor/bin/pest` (3 passed, 10 assertions).
- `php artisan migrate:fresh` (ejecución limpia de migraciones).

### 2026-08-21 — PH00 (Bootstrap)
**Added**
- Estructura base del Monorepo `BSNutrition` (`NutritionApp` y `nutrition-backend`).
- Guías de gobernanza, branching (`BRANCHING.md`), control de entorno (`ENVIRONMENT.md`) y workflows de CI.
- `.gitignore` y `README.md` unificados.


## Entry format

### YYYY-MM-DD — TASK-ID

**Added**
- ...

**Changed**
- ...

**Fixed**
- ...

**Tests**
- ...
