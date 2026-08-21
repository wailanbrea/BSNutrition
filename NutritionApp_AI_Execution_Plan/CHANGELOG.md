# CHANGELOG

## Unreleased

### 2026-08-21 — PH09-T03
**Added**
- Servicio `FoodMatchingService` para vinculación determinista de alimentos reconocidos por IA con la base de datos canónica.
- DTO `FoodMatchCandidate` con puntuación de confianza, tipo de coincidencia (exact, alias, token) y desglose de macronutrientes.
- Algoritmo de normalización léxica (eliminación de acentos y caracteres especiales), compatibilidad con métodos de cocción (frito, asado, hervido) y boosting regional dominicano (`DO`).
- Suite de pruebas en Pest (`FoodMatchingServiceTest.php`, 5 tests pasando).

### 2026-08-21 — PH09-T02
**Added**
- Interfaz `AiVisionProviderInterface` y DTOs estructurados `AiFoodAnalysisResult` y `AiRecognizedFoodItem`.
- Implementación de `OpenAiVisionProvider` utilizando modelos multimodales GPT-4o / GPT-4o-mini con schema JSON estructurado, inteligencia culinaria dominicana y contabilidad de tokens y costes en USD.
- Implementación `MockVisionProvider` para pruebas deterministas y ejecución offline.
- Gestor `AiVisionManager` para resolución dinámica de proveedores de visión IA.
- Tests en Pest (`AiVisionProviderTest.php`, 4 tests pasando).

### 2026-08-21 — PH09-T01
**Added**
- Tabla y migración `ai_image_uploads` y modelo Eloquent `AiImageUpload` para gestión segura de imágenes privadas.
- Servicio `ImageStorageService` con almacenamiento privado (`Storage::disk('local')`), extracción base64 para APIs de visión, validación de tipo MIME y tamaño (<10MB).
- Métodos de borrado tras inferencia (`deleteUpload`) y limpieza automatizada de registros y archivos expirados (`cleanupExpiredUploads`).
- Suite de pruebas en Pest (`ImageStorageServiceTest.php`, 5 tests pasando).

### 2026-08-21 — PH08-T03
**Added**
- Despliegue automático del bottom sheet modal `FoodDetailSheet` al detectar un código de barras en el escáner.
- Selección interactiva de porción y cálculo dinámico de macronutrientes en tiempo real.
- Registro directo en el diario nutricional con persistencia local-first y adición automática a alimentos recientes.
- Diálogo de producto no encontrado con opción para reintentar escaneo o regresar a búsqueda.
- Tests unitarios en `BarcodeScannerViewModelTest.kt`.
- **Fase 08 (Barcode) completada al 100%**.

### 2026-08-21 — PH08-T02
**Added**
- Pantalla `BarcodeScannerScreen` con vista previa de cámara en tiempo real mediante CameraX y análisis con Google ML Kit.
- Analizador `BarcodeAnalyzer` con soporte para formatos EAN-13, EAN-8, UPC-A, UPC-E, QR y Code-128.
- Control de throttling de 1.5s y manejo de permisos `Manifest.permission.CAMERA` en tiempo de ejecución.
- Superposición de visor con retícula cuadrada y botón para alternar linterna/flash.
- Integración en `AddScreen` y `SearchScreen`.

### 2026-08-21 — PH08-T01
**Added**
- Endpoint `GET /api/v1/foods/barcode/{barcode}` con lookup local en base de datos de alimentos.
- Fallback automático e importación transparente de productos desde Open Food Facts mediante `OpenFoodFactsService`.
- Creación y persistencia estructurada de marcas, nutrientes canónicos y porciones en la base de datos canónica.
- Respuesta HTTP 404 estructurada con formato JSON estandarizado si el producto no existe en ninguna fuente.
- Tests completos en `FoodSearchApiTest.php` y `OpenFoodFactsServiceTest.php`.

### 2026-08-21 — PH07-T05
**Added**
- Suite integral de pruebas E2E `OfflineSyncE2ETest.kt` cubriendo ciclo completo offline (creación, edición, eliminación de comidas y agua sin red).
- Validación de persistencia de transacciones y de la cola `SyncQueueDao` simulando caída o reinicio del proceso de la aplicación.
- Drenado automático y reconciliación consistente de mutaciones al restaurar la conectividad a internet.
- Tolerancia a fallos de backend y descarte de respuestas cliente (4xx) para evitar bucles venenosos en la cola.
- **Fase 07 (Offline-First and Sync) completada al 100%**.

