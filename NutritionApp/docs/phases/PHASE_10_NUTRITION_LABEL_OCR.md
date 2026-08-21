# Phase 10 — Nutrition Label OCR

## Goal
Crear fallback para productos desconocidos mediante etiqueta.

## Read for this phase
- `09_AI_PIPELINE.md`
- `04_DATABASE_DESIGN.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [ ] Barcode/camera pipeline ready

## Tasks

### [ ] PH10-T01 — Label capture/OCR
**Depends on:** None

**Implementation checklist:**
- [ ] Guided UI
- [ ] Crop
- [ ] ML Kit OCR
- [ ] Raw text

**Acceptance criteria:**
- Readable label -> OCR text

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH10-T02 — Structured label parser
**Depends on:** None

**Implementation checklist:**
- [ ] Serving size
- [ ] Nutrients
- [ ] Units
- [ ] Validation
- [ ] Structured output

**Acceptance criteria:**
- Does not invent missing values

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH10-T03 — Confirm/create product
**Depends on:** None

**Implementation checklist:**
- [ ] Editable data
- [ ] Barcode link
- [ ] Brand/name
- [ ] Persist canonical

**Acceptance criteria:**
- Unknown barcode becomes reusable food

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] OCR product creation works
- [ ] Status -> Phase 11

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
