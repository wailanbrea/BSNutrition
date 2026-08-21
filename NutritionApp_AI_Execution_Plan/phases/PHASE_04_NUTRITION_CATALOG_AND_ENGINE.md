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

### [ ] PH04-T01 — Food/nutrient schema
**Depends on:** None

**Implementation checklist:**
- [ ] Categories
- [ ] Brands
- [ ] Foods
- [ ] Aliases
- [ ] Barcodes
- [ ] Portions
- [ ] Nutrients
- [ ] Sources
- [ ] Indexes

**Acceptance criteria:**
- Supports generic/branded/portions/micronutrients

**Tests / verification:**
- Migration/model tests

### [ ] PH04-T02 — Nutrient seed
**Depends on:** None

**Implementation checklist:**
- [ ] Calories
- [ ] Protein
- [ ] Carbs
- [ ] Fat
- [ ] Fiber
- [ ] Sugar
- [ ] Sodium
- [ ] Core vitamins/minerals

**Acceptance criteria:**
- Stable nutrient codes

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH04-T03 — Nutrition calculation service
**Depends on:** None

**Implementation checklist:**
- [ ] Basis normalization
- [ ] Portions
- [ ] Scaling
- [ ] Aggregation
- [ ] Precision

**Acceptance criteria:**
- Deterministic calculations

**Tests / verification:**
- 100g/fraction/portion/rounding

### [ ] PH04-T04 — USDA adapter
**Depends on:** None

**Implementation checklist:**
- [ ] Search/detail
- [ ] Normalize
- [ ] Cache/source trace
- [ ] Error handling

**Acceptance criteria:**
- USDA result imports as canonical food

**Tests / verification:**
- Build/tests relevantes deben pasar

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
