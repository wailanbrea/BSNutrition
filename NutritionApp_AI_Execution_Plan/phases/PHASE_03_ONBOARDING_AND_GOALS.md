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

### [x] PH03-T01 — Goal formula ADR
**Depends on:** None

**Implementation checklist:**
- [x] Select BMR formula (Mifflin-St Jeor en `DECISIONS.md`)
- [x] Activity multipliers (Sedentary 1.2, Light 1.375, Moderate 1.55, Active 1.725, Very Active 1.9)
- [x] Goal constraints (Déficit/superávit por kg/sem, límites mínimos seguros de 1200 kcal mujeres / 1500 kcal hombres)
- [x] Macro defaults (Distribución 30/40/30 en pérdida, 25/45/30 en mantenimiento, 30/45/25 en ganancia, agua 35ml/kg)
- [x] Algorithm version (ADR-009 versionado como `mifflin_v1.0`)

**Acceptance criteria:**
- Fórmula documentada, versionada y aprobada en ADR-009

**Tests / verification:**
- Especificación formal y matemática en `DECISIONS.md` y `08_NUTRITION_ENGINE.md`

### [x] PH03-T02 — Backend goal calculator
**Depends on:** None

**Implementation checklist:**
- [x] Domain service (`NutritionGoalCalculatorService.php` con Mifflin-St Jeor)
- [x] Calculate endpoint (`POST /api/v1/goals/calculate`)
- [x] Persist goal (`nutrition_goals` tabla, `NutritionGoal` modelo, `GET /api/v1/goals/current`)
- [x] Manual overrides (`PUT /api/v1/goals`)

**Acceptance criteria:**
- Cálculo determinista de metas calóricas, macronutrientes, agua y fibra con soporte para sobreescritura manual

**Tests / verification:**
- Tests en Pest (`NutritionGoalTest.php`) con 100% de aserciones pasando (28 tests totales)

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
