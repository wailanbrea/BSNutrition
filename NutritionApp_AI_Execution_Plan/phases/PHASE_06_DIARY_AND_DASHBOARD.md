# Phase 06 — Diary and Dashboard

## Goal
Implementar núcleo diario con snapshots históricos y dashboard Today.

## Read for this phase
- `04_DATABASE_DESIGN.md`
- `08_NUTRITION_ENGINE.md`
- `05_API_CONTRACT.md`
- `06_ANDROID_ARCHITECTURE.md`

## Entry criteria
- [ ] Food search ready
- [ ] Goals ready

## Tasks

### [ ] PH06-T01 — Diary backend model/services
**Depends on:** None

**Implementation checklist:**
- [ ] Diaries
- [ ] Meals
- [ ] Entries
- [ ] Snapshots
- [ ] CRUD
- [ ] Ownership
- [ ] Client IDs

**Acceptance criteria:**
- Historical integrity/idempotency

**Tests / verification:**
- CRUD/snapshot/auth/idempotency

### [ ] PH06-T02 — Diary API
**Depends on:** None

**Implementation checklist:**
- [ ] Daily read
- [ ] Add/edit/delete
- [ ] Copy meal/day
- [ ] Totals

**Acceptance criteria:**
- Consistent daily payload

**Tests / verification:**
- Feature tests

### [ ] PH06-T03 — Android diary UI
**Depends on:** None

**Implementation checklist:**
- [ ] Date
- [ ] Meal sections
- [ ] Add/edit/delete
- [ ] Copy
- [ ] Totals

**Acceptance criteria:**
- Complete day management

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH06-T04 — Today dashboard
**Depends on:** None

**Implementation checklist:**
- [ ] Calories
- [ ] Macros
- [ ] Meals
- [ ] Quick add
- [ ] Water/weight hooks

**Acceptance criteria:**
- Updates immediately

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH06-T05 — History navigation
**Depends on:** None

**Implementation checklist:**
- [ ] Previous/next
- [ ] Calendar
- [ ] Empty days

**Acceptance criteria:**
- Historical view/edit works

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Online diary complete
- [ ] Status -> Phase 07

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
