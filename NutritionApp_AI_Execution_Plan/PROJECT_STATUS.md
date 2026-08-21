# PROJECT STATUS

## Current state
- Project status: IN PROGRESS
- Current phase: Phase 04 — Nutrition Catalog and Engine
- Current task: PH04-T04
- Current task status: `[ ]`
- Last completed task: PH04-T03
- Last update: 2026-08-21

## Exact next action
Abrir `phases/PHASE_04_NUTRITION_CATALOG_AND_ENGINE.md` y ejecutar `PH04-T04` (USDA adapter: Cliente HTTP y servicio de integración con USDA FoodData Central API, normalización de nutrientes USDA a códigos canónicos de BSNutrition, caching y trazabilidad de fuentes).

## Active blockers
None.

## Decisions pending
- nombre final de producto (actual: `BSNutrition`);
- Android applicationId (actual: `com.bsnutrition.app`);
- dominio/API hostname (desarrollo: `http://10.0.2.2:8000/api/v1` en emulador / `http://localhost:8000/api/v1`);
- ULID vs UUID;
- branding/design tokens;
- detalles finales de deployment.

## Recently completed
- PH04-T03 — Nutrition Calculation Service: Servicio `NutritionCalculatorService` con normalización por base (100g/ml), escalado por porciones y gramos libres, agregación de comidas y días completos, distribución porcentual de macros e inmutabilidad de snapshots históricos con tests en `NutritionCalculatorServiceTest.php`.
- PH04-T02 — Nutrient Seed: Seeder de catálogo canónico `NutrientSeeder` con 33 nutrientes normalizados (macros, subtipos lipídicos, azúcares, minerales y vitaminas clave), `FoodCategorySeeder` con 12 categorías taxonómicas y `FoodSourceSeeder` con tests en `NutrientSeederTest.php`.
- PH04-T01 — Food/Nutrient Schema (`nutrition-backend/`): Migración `2026_08_21_190000_create_food_catalog_tables.php` (9 tablas: `food_categories`, `food_brands`, `food_sources`, `nutrients`, `foods`, `food_aliases`, `food_barcodes`, `food_portions`, `food_nutrients`), modelos Eloquent con relaciones y scopes de búsqueda (`search`, `byBarcode`, `verified`), y tests en `FoodCatalogSchemaTest.php`.
- PH03-T04 — Connect/save Goals & Dashboard Integration: `ProfileRepository` e implementación en `ProfileRepositoryImpl`, inyección en `OnboardingViewModel` para persistencia simultánea de perfil biométrico y metas calculadas, `HomeViewModel` y `HomeScreen` con tarjetas reactivas de calorías y macronutrientes reales, y conexión en `AppNavHost` con tests en `HomeViewModelTest` y `OnboardingViewModelTest`.
- **Fase 03 (Onboarding and Goals) completada al 100%**.
- PH03-T03 — Android Onboarding Flow (`com.bsnutrition.app.feature.onboarding`): `OnboardingStep` (6 pasos), `OnboardingUiState`, `OnboardingViewModel`, `OnboardingScreen` con Compose y Material 3, selector de sexo/fecha, medidas de altura/peso, nivel de actividad con multiplicadores TDEE, objetivo calórico/ritmo, sistema de unidades y previsualización de metas con tests unitarios en `OnboardingViewModelTest`.
- PH03-T02 — Backend Goal Calculator: Migración `nutrition_goals`, modelo `NutritionGoal`, servicio de dominio `NutritionGoalCalculatorService` (Mifflin-St Jeor `mifflin_v1.0`), endpoints `POST /api/v1/goals/calculate`, `GET /api/v1/goals/current`, `PUT /api/v1/goals` y suite de tests en Pest (`NutritionGoalTest.php`).
- PH03-T01 — Goal formula ADR: ADR-009 formalizado en `DECISIONS.md` con la ecuación Mifflin-St Jeor, multiplicadores TDEE, ajustes por objetivo con déficit/superávit calibrado, límites de seguridad (1200/1500 kcal), distribución de macronutrientes y versión de algoritmo `mifflin_v1.0`.
- **Fase 02 (Android Foundation and Authentication) completada al 100%**.
- PH02-T05 — Navigation shell en Android (`com.bsnutrition.app.navigation`): `Route`, `TopLevelDestination`, `MainTabScreen` con Bottom Navigation Bar (Hoy, Diario, Registrar, Progreso, Más), `AppNavHost` reactivo a autenticación, integración en `MainActivity` y tests unitarios en `NavigationTest`.
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
- `nutrition-backend/`: database/migrations/2026_08_21_190000_create_food_catalog_tables.php, app/Models/Food*.php, app/Models/Nutrient.php, tests/Feature/FoodCatalogSchemaTest.php
- `NutritionApp_AI_Execution_Plan/`: PROJECT_STATUS.md, CHANGELOG.md, phases/PHASE_04_NUTRITION_CATALOG_AND_ENGINE.md

## Tests from last task
- `php ./vendor/bin/pest` -> 32 passed (191 assertions)
- `php ./vendor/bin/pint` -> Format verified

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
