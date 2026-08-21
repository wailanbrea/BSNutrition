# CHANGELOG

## Unreleased

### 2026-08-21 — PH04-T05
**Added**
- Adaptador y cliente `app/Services/OpenFoodFactsService.php` para búsqueda, escaneo y resolución de códigos de barras (EAN-13, UPC-A) en Open Food Facts World Database.
- Mapeo de macronutrientes y micronutrientes (`energy-kcal`, `proteins`, `carbohydrates`, `fat`, `fiber`, `sugars`, `sodium`, `calcium`, etc.) con conversión de unidades estándar.
- Resolución de marcas en `food_brands` y porciones en `food_portions`.
- Comando Artisan `php artisan foods:import-off {--barcode=} {--query=}`.
- Suite de pruebas con `Http::fake()` en `tests/Feature/OpenFoodFactsServiceTest.php` (45 tests pasando al 100%).

### 2026-08-21 — PH04-T04
**Added**
- Adaptador y cliente USDA `app/Services/UsdaFoodDataService.php` para búsqueda y detalle de alimentos en USDA FoodData Central con caching y manejo de errores.
- Mapeo determinista de más de 30 IDs y números de nutrientes del USDA a códigos canónicos de BSNutrition (`calories`, `protein`, `carbohydrate`, `total_fat`, `fiber`, `sugar`, etc.).
- Comando Artisan `php artisan foods:import-usda {--query=} {--fdcId=}` para importación automática de alimentos con sus porciones y nutrientes asociados.
- Suite de pruebas con `Http::fake()` en `tests/Feature/UsdaFoodDataServiceTest.php` (42 tests pasando al 100%).

### 2026-08-21 — PH04-T03
**Added**
- Servicio de cálculo de nutrición `app/Services/NutritionCalculatorService.php`:
  - Conversión y normalización exacta por base (100g/ml) para cantidades arbitrarias.
  - Escalado de porciones registradas en `food_portions` o gramos libres.
  - Agregación multinivel para comidas (`meals`) y días completos con cálculo de porcentajes calóricos de macronutrientes.
  - Generación de snapshots históricos inmutables (`calories_snapshot`, `protein_snapshot`, `carbs_snapshot`, `fat_snapshot`, `fiber_snapshot`, `nutrient_snapshot_json`).
- Suite de pruebas exhaustiva en `tests/Feature/NutritionCalculatorServiceTest.php` (39 tests pasando al 100%).

### 2026-08-21 — PH04-T02
**Added**
- Seeder de catálogo canónico `NutrientSeeder.php` con 33 nutrientes normalizados (calorías, macronutrientes, subtipos de ácidos grasos, azúcares añadidos, electrolitos, minerales y vitaminas clave).
- Seeder taxonómico `FoodCategorySeeder.php` con 12 categorías de alimentos con slug e icono.
- Seeder de proveedores `FoodSourceSeeder.php` (`usda_fdc`, `openfoodfacts`, `generic`, `user_custom`).
- Integración en `DatabaseSeeder.php` y suite de pruebas en `tests/Feature/NutrientSeederTest.php` (34 tests pasando al 100%).

### 2026-08-21 — PH04-T01
**Added**
- Migración de catálogo canónico `2026_08_21_190000_create_food_catalog_tables.php` estructurando: `food_categories`, `food_brands`, `food_sources`, `nutrients`, `foods`, `food_aliases`, `food_barcodes`, `food_portions`, `food_nutrients`.
- Modelos Eloquent completos (`FoodCategory`, `FoodBrand`, `FoodSource`, `Nutrient`, `Food`, `FoodAlias`, `FoodBarcode`, `FoodPortion`, `FoodNutrient`) con relaciones íntegras, eliminación en cascada y soft deletes.
- Scopes de consulta optimizados en `Food`: `search(term)` (búsqueda normalizada por nombre, alias y marca), `byBarcode(barcode)` y `verified()`.
- Suite de pruebas exhaustiva en `tests/Feature/FoodCatalogSchemaTest.php` (32 tests pasando al 100%).

**Changed**
- Inicio de la **Fase 04 (Nutrition Catalog and Engine)**.

### 2026-08-21 — PH03-T04
**Added**
- Interfaz `ProfileRepository` e implementación `ProfileRepositoryImpl` con mapeo de red y enlace Hilt en `DataModule`.
- Persistencia unificada de perfil y metas en `OnboardingViewModel.completeOnboarding()`.
- Integración de `Route.Onboarding` en `AppNavHost` para dirigir a usuarios nuevos tras el registro.
- `HomeViewModel` y `HomeScreen` dinámico con consumo reactivo de metas nutricionales calculadas (calorías restantes y desglose de macronutrientes).
- Pruebas unitarias en `HomeViewModelTest.kt` y ampliación de `OnboardingViewModelTest.kt`.

