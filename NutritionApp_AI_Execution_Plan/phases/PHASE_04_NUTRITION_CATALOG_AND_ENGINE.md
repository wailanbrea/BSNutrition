# Phase 04 — Nutrition Catalog and Engine

## Goal
Crear esquema canónico, cálculo nutricional y adapters externos.

## Read for this phase
- `04_DATABASE_DESIGN.md`
- `08_NUTRITION_ENGINE.md`
- `03_SYSTEM_ARCHITECTURE.md`

## Entry criteria
- [ ] Goals working

## Tasks

### [x] PH04-T01 — Food/nutrient schema
**Depends on:** None

**Implementation checklist:**
- [x] Categories (`food_categories`, `FoodCategory`)
- [x] Brands (`food_brands`, `FoodBrand`)
- [x] Foods (`foods`, `Food`)
- [x] Aliases (`food_aliases`, `FoodAlias`)
- [x] Barcodes (`food_barcodes`, `FoodBarcode`)
- [x] Portions (`food_portions`, `FoodPortion`)
- [x] Nutrients (`nutrients`, `Nutrient`)
- [x] Sources (`food_sources`, `FoodSource`)
- [x] Indexes (Índices normalizados, compuestos, únicos y de búsqueda)

**Acceptance criteria:**
- Esquema de base de datos canónico y modelos Eloquent con soporte completo para alimentos genéricos, de marca, porciones, códigos de barras y micronutrientes

**Tests / verification:**
- Tests en Pest (`FoodCatalogSchemaTest.php`) con 32 tests pasando al 100% (191 aserciones)

### [x] PH04-T02 — Nutrient seed
**Depends on:** None

**Implementation checklist:**
- [x] Calories (`calories`, kcal)
- [x] Protein (`protein`, g)
- [x] Carbs (`carbohydrate`, g)
- [x] Fat (`total_fat`, g)
- [x] Fiber (`fiber`, g)
- [x] Sugar (`sugar`, g)
- [x] Sodium (`sodium`, mg)
- [x] Core vitamins/minerals (Ácidos grasos, azúcares añadidos, minerales, vitaminas A, C, D, E, K y complejo B)

**Acceptance criteria:**
- Códigos de nutrientes estables y normalizados con estándares USDA / FAO / INFOODS

**Tests / verification:**
- Tests en Pest (`NutrientSeederTest.php`) verificando catálogo maestro de 33 nutrientes, categorías y fuentes

### [x] PH04-T03 — Nutrition calculation service
**Depends on:** None

**Implementation checklist:**
- [x] Basis normalization (conversión desde base 100g/ml a cantidades libres)
- [x] Portions (escalado de medidas caseras definidas en `food_portions`)
- [x] Scaling (escalado lineal de todos los nutrientes asociados)
- [x] Aggregation (consolidación de múltiples alimentos en comidas y diarios con porcentajes de distribución)
- [x] Precision (calorías en enteros, macronutrientes en 2 decimales, micronutrientes en 4 decimales)
- [x] Snapshot generation (`calories_snapshot`, `protein_snapshot`, `carbs_snapshot`, `fat_snapshot`, `fiber_snapshot`, `nutrient_snapshot_json`)

**Acceptance criteria:**
- Motor de cálculo nutricional determinista, inmutable para históricos y con agregación de macronutrientes

**Tests / verification:**
- Tests en Pest (`NutritionCalculatorServiceTest.php`) verificando base 100g, fracciones, porciones, comidas mixtas e inmutabilidad de snapshots

### [x] PH04-T04 — USDA adapter
**Depends on:** None

**Implementation checklist:**
- [x] Search/detail (`UsdaFoodDataService.php` search y getFoodDetails con caching)
- [x] Normalize (Mapeo estricto de más de 30 IDs de nutrientes USDA a códigos canónicos)
- [x] Cache/source trace (`source = 'usda_fdc'`, `external_source_id = fdcId`)
- [x] Error handling (Manejo de excepciones HTTP, logging y comando `foods:import-usda`)

**Acceptance criteria:**
- Los alimentos de USDA FoodData Central se importan y mapean fielmente a la estructura canónica local con porciones y nutrientes

**Tests / verification:**
- Tests en Pest (`UsdaFoodDataServiceTest.php`) con 42 tests pasando al 100% (285 aserciones)

### [ ] PH04-T05 — Open Food Facts adapter
**Depends on:** None

**Implementation checklist:**
- [ ] Barcode lookup
- [ ] Nutrition mapping
- [ ] Brand/image
- [ ] Cache/source

**Acceptance criteria:**
- External barcode becomes local canonical food

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH04-T06 — Dominican dataset foundation
**Depends on:** None

**Implementation checklist:**
- [ ] Country/locale
- [ ] Aliases
- [ ] Preparation variants
- [ ] Verification

**Acceptance criteria:**
- Local foods represented/boostable

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Catalog and engine tested
- [ ] Status -> Phase 05

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
