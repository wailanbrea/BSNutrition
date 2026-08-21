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


### [ ] PH07-T03 — Sync worker
**Depends on:** None

**Implementation checklist:**
- [ ] Network constraint
- [ ] Push
- [ ] Pull cursor
- [ ] Backoff
- [ ] Permanent errors
- [ ] Auth

**Acceptance criteria:**
- Reconnect sync succeeds

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH07-T04 — Idempotency/conflicts
**Depends on:** None

**Implementation checklist:**
- [ ] Client IDs
- [ ] Versions
- [ ] Tombstones
- [ ] Retry dedupe
- [ ] ADR

**Acceptance criteria:**
- No duplicate retries

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH07-T05 — Offline E2E
**Depends on:** None

**Implementation checklist:**
- [ ] Create/edit/delete offline
- [ ] Kill/restart
- [ ] Reconnect
- [ ] Server errors

**Acceptance criteria:**
- Critical cases pass

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Offline diary reliable
- [ ] Status -> Phase 08

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