**Changed**
- Cierre exitoso de la **Fase 03 (Onboarding and Goals)** al 100%.

### 2026-08-21 — PH03-T03
**Added**
- Modelo de dominio `NutritionGoal.kt` y DTOs de cálculo y metas `GoalDtos.kt`.
- Interfaz Retrofit `GoalApiService` y repositorio `GoalRepository` / `GoalRepositoryImpl` con inyección Hilt.
- Feature de Onboarding (`com.bsnutrition.app.feature.onboarding`):
  - `OnboardingStep` (6 pasos) y `OnboardingUiState`.
  - `OnboardingViewModel` para control de pasos, cálculo dinámico de metas y envío al repositorio.
  - `OnboardingScreen` con Compose y Material 3 (selector de sexo/fecha, altura/peso, nivel de actividad, objetivo nutricional, unidades y resumen de metas).
- Pruebas unitarias en `OnboardingViewModelTest.kt`.

### 2026-08-21 — PH03-T02
**Added**
- Migración `2026_08_21_180000_create_nutrition_goals_table.php` para almacenar objetivos calóricos, macronutrientes, agua, fibra y versión de cálculo.
- Modelo Eloquent `app/Models/NutritionGoal.php` y relaciones `nutritionGoals()`, `currentNutritionGoal()` en `User.php`.
- Servicio de dominio `app/Services/NutritionGoalCalculatorService.php` con la implementación canónica de Mifflin-St Jeor, TDEE, ajustes por objetivo y distribución de macronutrientes (`mifflin_v1.0`).
- FormRequests de validación `CalculateGoalRequest.php` y `SaveGoalRequest.php`.
- Recurso de serialización JSON `NutritionGoalResource.php`.
- Controlador `GoalController.php` con endpoints:
  - `POST /api/v1/goals/calculate` (cálculo y previsualización).
  - `GET /api/v1/goals/current` (consulta y auto-generación de metas iniciales).
  - `PUT /api/v1/goals` (guardado y ajustes personalizados).
- Suite de pruebas exhaustiva en `tests/Feature/NutritionGoalTest.php` (28 tests pasando al 100%).

### 2026-08-21 — PH03-T01
**Added**
- Formalización de **ADR-009** (Nutrition Goals & Energy Expenditure Calculation Algorithm) en `DECISIONS.md`.
- Definición canónica de BMR con ecuación de Mifflin-St Jeor, factores de actividad TDEE (1.20 a 1.90), ajustes calóricos por objetivo con déficit/superávit calibrado, umbrales de seguridad fisiológica (1200 kcal mujeres / 1500 kcal hombres), distribución porcentual de macronutrientes y versión de algoritmo `mifflin_v1.0`.

**Changed**
- Inicio de la **Fase 03 (Onboarding and Goals)**.

### 2026-08-21 — PH02-T05
**Added**
- Shell de navegación con Jetpack Compose Navigation (`com.bsnutrition.app.navigation`):
  - Rutas tipadas `Route.Login`, `Route.Register`, `Route.Main` y destinos `TopLevelRoute`.
  - Enum `TopLevelDestination` con 5 pestañas canónicas: Hoy, Diario, Registrar, Progreso y Más.
  - `MainTabScreen` con `Scaffold` y `NavigationBar` de Material 3.
  - Vistas base de Compose para features: `HomeScreen`, `DiaryScreen`, `AddScreen`, `ProgressScreen` y `MoreScreen` (con resumen de cuenta y botón de Logout).
  - `AppNavHost` con redirección reactiva basada en `isAuthenticated` y control de backstack.
  - `MainActivity` conectada al `AppNavHost` y temas `BSNutritionTheme`.
  - Pruebas unitarias en `NavigationTest.kt`.

**Changed**
- Cierre exitoso de la **Fase 02 (Android Foundation and Authentication)** al 100%.

### 2026-08-21 — PH02-T04
**Added**
- Capa de datos y repositorio de autenticación: `AuthRepository` y `AuthRepositoryImpl` con persistencia de token en DataStore y cache de usuario en Room (`UserDao`).
- `DataModule` para inyección de dependencias con Hilt.
- Feature de Autenticación (`com.bsnutrition.app.feature.auth`):
  - `AuthUiState` y `AuthViewModel` con manejo de estados asíncronos y errores de validación tipados.
  - `LoginScreen` con Jetpack Compose, Material 3 y Design System.
  - `RegisterScreen` con Jetpack Compose, validación de contraseñas y campos.
