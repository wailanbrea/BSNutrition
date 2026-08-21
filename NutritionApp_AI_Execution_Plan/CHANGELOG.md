# CHANGELOG

## Unreleased

### 2026-08-21 — PH01-T03
**Added**
- `app/Http/Controllers/Api/V1/AuthController.php` para flujos de registro, login, logout, me y eliminación de cuenta.
- `app/Http/Requests/Auth/RegisterRequest.php` y `LoginRequest.php` con validación y confirmación de contraseñas.
- `app/Http/Resources/UserResource.php` para serialización de datos de usuario.
- Rutas protegidas y públicas en `routes/api.php` bajo prefijo `/api/v1`.
- Suite de pruebas de autenticación en `tests/Feature/AuthTest.php`.

**Changed**
- Configuración de tokens Sanctum por dispositivo y revocación en logout.

**Tests**
- `php ./vendor/bin/pest` (17 passed, 91 assertions).
- `php ./vendor/bin/pint` (formato limpio verificado).

### 2026-08-21 — PH01-T02
**Added**
- `app/Http/Responses/ApiErrorResponse.php` para estandarización de payloads de error JSON `{ error: { code, message, fields } }`.
- `app/Exceptions/ApiExceptionHandler.php` para captura global y mapeo de excepciones API (401, 403, 404, 405, 422, 429, 500).
- `app/Http/Middleware/ForceJsonResponse.php` para asegurar cabeceras JSON en peticiones `/api/*`.
- Suite de pruebas de contrato de errores en `tests/Feature/ApiErrorContractTest.php`.

**Changed**
- Configuración de excepciones y middleware en `bootstrap/app.php`.

**Tests**
- `php ./vendor/bin/pest` (9 passed, 42 assertions).
- `php ./vendor/bin/pint` (formato limpio verificado).

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
