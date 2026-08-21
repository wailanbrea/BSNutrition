# PROJECT STATUS

## Current state
- Project status: IN PROGRESS
- Current phase: Phase 07 — Offline-first and Sync
- Current task: PH07-T01
- Current task status: `[ ]`
- Last completed task: PH06-T05
- Last update: 2026-08-21

## Exact next action
Abrir `phases/PHASE_07_OFFLINE-FIRST_AND_SYNC.md` y ejecutar `PH07-T01` (Room core schema: Entidades completas de Room para `DiaryEntity`, `MealEntity`, `MealEntryEntity`, `WaterLogEntity`, `SyncQueueEntity`, DAOs correspondientes, índices compuestos, TypeConverters e incremento de versión de `AppDatabase`).

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
- PH06-T05 — History navigation: Navegación de historial con fechas relativas (Hoy, Ayer, Mañana) y fechas arbitrarias, modal `DiaryDatePickerModal` con `DatePickerDialog` de Material 3, soporte y renderizado limpio de días vacíos y tests en `DiaryViewModelTest.kt`.
- **Fase 06 (Diary and Dashboard) completada al 100%**.
- PH06-T04 — Today dashboard: `HomeViewModel` y `HomeScreen` actualizados con tarjeta Hero de calorías y anillo circular de progreso, barras lineales reactivas de macronutrientes (`MacroProtein`, `MacroCarbs`, `MacroFat`), widget de hidratación con registros rápidos de agua +250ml / +500ml y accesos directos a comidas con tests en `HomeViewModelTest.kt`.
- PH06-T03 — Android diary UI: Modelos de dominio (`DailyDiary`, `MealLog`, `FoodLogEntry`, `WaterLog`, `DailySummary`), DTOs `DiaryDtos.kt`, cliente Retrofit `DiaryApiService`, repositorio `DiaryRepositoryImpl`, `DiaryViewModel`, pantalla Compose `DiaryScreen` con navegación temporal completa, widget de progreso calórico y de macronutrientes, contador y registro de agua, secciones para Desayuno/Almuerzo/Cena/Meriendas con eliminación y duplicación de comidas/días, con tests en `DiaryRepositoryTest.kt` y `DiaryViewModelTest.kt`.
- PH06-T02 — Diary API: Controladores REST `DiaryController.php`, FormRequests con validación estricta (`AddMealEntryRequest`, `UpdateMealEntryRequest`, `CopyMealRequest`, `CopyDayRequest`, `LogWaterRequest`), Resources JSON estructurados (`DiaryDayResource`, `MealResource`, `MealEntryResource`, `WaterLogResource`, `DailySummaryResource`), endpoints REST para gestión diaria de comidas, agua y duplicación con tests en `DiaryApiTest.php` (75 tests pasando al 100%, 802 aserciones).
- PH06-T01 — Diary backend model/services: Migraciones de base de datos `2026_08_21_220000_create_diary_tables.php` (`diaries`, `meals`, `meal_entries`, `water_logs`), modelos Eloquent (`Diary`, `Meal`, `MealEntry`, `WaterLog`), servicio de dominio `DiaryService` con cálculo inmutable de snapshots nutricionales vía `NutritionCalculatorService`, soporte de idempotencia por `client_id`, operaciones CRUD, clonación de comidas/días completos, control estricto de propiedad por usuario y suite de tests en Pest (`DiaryServiceTest.php`) con 67 tests pasando al 100% (722 aserciones).
- PH05-T04 — Recents: Migración `user_food_recents`, relación `recentFoods` en `User`, endpoints `GET /api/v1/foods/recents`, `POST /api/v1/foods/{id}/recent`, entidad Room `RecentFoodEntity`, `RecentFoodDao`, soporte en `FoodRepositoryImpl`, registro automático de alimentos al seleccionar/guardar, pestaña '🕒 Recientes' en `SearchScreen` con tests en `FoodRecentTest.php` (59 tests pasando al 100%).
- **Fase 05 (Food Search, Favorites and Recents) completada al 100%**.
- PH05-T03 — Favorites: Migración `user_food_favorites`, relación `favoriteFoods` en `User` y `Food`, endpoints `GET /api/v1/foods/favorites`, `POST /api/v1/foods/{id}/favorite` y `GET /api/v1/foods/{id}/favorite`, entidad Room `FavoriteFoodEntity`, `FavoriteFoodDao`, soporte en `FoodRepositoryImpl`, pestaña de favoritos y botones interactivos en `SearchScreen` y `FoodDetailSheet` con tests en `FoodFavoriteTest.php`.
- PH05-T02 — Android Search: Modelos de dominio (`FoodSummary`, `FoodDetail`, `FoodPortion`, `FoodNutrient`, `NutritionCalculation`), `FoodApiService`, `FoodRepository` / `FoodRepositoryImpl`, `SearchViewModel` con debouncing de 300ms, pantalla `SearchScreen` en Compose con badges de cocina dominicana `🇩🇴 RD`, `FoodDetailSheet` modal con selector interactivo de porciones y recálculo en tiempo real, e integración en `AddScreen` con tests en `FoodRepositoryTest.kt` y `SearchViewModelTest.kt`.
- PH05-T01 — Backend Food Search: Endpoints REST `GET /api/v1/foods/search`, `GET /api/v1/foods/{id}`, `GET /api/v1/foods/barcode/{barcode}`, `POST /api/v1/foods/{id}/calculate` con recursos JSON `FoodSummaryResource`, `FoodDetailResource`, `FoodPortionResource`, `FoodNutrientResource`, ranking con locale boosting para República Dominicana `DO`, paginación y tests en `FoodSearchApiTest.php`.
- PH04-T06 — Dominican Dataset Foundation: Seeder de catálogo criollo `DominicanFoodDatasetSeeder` con 15 platos y alimentos canónicos dominicanos (Mangú, Los tres golpes, La bandera, Habichuelas guisadas, Pollo guisado, Moro de guandules, Sancocho, Tostones, Queso frito, Salami frito, Morir soñando, Mofongo, etc.) con sus porciones, nutrientes y alias con tests en `DominicanFoodDatasetSeederTest.php`.
- **Fase 04 (Nutrition Catalog and Engine) completada al 100%**.