- Pruebas unitarias completas: `AuthRepositoryTest.kt` y `AuthViewModelTest.kt`.

**Changed**
- Flujo de autenticación y persistencia de sesión conectado de extremo a extremo en Android.

### 2026-08-21 — PH02-T03
**Added**
- Interfaces de servicios Retrofit: `AuthApiService`, `ProfileApiService` y `HealthApiService`.
- DTOs de transferencia serializables: `RegisterRequestDto`, `LoginRequestDto`, `AuthResponseDto`, `UserDto`, `UserContainerDto`, `UpdateProfileRequestDto`, `UserProfileDto`, `ProfileContainerDto`, `HealthResponseDto`, `MessageResponseDto`.
- Utilidad `safeApiCall` con captura tipada de `ApiException` y parseo de errores de backend `ApiErrorResponse`.
- Provisión de servicios API en `NetworkModule`.
- Pruebas unitarias de red y parseo de errores en `SafeApiCallTest.kt`.

**Changed**
- Configuración de dependencias de prueba con `okhttp-mockwebserver`.

### 2026-08-21 — PH02-T02
**Added**
- Arquitectura `core` en Android:
  - `core.common`: Cualificadores `@Dispatcher`, enum `BsnDispatchers`, `DispatchersModule` y wrapper `Result<T>`.
  - `core.model`: Modelos de dominio `User`, `UserProfile`, `AuthToken`, `AuthResponse`, `ApiErrorResponse`, `ApiErrorDetail` y `ApiException`.
  - `core.datastore`: Interfaz `AuthTokenDataSource`, implementación `AuthTokenDataSourceImpl` con Jetpack Preferences DataStore y `DataStoreModule`.
  - `core.network`: `AuthInterceptor` para inyección de token Bearer en peticiones HTTP y `NetworkModule` con configuración de `Json`, `OkHttpClient` y `Retrofit`.
  - `core.database`: `NutritionDatabase` con Room, `UserEntity`, `UserDao` y `DatabaseModule`.
  - `core.designsystem`: Componentes reutilizables `BsnPrimaryButton`, `BsnSecondaryButton`, `BsnTextField`, `BsnCard` y `BsnLoadingIndicator`.
  - Pruebas unitarias en `ResultTest.kt`.

**Changed**
- Arquitectura desacoplada y modular lista para las capas de red y autenticación.

### 2026-08-21 — PH02-T01
**Added**
- Proyecto base de Android en `NutritionApp/` con Jetpack Compose y Material 3.
- Gradle Version Catalog en `NutritionApp/gradle/libs.versions.toml` con versiones canónicas de Compose, Hilt, Room, Retrofit, OkHttp, Serialization, DataStore y Testing.
- `settings.gradle.kts`, `build.gradle.kts` raíz y `app/build.gradle.kts` con build types (`debug` con Base URL local `http://10.0.2.2:8000/api/v1/` y `release`).
- `NutritionApplication.kt` con `@HiltAndroidApp` y `MainActivity.kt` con `@AndroidEntryPoint`.
- Sistema de temas `BSNutritionTheme` con paleta de colores de nutrición y tipografía Material 3.
- `ExampleUnitTest.kt` para verificación de test runner.

**Changed**
- Inicio de la **Fase 02 (Android Foundation and Authentication)**.

### 2026-08-21 — PH01-T04
**Added**
- Migración `2026_08_21_143204_create_user_profiles_table.php` con campos de datos biométricos, objetivos y preferencias regionales.
- Modelo `app/Models/UserProfile.php` y relación `profile()` en `User`.
- `app/Http/Requests/Profile/UpdateProfileRequest.php` para validación estricta de perfiles.
- `app/Http/Resources/UserProfileResource.php` para serialización de perfiles.
- `app/Http/Controllers/Api/V1/ProfileController.php` con soporte para `GET /api/v1/profile` y `PUT /api/v1/profile`.
- Suite de pruebas de perfil y aislamiento de propiedad en `tests/Feature/ProfileTest.php`.

**Changed**
- Cierre exitoso de la **Fase 01 (Backend Foundation and Authentication)**.

**Tests**
- `php ./vendor/bin/pest` (23 passed, 134 assertions).
- `php ./vendor/bin/pint` (formato limpio verificado).

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