### 2026-08-21 — PH07-T04
**Added**
- Formalización de **ADR-010 — Offline-First Synchronization & Conflict Resolution Strategy** en `DECISIONS.md`.
- Garantía de idempotencia en peticiones de diario y agua mediante `client_id` (UUIDv4) con verificación en `DiaryService.php`.
- Control de versiones optimista y políticas de Last-Write-Wins (LWW) en sincronización distribuida.
- Estrategia de eliminación basada en lápidas (tombstones) mediante soft deletes locales (`is_deleted`) y en servidor (`deleted_at`).

### 2026-08-21 — PH07-T03
**Added**
- Worker de sincronización en segundo plano `DiarySyncWorker` (HiltWorker + CoroutineWorker) con restricciones de red activa `NetworkType.CONNECTED`.
- Drenado automático de mutaciones pendientes de creación, edición y eliminación de comidas y agua desde `SyncQueueDao`.
- Política de reintentos y retroceso exponencial `BackoffPolicy.EXPONENTIAL` ante fallos transitorios de red.
- Manejo inteligente de errores permanentes (4xx) para prevenir atascos en la cola local.
- Utilidad `SyncManager` para programar tareas periódicas cada 15 minutos o invocar sincronizaciones inmediatas bajo demanda.
- Tests unitarios en `DiarySyncWorkerTest.kt`.

### 2026-08-21 — PH07-T02
**Added**
- Lecturas reactivas sin bloqueo de red en `DiaryRepositoryImpl` vía `observeDiaryDay`, `observeWaterLogs` y `observeTotalWater`.
- Transacciones locales inmediatas en Room para agregar, editar y eliminar comidas, y registrar ingesta de agua con asignación de `clientId` único (`UUID`).
- Encolado atómico de mutaciones en `SyncQueueDao` con payload serializado JSON para sincronización garantizada.
- Reconciliación asíncrona transparente en segundo plano sin interrumpir ni demorar la respuesta de la UI.
- Pruebas unitarias completas en `DiaryRepositoryTest.kt` cubriendo lectura en caché, inserción con cola y flujos reactivos.

### 2026-08-21 — PH07-T01
**Added**
- Entidades completas de Room para persistencia offline-first: `DiaryEntity`, `MealEntity`, `MealEntryEntity`, `MealWithEntries`, `DiaryWithMeals`, `WaterLogEntity`, `WeightLogEntity`, `FoodCacheEntity`, `SyncQueueEntity`.
- DAOs de Room con soporte de reactividad Flow: `DiaryDao`, `MealEntryDao`, `WaterLogDao`, `WeightLogDao`, `FoodCacheDao`, `SyncQueueDao`.
- Índices únicos y compuestos optimizados para queries locales instantáneos y estados de sincronización (`synced`, `pending_create`, `pending_update`, `pending_delete`).
- Actualización a versión 2 de `NutritionDatabase` y registro de todos los DAOs en `DatabaseModule`.
- Suite de pruebas unitarias en `RoomSchemaDaoTest.kt`.

### 2026-08-21 — PH06-T05
**Added**
- Navegación temporal e histórica en `DiaryViewModel` con selección de fechas arbitrarias y recarga dinámica de registros.
- Componente `DiaryDatePickerModal` con `DatePickerDialog` de Material 3 para salto rápido a cualquier fecha histórica o futura.
- Soporte y renderizado de días vacíos con estado limpio y botones directos para agregar comidas.
- Tests unitarios en `DiaryViewModelTest.kt` cubriendo apertura/cierre de modal de fecha y carga de días históricos.
- **Fase 06 (Diary and Dashboard) completada al 100%**.

### 2026-08-21 — PH06-T04
**Added**
- Integración de `DiaryRepository` en `HomeViewModel` para cargar reactivamente el diario y consumo del día actual.
- Tarjeta Hero en `HomeScreen` con anillo circular de progreso calórico (`CalorieHeroCard`), calorías consumidas, meta y balance restante.
- Desglose interactivo de macronutrientes (`MacroBreakdownCard`) con barras de progreso para Proteínas, Carbohidratos y Grasas.
- Widget de hidratación (`HomeWaterTrackerCard`) con botones de acceso rápido `+250ml` y `+500ml` con recálculo en tiempo real.
- Listado de comidas del día (`HomeMealCard`) con conteo de alimentos, calorías registradas y botón de agregar alimento con cambio fluido a la pestaña de búsqueda.
- Pruebas unitarias actualizadas en `HomeViewModelTest.kt`.

