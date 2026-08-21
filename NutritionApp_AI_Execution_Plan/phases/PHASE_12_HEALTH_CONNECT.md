# Phase 12 — Health Connect

## Goal
Integrar selected health metrics sin duplicados ni loops.

## Read for this phase
- `03_SYSTEM_ARCHITECTURE.md`
- `10_OFFLINE_SYNC.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [ ] Core tracking stable

## Tasks

### [ ] PH12-T01 — Permissions/availability
**Depends on:** None

**Implementation checklist:**
- [ ] Detect
- [ ] Explain
- [ ] Request
- [ ] Denied/unavailable
- [ ] Disconnect

**Acceptance criteria:**
- All permission states handled

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH12-T02 — Import metrics
**Depends on:** None

**Implementation checklist:**
- [ ] Weight
- [ ] Steps
- [ ] Exercise/active calories if used
- [ ] External IDs
- [ ] Cursor

**Acceptance criteria:**
- No duplicates

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH12-T03 — Nutrition/hydration write policy ADR
**Depends on:** None

**Implementation checklist:**
- [ ] Decide writes
- [ ] Loop prevention
- [ ] Source IDs

**Acceptance criteria:**
- No feedback loop

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Health Connect stable
- [ ] Status -> Phase 13

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
