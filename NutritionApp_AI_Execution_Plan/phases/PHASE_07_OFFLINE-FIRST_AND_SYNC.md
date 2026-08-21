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


### [ ] PH07-T02 — Local-first repositories
**Depends on:** None

**Implementation checklist:**
- [ ] Room Flow reads
- [ ] Local transactions
- [ ] Pending mutations
- [ ] Reconcile

**Acceptance criteria:**
- UI does not wait on network

**Tests / verification:**
- Build/tests relevantes deben pasar

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