## Files/modules changed in last task
- `NutritionApp/`: feature/diary/DiaryUiState.kt, feature/diary/DiaryViewModel.kt, feature/diary/DiaryScreen.kt, test/java/com/bsnutrition/app/feature/diary/DiaryViewModelTest.kt
- `NutritionApp_AI_Execution_Plan/`: PROJECT_STATUS.md, CHANGELOG.md, phases/PHASE_06_DIARY_AND_DASHBOARD.md

## Tests from last task
- `php ./vendor/bin/pest` -> 75 passed (802 assertions)
- `DiaryViewModelTest` -> Added history navigation and date picker tests

## Known issues
None.

## Manual owner actions required
None.
- PH05-T03 — Favorites: Migración `user_food_favorites`, relación `favoriteFoods` en `User` y `Food`, endpoints `GET /api/v1/foods/favorites`, `POST /api/v1/foods/{id}/favorite` y `GET /api/v1/foods/{id}/favorite`, entidad Room `FavoriteFoodEntity`, `FavoriteFoodDao`, soporte en `FoodRepositoryImpl`, pestaña de favoritos y botones interactivos en `SearchScreen` y `FoodDetailSheet` con tests en `FoodFavoriteTest.php`.
- PH05-T02 — Android Search: Modelos de dominio (`FoodSummary`, `FoodDetail`, `FoodPortion`, `FoodNutrient`, `NutritionCalculation`), `FoodApiService`, `FoodRepository` / `FoodRepositoryImpl`, `SearchViewModel` con debouncing de 300ms, pantalla `SearchScreen` en Compose con badges de cocina dominicana `🇩🇴 RD`, `FoodDetailSheet` modal con selector interactivo de porciones y recálculo en tiempo real, e integración en `AddScreen` con tests en `FoodRepositoryTest.kt` y `SearchViewModelTest.kt`.
- PH05-T01 — Backend Food Search: Endpoints REST `GET /api/v1/foods/search`, `GET /api/v1/foods/{id}`, `GET /api/v1/foods/barcode/{barcode}`, `POST /api/v1/foods/{id}/calculate` con recursos JSON `FoodSummaryResource`, `FoodDetailResource`, `FoodPortionResource`, `FoodNutrientResource`, ranking con locale boosting para República Dominicana `DO`, paginación y tests en `FoodSearchApiTest.php`.
- PH04-T06 — Dominican Dataset Foundation: Seeder de catálogo criollo `DominicanFoodDatasetSeeder` con 15 platos y alimentos canónicos dominicanos (Mangú, Los tres golpes, La bandera, Habichuelas guisadas, Pollo guisado, Moro de guandules, Sancocho, Tostones, Queso frito, Salami frito, Morir soñando, Mofongo, etc.) con sus porciones, nutrientes y alias con tests en `DominicanFoodDatasetSeederTest.php`.
- **Fase 04 (Nutrition Catalog and Engine) completada al 100%**.

