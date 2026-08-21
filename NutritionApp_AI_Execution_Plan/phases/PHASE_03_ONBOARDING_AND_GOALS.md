# Phase 03 — Onboarding and Goals

## Goal
Implementar perfil inicial, cálculo versionado de objetivos y flujo Android.

## Read for this phase
- `08_NUTRITION_ENGINE.md`
- `04_DATABASE_DESIGN.md`
- `05_API_CONTRACT.md`

## Entry criteria
- [ ] Auth complete

## Tasks

### [ ] PH03-T01 — Goal formula ADR
**Depends on:** None

**Implementation checklist:**
- [ ] Select BMR formula
- [ ] Activity multipliers
- [ ] Goal constraints
- [ ] Macro defaults
- [ ] Algorithm version

**Acceptance criteria:**
- Formula documented/versioned

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH03-T02 — Backend goal calculator
**Depends on:** None

**Implementation checklist:**
- [ ] Domain service
- [ ] Calculate endpoint
- [ ] Persist goal
- [ ] Manual overrides

**Acceptance criteria:**
- Deterministic result

**Tests / verification:**
- Formula/boundary tests

### [ ] PH03-T03 — Android onboarding
**Depends on:** None

**Implementation checklist:**
- [ ] Birth/sex
- [ ] Height/weight
- [ ] Activity
- [ ] Goal/rate
- [ ] Units
- [ ] Review

**Acceptance criteria:**
- Validated complete flow

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH03-T04 — Connect/save goals
**Depends on:** None

**Implementation checklist:**
- [ ] Submit profile
- [ ] Calculate
- [ ] Review
- [ ] Confirm
- [ ] Mark onboarding complete

**Acceptance criteria:**
- Fresh user reaches Today with targets

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Onboarding E2E complete
- [ ] Status -> Phase 04

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