### 2026-08-21 — PH06-T03
**Added**
- Modelos de dominio del diario en Android (`DailyDiary`, `MealLog`, `FoodLogEntry`, `WaterLog`, `DailySummary`).
- DTOs y serialización `DiaryDtos.kt` para respuestas y solicitudes del diario.
- Cliente Retrofit `DiaryApiService` e inyección en `NetworkModule`.
- Repositorio `DiaryRepository` e implementación `DiaryRepositoryImpl` con mapeo y manejo de errores.
- `DiaryViewModel` con estado reactivo, navegación de fechas, eliminación de entradas, modales de duplicación y registro de agua.
- Pantalla en Jetpack Compose `DiaryScreen` con cabecera de fechas (Hoy, Ayer, Mañana), tarjeta de calorías restantes vs metas, barras de macronutrientes, widget de hidratación y 4 secciones de comidas estructuradas.
- Conexión de navegación en `MainTabScreen`.
- Suite de pruebas unitarias en `DiaryRepositoryTest.kt` y `DiaryViewModelTest.kt`.

### 2026-08-21 — PH06-T02
**Added**
- Controlador REST `app/Http/Controllers/Api/V1/DiaryController.php` con endpoints:
  - `GET /api/v1/diary/{date}`: Obtener o inicializar el diario diario con sus 4 comidas y resumen.
  - `POST /api/v1/diary/{date}/entries`: Registrar alimento en una comida con cálculo dinámico de snapshots nutricionales.
  - `PUT /api/v1/diary/entries/{id}`: Actualizar cantidad, porción o alimento con recálculo e incremento de versión.
  - `DELETE /api/v1/diary/entries/{id}`: Eliminación lógica de entrada de comida.
  - `POST /api/v1/diary/copy-meal`: Copiar comida completa a otra fecha o sección.
  - `POST /api/v1/diary/copy-day`: Duplicar el diario completo a otra fecha.
  - `GET /api/v1/diary/{date}/water`: Listado de registros de agua del día.
  - `POST /api/v1/diary/{date}/water`: Registro de consumo de agua en mililitros.
  - `DELETE /api/v1/diary/water/{id}`: Eliminar registro de agua.
  - `GET /api/v1/diary/{date}/summary`: Totales calóricos, macronutrientes, fibra y agua.
- FormRequests con validación exhaustiva: `AddMealEntryRequest`, `UpdateMealEntryRequest`, `CopyMealRequest`, `CopyDayRequest`, `LogWaterRequest`.
- JSON Resources estructurados: `DiaryDayResource`, `MealResource`, `MealEntryResource`, `WaterLogResource`, `DailySummaryResource`.
- Suite de pruebas de integración en Pest `tests/Feature/DiaryApiTest.php` (75 tests pasando al 100%, 802 aserciones).

### 2026-08-21 — PH06-T01
**Added**
- Migración `2026_08_21_220000_create_diary_tables.php` con tablas `diaries`, `meals`, `meal_entries` y `water_logs`.
- Modelos Eloquent `Diary`, `Meal`, `MealEntry` y `WaterLog` con relaciones, casts y soft deletes.
- Relaciones `diaries()` y `waterLogs()` en modelo `User`.
- Servicio de dominio `DiaryService`:
  - `getOrCreateDiaryForDate`: Generación automática de diario con 4 comidas canónicas (desayuno, almuerzo, cena, merienda).
  - `addEntry`: Creación de entradas con cálculo exacto de snapshots nutricionales inmutables vía `NutritionCalculatorService`, soporte de idempotencia por `client_id` y registro automático de recientes.
  - `updateEntry`: Modificación segura con recálculo de snapshots e incremento de versión.
  - `deleteEntry`: Eliminación lógica (soft delete) con verificación de pertenencia.
  - `copyMeal`: Duplicación de comidas completas entre fechas/tipos de comida.
  - `copyDay`: Duplicación de días completos de diario.
  - `logWater` y `getDailyWaterTotal`: Registro y sumatoria de agua consumida.
  - `getDailySummary`: Agregación de totales calóricos, macronutrientes y resumen por comidas.
- Suite de pruebas en Pest `tests/Feature/DiaryServiceTest.php` (67 tests pasando al 100%, 722 aserciones).

### 2026-08-21 — PH05-T04 (Fase 05 Completada al 100%)
**Added**
- Migración de base de datos `2026_08_21_210000_create_user_food_recents_table.php` para almacenamiento del historial de consumo con contador `use_count` y marca temporal `last_used_at`.
- Relación `recentFoods()` en modelo `User`.
- Endpoints REST en `FoodController.php`:
  - `GET /api/v1/foods/recents`: Listado ordenado por mayor frecuencia y último uso.
  - `POST /api/v1/foods/{id}/recent`: Registro de consumo o selección.
- Entidad Room `RecentFoodEntity`, DAO `RecentFoodDao`, e integración en `NutritionDatabase` y `DatabaseModule`.
- Pestaña '🕒 Recientes' en `SearchScreen` y registro automático de alimentos en `SearchViewModel`.
- Tests en Pest `tests/Feature/FoodRecentTest.php` (59 tests pasando al 100%) y tests unitarios en Android.

