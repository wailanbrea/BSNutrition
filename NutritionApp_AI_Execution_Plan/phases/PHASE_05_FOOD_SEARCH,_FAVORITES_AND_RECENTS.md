# Phase 05 — Food Search, Favorites and Recents

## Goal
Crear búsqueda rápida y métodos de reuso.

## Read for this phase
- `04_DATABASE_DESIGN.md`
- `05_API_CONTRACT.md`
- `08_NUTRITION_ENGINE.md`

## Entry criteria
- [ ] Catalog ready

## Tasks

### [ ] PH05-T01 — Backend food search
**Depends on:** None

**Implementation checklist:**
- [ ] Normalization
- [ ] Exact/alias
- [ ] Brand
- [ ] Locale boost
- [ ] Pagination
- [ ] Index review

**Acceptance criteria:**
- Relevant stable results

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH05-T02 — Android search
**Depends on:** None

**Implementation checklist:**
- [ ] Debounce
- [ ] States
- [ ] Results
- [ ] Food detail
- [ ] Portion

**Acceptance criteria:**
- Fast search without request storm

**Tests / verification:**
- ViewModel/MockWebServer

### [ ] PH05-T03 — Favorites
**Depends on:** None

**Implementation checklist:**
- [ ] API
- [ ] Room/cache
- [ ] Toggle/list

**Acceptance criteria:**
- Persists/reconciles

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH05-T04 — Recents
**Depends on:** None

**Implementation checklist:**
- [ ] History query
- [ ] Local section
- [ ] Ranking

**Acceptance criteria:**
- Recent foods easy to add

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Search usable
- [ ] Status -> Phase 06

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
