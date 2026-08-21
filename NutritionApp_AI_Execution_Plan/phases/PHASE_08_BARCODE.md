# Phase 08 — Barcode

## Goal
Agregar escaneo CameraX/ML Kit y lookup local/externo.

## Read for this phase
- `02_TECH_STACK.md`
- `05_API_CONTRACT.md`
- `04_DATABASE_DESIGN.md`

## Entry criteria
- [ ] Catalog/search ready

## Tasks

### [ ] PH08-T01 — Barcode lookup API
**Depends on:** None

**Implementation checklist:**
- [ ] Local first
- [ ] OFF fallback
- [ ] Import/cache
- [ ] Not found
- [ ] Rate limit

**Acceptance criteria:**
- Known local no external call
- External cached

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH08-T02 — Android scanner
**Depends on:** None

**Implementation checklist:**
- [ ] CameraX
- [ ] ML Kit
- [ ] Throttle
- [ ] Permissions
- [ ] Lifecycle

**Acceptance criteria:**
- Stable single scan event

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH08-T03 — Log scanned product
**Depends on:** None

**Implementation checklist:**
- [ ] Detail
- [ ] Portion
- [ ] Add diary
- [ ] Unknown path

**Acceptance criteria:**
- Barcode -> diary E2E

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Barcode E2E complete
- [ ] Status -> Phase 09

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
