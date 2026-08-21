# Phase 07 — Offline-first and Sync

## Goal
Convertir diario/tracking a local-first con Room + WorkManager.

## Read for this phase
- `10_OFFLINE_SYNC.md`
- `06_ANDROID_ARCHITECTURE.md`
- `04_DATABASE_DESIGN.md`

## Entry criteria
- [x] Online diary correct (Completado en Fase 06)

## Tasks

### [x] PH07-T01 — Room core schema
**Depends on:** None

**Implementation checklist:**
- [x] Diary (`DiaryEntity`, `DiaryWithMeals`, `DiaryDao`)
- [x] Entries (`MealEntity`, `MealEntryEntity`, `MealWithEntries`, `MealEntryDao`)
- [x] Foods cache (`FoodCacheEntity`, `FoodCacheDao`)
- [x] Favorites (`FavoriteFoodEntity`, `FavoriteFoodDao`)
- [x] Water (`WaterLogEntity`, `WaterLogDao`)
- [x] Weight (`WeightLogEntity`, `WeightLogDao`)
- [x] Sync queue (`SyncQueueEntity`, `SyncQueueDao`)
- [x] Indexes (Índices únicos y compuestos en `diary_date`, `client_id`, `sync_status`, `meal_id`, `is_deleted`)

**Acceptance criteria:**
- Esquema de base de datos Room completo y preparado para almacenamiento y sincronización offline-first

**Tests / verification:**
- Tests en Kotlin (`RoomSchemaDaoTest.kt`)


### [x] PH07-T02 — Local-first repositories
**Depends on:** None

**Implementation checklist:**
- [x] Room Flow reads (`observeDiaryDay`, `observeWaterLogs`, `observeTotalWater`)
- [x] Local transactions (Operaciones inmediatas en Room con estado `pending_*`)
- [x] Pending mutations (Encolado automático en `SyncQueueDao` con payload serializado en JSON)
- [x] Reconcile (Reconciliación asíncrona sin bloquear la interacción del usuario en la UI)

**Acceptance criteria:**
- La interfaz responde de forma instantánea leyendo y mutando datos locales desde Room, desacoplada de la red

**Tests / verification:**
- Tests en Kotlin (`DiaryRepositoryTest.kt` actualizado con Room DAOs y Flow)


### [x] PH07-T03 — Sync worker
**Depends on:** None

**Implementation checklist:**
- [x] Network constraint (`Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)`)
- [x] Push (Drenado de cola `SyncQueueDao` procesando inserciones, actualizaciones y borrados hacia el backend)
- [x] Pull cursor (Refresco y actualización del estado del diario al reconectar)
- [x] Backoff (`BackoffPolicy.EXPONENTIAL` con reintento automático)
- [x] Permanent errors (Manejo de errores HTTP 4xx definitivos descartando elementos no recuperables)
- [x] Auth (Verificación del token de sesión antes de sincronizar)

**Acceptance criteria:**
- Sincronización automática garantizada en reconexión y en background mediante WorkManager

**Tests / verification:**
- Tests en Kotlin (`DiarySyncWorkerTest.kt`)


### [x] PH07-T04 — Idempotency/conflicts
**Depends on:** None

**Implementation checklist:**
- [x] Client IDs (Generación de UUID en cliente y verificación de unicidad en backend para garantizar idempotencia)
- [x] Versions (Control de versión optimista secuencial `version = version + 1`)
- [x] Tombstones (Borrado lógico en Room y SoftDeletes en backend para evitar reaparición de datos eliminados)
- [x] Retry dedupe (Deduplicación de mutaciones mediante cola FIFO y claves de idempotencia)
- [x] ADR (ADR-010 documentado y aprobado en `DECISIONS.md`)

**Acceptance criteria:**
- Cero registros duplicados en reintentos y convergencia consistente entre cliente y servidor

**Tests / verification:**
- Tests en Pest (`DiaryServiceTest.php` e idempotencia de `client_id`) y tests en Android


### [x] PH07-T05 — Offline E2E
**Depends on:** None

**Implementation checklist:**
- [x] Create/edit/delete offline (Flujo completo de mutaciones persistidas en local sin conexión)
- [x] Kill/restart (Persistencia garantizada de transacciones y de la cola `SyncQueueDao` entre sesiones)
- [x] Reconnect (Drenado automático y sincronización al restaurar la conectividad a internet)
- [x] Server errors (Descarte de errores cliente 4xx definitivos y reintentos con backoff para 5xx)

**Acceptance criteria:**
- Casos críticos de uso offline-first pasan satisfactoriamente

**Tests / verification:**
- Tests en Kotlin (`OfflineSyncE2ETest.kt`)

## Phase exit criteria
- [x] Offline diary reliable (Persistencia en Room con Flow reactivo, WorkManager y reconciliación garantizada)
- [x] Status -> Phase 08

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente (ADR-010 añadido)
- [x] No dejar tareas `[-]` sin checkpoint

