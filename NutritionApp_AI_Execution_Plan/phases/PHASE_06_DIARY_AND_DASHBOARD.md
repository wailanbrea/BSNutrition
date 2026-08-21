# Phase 06 — Diary and Dashboard

## Goal
Implementar núcleo diario con snapshots históricos y dashboard Today.

## Read for this phase
- `04_DATABASE_DESIGN.md`
- `08_NUTRITION_ENGINE.md`
- `05_API_CONTRACT.md`
- `06_ANDROID_ARCHITECTURE.md`

## Entry criteria
- [x] Food search ready (Búsqueda de alimentos por texto, alias, códigos de barras y cálculo dinámico operativo)
- [x] Goals ready (Metas nutricionales calculadas y persistidas)

## Tasks

### [x] PH06-T01 — Diary backend model/services
**Depends on:** None

**Implementation checklist:**
- [x] Diaries (Migración `diaries`, modelo `Diary`, unicidad `user_id` + `diary_date`, zona horaria y notas)
- [x] Meals (Migración `meals`, modelo `Meal`, 4 comidas estándar: desayuno, almuerzo, cena, merienda)
- [x] Entries (Migración `meal_entries`, modelo `MealEntry`, soft deletes)
- [x] Snapshots (Inmutabilidad histórica de calorías, macros y micronutrientes calculados con `NutritionCalculatorService`)
- [x] CRUD (`addEntry`, `updateEntry`, `deleteEntry`, `getOrCreateDiaryForDate`)
- [x] Ownership (Validación estricta de propiedad de usuario en modificaciones)
- [x] Client IDs (Soporte de idempotencia con `client_id` para operaciones offline-first)
- [x] Water logs (Migración `water_logs`, modelo `WaterLog`, registro y agregación diaria)
- [x] Copying (`copyMeal`, `copyDay`)

**Acceptance criteria:**
- Integridad histórica garantizada mediante snapshots e idempotencia por `client_id`

**Tests / verification:**
- Tests en Pest (`DiaryServiceTest.php`) con 67 tests pasando al 100% (722 aserciones)


### [x] PH06-T02 — Diary API
**Depends on:** None

**Implementation checklist:**
- [x] Daily read (`GET /api/v1/diary/{date}` con `DiaryDayResource`, `MealResource` y totales agregados)
- [x] Add/edit/delete (`POST /api/v1/diary/{date}/entries`, `PUT /api/v1/diary/entries/{id}`, `DELETE /api/v1/diary/entries/{id}`)
- [x] Copy meal/day (`POST /api/v1/diary/copy-meal`, `POST /api/v1/diary/copy-day`)
- [x] Totals (`GET /api/v1/diary/{date}/summary`, registro y consulta de agua `GET/POST /api/v1/diary/{date}/water`, `DELETE /api/v1/diary/water/{id}`)

**Acceptance criteria:**
- Cargas y respuestas JSON normalizadas, estructuradas y consistentes para consumo directo por la app Android

**Tests / verification:**
- Tests en Pest (`DiaryApiTest.php`) con 75 tests pasando al 100% (802 aserciones)


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
