# Phase 10 — Nutrition Label OCR

## Goal
Crear fallback para productos desconocidos mediante etiqueta.

## Read for this phase
- `09_AI_PIPELINE.md`
- `04_DATABASE_DESIGN.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [x] Barcode/camera pipeline ready

## Tasks

### [x] PH10-T01 — Label capture/OCR
**Depends on:** None

**Implementation checklist:**
- [x] Guided UI (Retícula guía en `NutritionLabelScanScreen.kt`)
- [x] Crop (Área rectangular delimitada para tabla de hechos nutricionales)
- [x] ML Kit OCR (Integración de `play-services-mlkit-text-recognition` con `NutritionLabelAnalyzer`)
- [x] Raw text (Extracción y debouncing de bloques de texto crudo)

**Acceptance criteria:**
- Lectura continua de etiqueta nutricional con extracción precisa de texto OCR

**Tests / verification:**
- Tests en Pest (`NutritionLabelParserServiceTest.php`) y Kotlin (`NutritionLabelScanViewModelTest.kt`)

### [x] PH10-T02 — Structured label parser
**Depends on:** None

**Implementation checklist:**
- [x] Serving size (Detección de tamaño de porción en gramos/ml)
- [x] Nutrients (Calorías, grasas totales, saturadas, trans, sodio, carbohidratos, fibra, azúcares y proteínas)
- [x] Units (Normalización de g/mg a base de 100g)
- [x] Validation (No inventa valores faltantes si no aparecen en la etiqueta)
- [x] Structured output (Endpoint `POST /api/v1/foods/ocr/parse-label` con JSON normalizado)

**Acceptance criteria:**
- No inventa valores nutricionales ausentes y calcula proporciones por 100g

**Tests / verification:**
- Tests en Pest (`NutritionLabelParserServiceTest.php`, `NutritionLabelOcrApiTest.php`, 5 tests pasando)

### [x] PH10-T03 — Confirm/create product
**Depends on:** None

**Implementation checklist:**
- [x] Editable data (Formulario `EditAndConfirmProductView` para ajustes por el usuario)
- [x] Barcode link (Vinculación automática con código de barras en caso de fallback desde el escáner)
- [x] Brand/name (Campos de nombre del producto y marca/fabricante)
- [x] Persist canonical (Endpoint `POST /api/v1/foods/from-label` creando registros en `foods`, `food_brands`, `food_portions`, `food_nutrients` y `food_barcodes`)

**Acceptance criteria:**
- Producto desconocido escaneado se convierte en alimento canónico reutilizable en el catálogo y registrado en el diario

**Tests / verification:**
- Tests en Pest (`NutritionLabelOcrApiTest.php`) y Kotlin (`NutritionLabelScanViewModelTest.kt`)

## Phase exit criteria
- [x] OCR product creation works (Flujo completo: Escáner -> ML Kit OCR -> Parseo regex -> Edición -> Persistencia canónica y diario)
- [x] Status -> Phase 11

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

