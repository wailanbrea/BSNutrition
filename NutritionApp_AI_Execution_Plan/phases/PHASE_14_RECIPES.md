# Phase 14 — Recipes

## Goal
Crear recetas multiingrediente y añadir servings al diario.

## Read for this phase
- `04_DATABASE_DESIGN.md`
- `08_NUTRITION_ENGINE.md`

## Entry criteria
- [x] Nutrition/diary stable

## Tasks

### [x] PH14-T01 — Recipe schema/calculation
**Depends on:** None

**Implementation checklist:**
- [x] Recipes (Migración `recipes`, modelo Eloquent `Recipe`)
- [x] Ingredients (Migración `recipe_ingredients`, modelo `RecipeIngredient` y vinculación con catálogo)
- [x] Steps (Migración `recipe_steps`, modelo `RecipeStep` con ordenación secuencial)
- [x] Yield (Cálculo automático de peso total terminado en gramos)
- [x] Per-serving nutrition (Cálculo exacto de calorías, proteínas, carbohidratos y grasas por porción en `RecipeCalculationService.php`)

**Acceptance criteria:**
- Agregación y distribución matemática exacta por porción

**Tests / verification:**
- Tests en Pest (`RecipeCalculationServiceTest.php`)

### [x] PH14-T02 — Recipe CRUD
**Depends on:** None

**Implementation checklist:**
- [x] API (Endpoints REST `GET /api/v1/recipes`, `POST /api/v1/recipes`, `GET /api/v1/recipes/{id}`, `PUT /api/v1/recipes/{id}`, `DELETE /api/v1/recipes/{id}`)
- [x] Android list/detail (Pantallas `RecipeListScreen.kt` y `RecipeDetailScreen.kt`)
- [x] User recipe (Pantalla de creación `RecipeCreateScreen.kt` con gestión dinámica de ingredientes y pasos)

**Acceptance criteria:**
- Creación, listado, visualización y eliminación completas

**Tests / verification:**
- Tests en Pest (`RecipeApiTest.php`) y Kotlin (`RecipeRepositoryTest.kt`, `RecipeViewModelTest.kt`)

### [x] PH14-T03 — Recipe to diary
**Depends on:** None

**Implementation checklist:**
- [x] Serving (Selector de porciones unitarias y fraccionales en `LogRecipeDialog`)
- [x] Snapshot (Captura inmutable de `calories_snapshot`, `protein_snapshot`, etc.)
- [x] Source (Etiquetado `source = 'recipe'` y generación de `client_id` para idempotencia)

**Acceptance criteria:**
- Comportamiento del diario histórico inmutable garantizado al registrar recetas

**Tests / verification:**
- Tests en Pest (`RecipeApiTest.php`) y Kotlin (`RecipeViewModelTest.kt`)

## Phase exit criteria
- [x] Recipes usable (Motor de cálculo backend + CRUD API + Interfaz Compose de recetas e inserción en diario)
- [x] Status -> Phase 15

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