**Changed**
- **Fase 05 (Food Search, Favorites and Recents) completada al 100%**.
- Inicio de la **Fase 06 (Diary and Dashboard)**.

### 2026-08-21 — PH05-T03
**Added**
- Migración de base de datos `2026_08_21_200000_create_user_food_favorites_table.php` para persistencia relacional con clave compuesta única e índices.
- Relación `favoriteFoods()` en `User` y `favoritedByUsers()` en `Food`.
- Endpoints REST en `FoodController.php`:
  - `GET /api/v1/foods/favorites`: Listado paginado de favoritos del usuario autenticado.
  - `POST /api/v1/foods/{id}/favorite`: Alternar (toggle) alimento como favorito.
  - `GET /api/v1/foods/{id}/favorite`: Verificación del estado de favorito.
- Entidad Room `FavoriteFoodEntity`, interfaz `FavoriteFoodDao`, e integración en `NutritionDatabase` y `DatabaseModule` para soporte offline y rendimiento instantáneo.
- Métodos `getFavorites()`, `toggleFavorite()` y `isFavorite()` en `FoodRepository` y `FoodRepositoryImpl`.
- Pestaña '⭐ Favoritos' en `SearchScreen` y botones de estrella de favoritos interactivos en `FoodResultCard` y `FoodDetailSheet`.
- Tests en Pest `tests/Feature/FoodFavoriteTest.php` (56 tests pasando al 100%) y tests unitarios en Android.

### 2026-08-21 — PH05-T02
**Added**
- Modelos de dominio de alimentos (`FoodSummary`, `FoodDetail`, `FoodPortion`, `FoodNutrient`, `NutritionCalculation`, `MacroBreakdown`).
- DTOs y adaptadores de serialización `FoodDtos.kt` con mapeo a entidades de dominio.
- Cliente Retrofit `FoodApiService` con endpoints de búsqueda, detalle, código de barras y cálculo por porciones.
- Repositorio `FoodRepository` e implementación `FoodRepositoryImpl` con manejo estructurado de errores y corrutinas.
- `SearchViewModel` con debouncing reactivo de 300ms, filtrado por categorías taxonómicas, carga de detalles y recálculo de porciones.
- Pantalla Compose `SearchScreen` con buscador rápido, chips de categorías, listado optimizado con distintivo de comida típica `🇩🇴 RD` y resumen de macronutrientes.
- Componente interactivo `FoodDetailSheet` (ModalBottomSheet) con selector de medidas caseras, ajuste dinámico de cantidad, desglose de micronutrientes y cálculo de calorías/macros en vivo.
- Integración en flujo de registro en `AddScreen`.
- Suite de pruebas unitarias en `FoodRepositoryTest.kt` y `SearchViewModelTest.kt`.

### 2026-08-21 — PH05-T01
**Added**
- Controlador REST `app/Http/Controllers/Api/V1/FoodController.php` con endpoints:
  - `GET /api/v1/foods/search`: Búsqueda de alimentos por nombre, alias y marca con ranking priorizado para República Dominicana (`country_code = 'DO'`), filtrado por categoría y paginación.
  - `GET /api/v1/foods/{id}`: Detalle exhaustivo del alimento con todas sus porciones, nutrientes, códigos de barras y alias.
  - `GET /api/v1/foods/barcode/{barcode}`: Búsqueda rápida por código de barras en base local con fallback automático a Open Food Facts.
  - `POST /api/v1/foods/{id}/calculate`: Cálculo de macros y micronutrientes para porciones o gramos personalizados.
- Recursos JSON estructurados `FoodSummaryResource`, `FoodDetailResource`, `FoodPortionResource`, `FoodNutrientResource`.
- Suite de pruebas en `tests/Feature/FoodSearchApiTest.php` (53 tests pasando al 100%).

### 2026-08-21 — PH04-T06 (Fase 04 Completada al 100%)
**Added**
- Seeder de base de datos `DominicanFoodDatasetSeeder.php` con 15 platos y alimentos canónicos dominicanos (Mangú, Los tres golpes, La bandera, Habichuelas guisadas, Pollo guisado, Moro de guandules, Sancocho, Tostones, Queso frito, Salami frito, Morir soñando, Mofongo, etc.).
- Alias criollos, variantes de preparación y porciones cotidianas registradas con país `DO` e idioma `es`.
- Conexión en `DatabaseSeeder.php` y suite de pruebas en `tests/Feature/DominicanFoodDatasetSeederTest.php` (46 tests pasando al 100%).

**Changed**
- **Fase 04 (Nutrition Catalog and Engine) completada al 100%**.
- Inicio de la **Fase 05 (Food Search, Favorites and Recents)**.

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
