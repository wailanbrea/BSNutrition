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

### [x] PH03-T03 — Android onboarding
**Depends on:** None

**Implementation checklist:**
- [x] Birth/sex (`BirthSexStepView`, `updateBirthAndSex`)
- [x] Height/weight (`HeightWeightStepView`, `updateHeightAndWeight`)
- [x] Activity (`ActivityStepView`, `updateActivityLevel`)
- [x] Goal/rate (`GoalRateStepView`, `updateGoalAndRate`)
- [x] Units (`UnitsStepView`, `updateUnitSystem`)
- [x] Review (`ReviewStepView`, cálculo y previsualización de metas)

**Acceptance criteria:**
- Flujo interactivo completo de onboarding en Compose con indicador de progreso y validación paso a paso

**Tests / verification:**
- Tests unitarios en `OnboardingViewModelTest` validando transiciones de estado, cálculo de metas y límites

### [x] PH03-T04 — Connect/save goals
**Depends on:** None

**Implementation checklist:**
- [x] Submit profile (`ProfileRepository.updateProfile` con datos de onboarding)
- [x] Calculate (`GoalRepository.calculateGoal` con algoritmo Mifflin-St Jeor)
- [x] Review (`ReviewStepView` mostrando desglose de calorías, macronutrientes y agua)
- [x] Confirm (`completeOnboarding()` sincronizando con backend)
- [x] Mark onboarding complete (`isOnboardingComplete` y redirección a `Route.Main` con metas dinámicas en `HomeScreen`)

**Acceptance criteria:**
- Usuario recién registrado completa onboarding y accede a la pantalla Hoy (`HomeScreen`) con sus metas calculadas y activas

**Tests / verification:**
- Tests unitarios en `HomeViewModelTest.kt` y `OnboardingViewModelTest.kt`

## Phase exit criteria
- [x] Onboarding E2E complete (flujo completo desde registro, onboarding 6 pasos, cálculo Mifflin-St Jeor, guardado y visualización en Home)
- [x] Status -> Phase 04

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente (ADR-009)
- [x] No dejar tareas `[-]` sin checkpoint

