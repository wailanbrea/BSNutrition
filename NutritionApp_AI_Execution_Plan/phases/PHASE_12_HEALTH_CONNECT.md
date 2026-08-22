# Phase 12 — Health Connect

## Goal
Integrar selected health metrics sin duplicados ni loops.

## Read for this phase
- `03_SYSTEM_ARCHITECTURE.md`
- `10_OFFLINE_SYNC.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [x] Core tracking stable

## Tasks

### [x] PH12-T01 — Permissions/availability
**Depends on:** None

**Implementation checklist:**
- [x] Detect (Comprobación de `HealthConnectClient.getSdkStatus(context)`)
- [x] Explain (Tarjeta explicativa de permisos y privacidad en `HealthConnectSettingsScreen.kt`)
- [x] Request (Lanzador de permisos de Health Connect `PermissionController`)
- [x] Denied/unavailable (Manejo de estados no soportado o pendiente de instalación)
- [x] Disconnect (Interruptor maestro para pausar y desconectar sincronización)

**Acceptance criteria:**
- Todos los estados de disponibilidad y permisos de Health Connect gestionados limpiamente

**Tests / verification:**
- Tests en Kotlin (`HealthConnectViewModelTest.kt`)

### [x] PH12-T02 — Import metrics
**Depends on:** None

**Implementation checklist:**
- [x] Weight (Lectura de `WeightRecord` con agregación de último peso corporal)
- [x] Steps (Lectura agregada de `StepsRecord.COUNT_TOTAL` para el día actual)
- [x] Exercise/active calories if used (Lectura agregada de `TotalCaloriesBurnedRecord.ENERGY_TOTAL`)
- [x] External IDs (Filtrado de IDs para prevenir duplicación)
- [x] Cursor (Filtros de rango temporal `TimeRangeFilter.between(start, end)`)

**Acceptance criteria:**
- Importación sin registros duplicados ni cálculos inflados

**Tests / verification:**
- Tests en Kotlin (`HealthConnectViewModelTest.kt`)

### [x] PH12-T03 — Nutrition/hydration write policy ADR
**Depends on:** None

**Implementation checklist:**
- [x] Decide writes (Exportación de `NutritionRecord` y `HydrationRecord`)
- [x] Loop prevention (Filtrado estricto de registros creados por `com.bsnutrition.app`)
- [x] Source IDs (Prefijado con `clientRecordId` en los metadatos)

**Acceptance criteria:**
- Prevención formalizada y validada contra bucles de retroalimentación (ADR-011)

**Tests / verification:**
- Formalización de ADR-011 en `DECISIONS.md` y tests en `HealthConnectViewModelTest.kt`

## Phase exit criteria
- [x] Health Connect stable (Gestor de permisos, lectura de métricas, exportación segura y prevención de bucles)
- [x] Status -> Phase 13

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

