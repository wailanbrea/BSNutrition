# Phase 07 — Offline-first and Sync

## Goal
Convertir diario/tracking a local-first con Room + WorkManager.

## Read for this phase
- `10_OFFLINE_SYNC.md`
- `06_ANDROID_ARCHITECTURE.md`
- `04_DATABASE_DESIGN.md`

## Entry criteria
- [ ] Online diary correct

## Tasks

### [ ] PH07-T01 — Room core schema
**Depends on:** None

**Implementation checklist:**
- [ ] Diary
- [ ] Entries
- [ ] Foods cache
- [ ] Favorites
- [ ] Water
- [ ] Weight
- [ ] Sync queue
- [ ] Indexes

**Acceptance criteria:**
- Diary renders from Room

**Tests / verification:**
- DAO/migration tests

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