## Files/modules changed in last task
- `NutritionApp/`: feature/home/HomeViewModel.kt, feature/home/HomeScreen.kt, navigation/MainTabScreen.kt, test/java/com/bsnutrition/app/feature/home/HomeViewModelTest.kt
- `NutritionApp_AI_Execution_Plan/`: PROJECT_STATUS.md, CHANGELOG.md, phases/PHASE_06_DIARY_AND_DASHBOARD.md

## Tests from last task
- `php ./vendor/bin/pest` -> 75 passed (802 assertions)
- `HomeViewModelTest` -> Updated and verified with mock DiaryRepository & GoalRepository

## Known issues
None.

## Manual owner actions required
None.
- PH05-T03 — Favorites: Migración `user_food_favorites`, relación `favoriteFoods` en `User` y `Food`, endpoints `GET /api/v1/foods/favorites`, `POST /api/v1/foods/{id}/favorite` y `GET /api/v1/foods/{id}/favorite`, entidad Room `FavoriteFoodEntity`, `FavoriteFoodDao`, soporte en `FoodRepositoryImpl`, pestaña de favoritos y botones interactivos en `SearchScreen` y `FoodDetailSheet` con tests en `FoodFavoriteTest.php`.
- PH05-T02 — Android Search: Modelos de dominio (`FoodSummary`, `FoodDetail`, `FoodPortion`, `FoodNutrient`, `NutritionCalculation`), `FoodApiService`, `FoodRepository` / `FoodRepositoryImpl`, `SearchViewModel` con debouncing de 300ms, pantalla `SearchScreen` en Compose con badges de cocina dominicana `🇩🇴 RD`, `FoodDetailSheet` modal con selector interactivo de porciones y recálculo en tiempo real, e integración en `AddScreen` con tests en `FoodRepositoryTest.kt` y `SearchViewModelTest.kt`.
- PH05-T01 — Backend Food Search: Endpoints REST `GET /api/v1/foods/search`, `GET /api/v1/foods/{id}`, `GET /api/v1/foods/barcode/{barcode}`, `POST /api/v1/foods/{id}/calculate` con recursos JSON `FoodSummaryResource`, `FoodDetailResource`, `FoodPortionResource`, `FoodNutrientResource`, ranking con locale boosting para República Dominicana `DO`, paginación y tests en `FoodSearchApiTest.php`.
- PH04-T06 — Dominican Dataset Foundation: Seeder de catálogo criollo `DominicanFoodDatasetSeeder` con 15 platos y alimentos canónicos dominicanos (Mangú, Los tres golpes, La bandera, Habichuelas guisadas, Pollo guisado, Moro de guandules, Sancocho, Tostones, Queso frito, Salami frito, Morir soñando, Mofongo, etc.) con sus porciones, nutrientes y alias con tests en `DominicanFoodDatasetSeederTest.php`.
- **Fase 04 (Nutrition Catalog and Engine) completada al 100%**.

## Files/modules changed in last task
- `NutritionApp/`: core/model/DiaryModels.kt, core/network/model/DiaryDtos.kt, core/network/api/DiaryApiService.kt, core/network/NetworkModule.kt, core/data/repository/DiaryRepository.kt, core/data/repository/DiaryRepositoryImpl.kt, core/data/di/DataModule.kt, feature/diary/DiaryUiState.kt, feature/diary/DiaryViewModel.kt, feature/diary/DiaryScreen.kt, navigation/MainTabScreen.kt, tests
- `NutritionApp_AI_Execution_Plan/`: PROJECT_STATUS.md, CHANGELOG.md, phases/PHASE_06_DIARY_AND_DASHBOARD.md

## Tests from last task
- `php ./vendor/bin/pest` -> 75 passed (802 assertions)
- `NutritionApp tests` -> DiaryRepositoryTest, DiaryViewModelTest created and verified

## Known issues
None.

