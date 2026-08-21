# PROJECT STATUS

## Current state
- Project status: IN PROGRESS
- Current phase: Phase 02 — Android Foundation and Authentication
- Current task: PH02-T05
- Current task status: `[ ]`
- Last completed task: PH02-T04
- Last update: 2026-08-21

## Exact next action
Abrir `phases/PHASE_02_ANDROID_FOUNDATION_AND_AUTHENTICATION.md` y ejecutar `PH02-T05` (Navigation shell: Single Activity, Jetpack Compose Navigation, Auth & App graph routing, bottom navigation bar con top-level placeholders para Hoy, Diario, Add, Progreso, Más).

## Active blockers
None.

## Decisions pending
- nombre final de producto (actual: `BSNutrition`);
- Android applicationId (actual: `com.bsnutrition.app`);
- dominio/API hostname (desarrollo: `http://10.0.2.2:8000/api/v1` en emulador / `http://localhost:8000/api/v1`);
- ULID vs UUID;
- fórmula exacta de goals;
- branding/design tokens;
- detalles finales de deployment.

No todos estos puntos bloquean el bootstrap.

## Recently completed
- PH02-T04 — Auth UI y Repositorio en Android (`com.bsnutrition.app.core.data` y `com.bsnutrition.app.feature.auth`): `AuthRepository`, `AuthRepositoryImpl`, `DataModule`, `AuthViewModel`, `LoginScreen`, `RegisterScreen`, persistencia en DataStore y tests unitarios en `AuthRepositoryTest` y `AuthViewModelTest`.
- PH02-T03 — Network Layer en Android (`NutritionApp/app/src/main/java/com/bsnutrition/app/core/network/`): Interfaces Retrofit (`AuthApiService`, `ProfileApiService`, `HealthApiService`), DTOs de autenticación y perfil, helper `safeApiCall` con captura tipada de `ApiException` y tests unitarios en `SafeApiCallTest`.
- PH02-T02 — Arquitectura Core en Android (`NutritionApp/app/src/main/java/com/bsnutrition/app/core/`): Módulos Hilt (`DispatchersModule`, `DataStoreModule`, `NetworkModule`, `DatabaseModule`), modelos (`User`, `UserProfile`, `AuthToken`, `ApiError`, `Result`), persistencia DataStore (`AuthTokenDataSourceImpl`), Room Database base (`NutritionDatabase`, `UserEntity`, `UserDao`) y Design System components (`BsnButton`, `BsnTextField`, `BsnCard`, `BsnLoadingIndicator`).
- PH02-T01 — Proyecto Android base en `NutritionApp/`: Gradle Version Catalog (`libs.versions.toml`), Jetpack Compose, Material 3, tema `BSNutritionTheme`, `MainActivity`, `NutritionApplication` con Hilt y build types debug/release.
- **Fase 01 (Backend Foundation and Authentication) completada al 100%**.
- PH01-T04 — Profile API: Migración `user_profiles`, modelo `UserProfile`, relación en `User`, FormRequest `UpdateProfileRequest`, serializador `UserProfileResource`, endpoints `GET/PUT /api/v1/profile` y tests de aislamiento de propiedad.
- PH01-T03 — Autenticación Sanctum: Registro, Login con token por dispositivo, Logout con revocación, Endpoint Me y Delete Me, y tests de integración.
- PH01-T02 — Contrato estándar de errores JSON (`VALIDATION_ERROR`, `UNAUTHENTICATED`, `NOT_FOUND`, `FORBIDDEN`, `METHOD_NOT_ALLOWED`, `RATE_LIMITED`, `SERVER_ERROR`), middleware `ForceJsonResponse` y tests de error.
- PH01-T01 — Inicialización de Laravel en `nutrition-backend/`, configuración de MySQL (`bsnutrition`), endpoint `/api/v1/health`, Pest y Pint.
- PH00 — Repositorios locales, gobernanza, convenciones de branching, CI skeleton y estrategias de entorno.

## Files/modules changed in last task
- `NutritionApp/`: app/src/main/java/com/bsnutrition/app/core/data/repository/*, app/src/main/java/com/bsnutrition/app/core/data/di/*, app/src/main/java/com/bsnutrition/app/feature/auth/*, app/src/test/java/com/bsnutrition/app/core/data/AuthRepositoryTest.kt, app/src/test/java/com/bsnutrition/app/feature/auth/AuthViewModelTest.kt
- `NutritionApp_AI_Execution_Plan/`: PROJECT_STATUS.md, CHANGELOG.md, phases/PHASE_02_ANDROID_FOUNDATION_AND_AUTHENTICATION.md

## Tests from last task
- `AuthRepositoryTest` unit tests (login, logout, token persistence & Room insert)
- `AuthViewModelTest` unit tests (field validation, mismatched passwords, state transitions)

## Known issues
None.

## Manual owner actions required
None.

---

## Update template

Mantener siempre estas secciones:
- Current state
- Exact next action
- Active blockers
- Recently completed
- Files/modules changed in last task
- Tests from last task
- Known issues
- Manual owner actions required
