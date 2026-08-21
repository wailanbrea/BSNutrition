# Phase 05 — Food Search, Favorites and Recents

## Goal
Crear búsqueda rápida y métodos de reuso.

## Read for this phase
- `04_DATABASE_DESIGN.md`
- `05_API_CONTRACT.md`
- `08_NUTRITION_ENGINE.md`

## Entry criteria
- [x] Catalog ready

## Tasks

### [x] PH05-T01 — Backend food search
**Depends on:** None

**Implementation checklist:**
- [x] Normalization (búsqueda normalizada por término, sin distinción de mayúsculas ni acentos)
- [x] Exact/alias (búsqueda por nombre canónico y términos coloquiales en `food_aliases`)
- [x] Brand (búsqueda por marca asociada en `food_brands`)
- [x] Locale boost (priorización de alimentos de República Dominicana `country_code = 'DO'` y verificados)
- [x] Pagination (paginación configurable con `per_page`)
- [x] Index review (índices en `normalized_name`, `normalized_alias`, `barcode`, `country_code`, `category_id`)
- [x] Endpoints REST: `GET /api/v1/foods/search`, `GET /api/v1/foods/{id}`, `GET /api/v1/foods/barcode/{barcode}`, `POST /api/v1/foods/{id}/calculate`

**Acceptance criteria:**
- Resultados de búsqueda relevantes, rápidos y estables con desglose nutricional por 100g y detalle completo

**Tests / verification:**
- Tests en Pest (`FoodSearchApiTest.php`) con 53 tests pasando al 100% (664 aserciones)

### [x] PH05-T02 — Android search
**Depends on:** None

**Implementation checklist:**
- [x] Debounce (Debouncing reactivo de 300ms en `_queryFlow` para evitar saturación de peticiones)
- [x] States (`SearchUiState`: Query, Categorías, Loading, Resultados, Detalle, Porciones, Cálculo)
- [x] Results (Listado optimizado en `SearchScreen` con badge dominicano `🇩🇴 RD` y resumen de macros)
- [x] Food detail (`FoodDetailSheet` modal con desglose nutricional, micronutrientes y acciones)
- [x] Portion (Selector de medidas caseras y gramos con recálculo dinámico en tiempo real)

**Acceptance criteria:**
- Búsqueda reactiva fluida, visualización de macros y selector interactivo de porciones sin tormentas de peticiones

**Tests / verification:**
- Tests unitarios en Android (`FoodRepositoryTest.kt` y `SearchViewModelTest.kt`)

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
