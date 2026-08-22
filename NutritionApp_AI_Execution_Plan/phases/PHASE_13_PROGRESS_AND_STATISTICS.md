# Phase 13 — Progress and Statistics

## Goal
Completar agua, peso y visualización de progreso.

## Read for this phase
- `04_DATABASE_DESIGN.md`
- `08_NUTRITION_ENGINE.md`

## Entry criteria
- [x] Diary data available

## Tasks

### [x] PH13-T01 — Water tracking
**Depends on:** None

**Implementation checklist:**
- [x] API (Endpoints `GET /api/v1/water/logs`, `POST /api/v1/water/logs`, `DELETE /api/v1/water/logs/{id}`)
- [x] Room (Entidad `WaterLogEntity`, DAO `WaterLogDao`, repositorio `WaterRepositoryImpl` con sincronización local-first)
- [x] Quick amounts (Botones rápidos +250ml, +500ml, +750ml, +1000ml en `WaterTrackerWidget`)
- [x] Goal (Meta configurable y cálculo de porcentaje consumido)
- [x] Dashboard (Integrado en `ProgressScreen.kt`)

**Acceptance criteria:**
- Registro de agua offline-first y reconciliación garantizada con el backend

**Tests / verification:**
- Tests en Pest (`WaterTrackingApiTest.php`) y Kotlin (`WaterRepositoryTest.kt`)

### [x] PH13-T02 — Weight tracking
**Depends on:** None

**Implementation checklist:**
- [x] API (Endpoints `GET /api/v1/weight/logs`, `POST /api/v1/weight/logs`, `DELETE /api/v1/weight/logs/{id}`)
- [x] Room (Entidad `WeightLogEntity`, DAO `WeightLogDao`, repositorio `WeightRepositoryImpl`)
- [x] Units (Conversión bidireccional kg / lbs)
- [x] Source (Etiquetado de origen manual o health_connect)
- [x] Health integration (Lectura e importación desde Health Connect)

**Acceptance criteria:**
- Historial de pesaje exacto con fuente trazable

**Tests / verification:**
- Tests en Pest (`WeightTrackingApiTest.php`) y Kotlin (`WeightRepositoryTest.kt`)

### [x] PH13-T03 — Statistics services
**Depends on:** None

**Implementation checklist:**
- [x] Daily/7/30/90 (Agregación dinámica por rangos temporales en `StatisticsService.php`)
- [x] Averages (Promedio diario de calorías, proteínas, carbohidratos, grasas y agua)
- [x] Adherence (Cálculo de tasa de adherencia porcentual a la meta calórica dentro del +/-15%)

**Acceptance criteria:**
- Los totales agregados coinciden exactamente con los snapshots inmutables del diario

**Tests / verification:**
- Tests en Pest (`StatisticsApiTest.php`) y Kotlin (`ProgressViewModelTest.kt`)

### [x] PH13-T04 — Progress UI
**Depends on:** None

**Implementation checklist:**
- [x] Weight chart (Visualización de peso actual, peso meta y delta del período)
- [x] Macro/calorie summary (Barras comparativas de distribución de macronutrientes)
- [x] Range selector (Chips de selección rápida 7d, 30d, 90d)
- [x] States (Estados interactivos de carga, éxito y diálogo de registro de peso)

**Acceptance criteria:**
- Pantalla de progreso reactiva y visualmente clara conectada a datos locales y remotos

**Tests / verification:**
- Tests en Kotlin (`ProgressViewModelTest.kt`)

## Phase exit criteria
- [x] Progress/stats complete (Backend Water/Weight/Statistics API + Room Local-First + Compose Progress UI)
- [x] Status -> Phase 14

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