## Manual owner actions required
None.
- PH04-T05 — Open Food Facts Adapter: Cliente `OpenFoodFactsService` para búsqueda y resolución de códigos de barras, mapeo de macronutrientes y micronutrientes, creación de marcas y porciones, y comando CLI `php artisan foods:import-off` con tests en `OpenFoodFactsServiceTest.php`.
- PH04-T04 — USDA Adapter: Cliente `UsdaFoodDataService` para búsqueda y detalle en FoodData Central, mapeo de más de 30 IDs de nutrientes a códigos canónicos, importación a base de datos con porciones y comando CLI `php artisan foods:import-usda` con tests en `UsdaFoodDataServiceTest.php`.
- PH04-T03 — Nutrition Calculation Service: Servicio `NutritionCalculatorService` con normalización por base (100g/ml), escalado por porciones y gramos libres, agregación de comidas y días completos, distribución porcentual de macros e inmutabilidad de snapshots históricos con tests en `NutritionCalculatorServiceTest.php`.
- PH04-T02 — Nutrient Seed: Seeder de catálogo canónico `NutrientSeeder` con 33 nutrientes normalizados (macros, subtipos lipídicos, azúcares, minerales y vitaminas clave), `FoodCategorySeeder` con 12 categorías taxonómicas y `FoodSourceSeeder` con tests en `NutrientSeederTest.php`.
- PH04-T01 — Food/Nutrient Schema: Migración `create_food_catalog_tables.php` (9 tablas), modelos Eloquent con relaciones y scopes de búsqueda (`search`, `byBarcode`, `verified`), y tests en `FoodCatalogSchemaTest.php`.
- PH03-T04 — Connect/save Goals & Dashboard Integration: `ProfileRepository` e implementación en `ProfileRepositoryImpl`, inyección en `OnboardingViewModel`, `HomeViewModel` y `HomeScreen` con tarjetas reactivas de calorías y macronutrientes reales.
- **Fase 03 (Onboarding and Goals) completada al 100%**.
- PH03-T03 — Android Onboarding Flow: `OnboardingStep` (6 pasos), `OnboardingUiState`, `OnboardingViewModel`, `OnboardingScreen` con Compose y Material 3.
- PH03-T02 — Backend Goal Calculator: Migración `nutrition_goals`, modelo `NutritionGoal`, servicio de dominio `NutritionGoalCalculatorService` (Mifflin-St Jeor `mifflin_v1.0`), endpoints `POST /api/v1/goals/calculate`, `GET /api/v1/goals/current`, `PUT /api/v1/goals`.
- PH03-T01 — Goal formula ADR: ADR-009 formalizado en `DECISIONS.md`.
- **Fase 02 (Android Foundation and Authentication) completada al 100%**.
- **Fase 01 (Backend Foundation and Authentication) completada al 100%**.
- PH00 — Repositorios locales, gobernanza, convenciones y CI.

## Files/modules changed in last task
- `nutrition-backend/`: app/Http/Controllers/Api/V1/DiaryController.php, app/Http/Requests/*, app/Http/Resources/*, routes/api.php, tests/Feature/DiaryApiTest.php
- `NutritionApp_AI_Execution_Plan/`: PROJECT_STATUS.md, CHANGELOG.md, phases/PHASE_06_DIARY_AND_DASHBOARD.md

## Tests from last task
- `php ./vendor/bin/pest` -> 75 passed (802 assertions)
- `php ./vendor/bin/pint` -> Format passed

## Known issues
None.

## Manual owner actions required
None.

- PH04-T05 — Open Food Facts Adapter: Cliente `OpenFoodFactsService` para búsqueda y resolución de códigos de barras, mapeo de macronutrientes y micronutrientes, creación de marcas y porciones, y comando CLI `php artisan foods:import-off` con tests en `OpenFoodFactsServiceTest.php`.
- PH04-T04 — USDA Adapter: Cliente `UsdaFoodDataService` para búsqueda y detalle en FoodData Central, mapeo de más de 30 IDs de nutrientes a códigos canónicos, importación a base de datos con porciones y comando CLI `php artisan foods:import-usda` con tests en `UsdaFoodDataServiceTest.php`.
- PH04-T03 — Nutrition Calculation Service: Servicio `NutritionCalculatorService` con normalización por base (100g/ml), escalado por porciones y gramos libres, agregación de comidas y días completos, distribución porcentual de macros e inmutabilidad de snapshots históricos con tests en `NutritionCalculatorServiceTest.php`.
- PH04-T02 — Nutrient Seed: Seeder de catálogo canónico `NutrientSeeder` con 33 nutrientes normalizados (macros, subtipos lipídicos, azúcares, minerales y vitaminas clave), `FoodCategorySeeder` con 12 categorías taxonómicas y `FoodSourceSeeder` con tests en `NutrientSeederTest.php`.
- PH04-T01 — Food/Nutrient Schema (`nutrition-backend/`): Migración `2026_08_21_190000_create_food_catalog_tables.php` (9 tablas), modelos Eloquent con relaciones y scopes de búsqueda (`search`, `byBarcode`, `verified`), y tests en `FoodCatalogSchemaTest.php`.
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
