# Phase 09 — AI Food Photo

## Goal
Crear foto IA segura, structured output, matching, correction y diary.

## Read for this phase
- `09_AI_PIPELINE.md`
- `11_SECURITY_PRIVACY.md`
- `08_NUTRITION_ENGINE.md`
- `05_API_CONTRACT.md`

## Entry criteria
- [x] Diary/catalog/barcode stable (Fases 06, 07 y 08 completadas)
- [x] AI/storage credentials available

## Tasks

### [x] PH09-T01 — Private image storage
**Depends on:** None

**Implementation checklist:**
- [x] Metadata (Tabla y modelo `AiImageUpload` con `mime_type`, `file_size_bytes`, `status`, `expires_at`)
- [x] Signed/private upload (`ImageStorageService::storePrivateUpload` en disco privado con UUID)
- [x] Validation (Whitelist de formatos JPEG/PNG/WEBP/HEIC y límite de 10 MB)
- [x] Delete (`ImageStorageService::deleteUpload` y purga periódica con `cleanupExpiredUploads`)

**Acceptance criteria:**
- Las imágenes de comida se guardan exclusivamente en almacenamiento privado, sin URLs públicas y con ciclo de vida/retención configurable

**Tests / verification:**
- Tests en Pest (`ImageStorageServiceTest.php`, 5 tests pasando)


### [x] PH09-T02 — AI provider abstraction
**Depends on:** None

**Implementation checklist:**
- [x] Interface (`AiVisionProviderInterface`)
- [x] OpenAI provider (`OpenAiVisionProvider` con soporte de modelos multimodal GPT-4o / GPT-4o-mini)
- [x] JSON schema (Contrato estructurado con `dish_name`, `summary`, `items`, `confidence_score` y métodos de preparación)
- [x] Timeout/errors (Manejo robusto de caídas de red, excepciones HTTP y descarte de respuestas inválidas)
- [x] Usage/cost (Conteo de tokens de entrada/salida y estimación de coste en USD)

**Acceptance criteria:**
- Proveedor de IA completamente desacoplado y protegido contra respuestas mal estructuradas

**Tests / verification:**
- Tests en Pest (`AiVisionProviderTest.php`, 4 tests pasando)


### [x] PH09-T03 — Food matcher v1
**Depends on:** None

**Implementation checklist:**
- [x] Normalization (Limpieza de acentos, minúsculas y caracteres especiales en `FoodMatchingService::normalize`)
- [x] Aliases (Búsqueda en tabla `food_aliases` para nombres populares dominicanos)
- [x] Locale (Ponderación favorable `+0.05` para platos dominicanos `country_code = 'DO'`)
- [x] Preparation (Alineación y coincidencia de métodos de cocción frito, guisado, asado, hervido)
- [x] Scoring (Algoritmo de puntuación de similitud léxica y de tokens)
- [x] Threshold (Umbral configurable `>=0.70` para match directo y sugerencia ordenada de candidatos alternativos)

**Acceptance criteria:**
- Vinculación determinista a alimentos canónicos o provisión de candidatos ordenados por relevancia

**Tests / verification:**
- Tests en Pest (`FoodMatchingServiceTest.php`, 5 tests pasando)


### [x] PH09-T04 — Analysis orchestration
**Depends on:** None

**Implementation checklist:**
- [x] Analysis lifecycle (Endpoints `POST /api/v1/ai/photo/analyze`, `GET /api/v1/ai/photo/analyses/{id}`, `POST /api/v1/ai/photo/analyses/{id}/confirm`)
- [x] AI call (Invocación de `AiVisionManager` con auditoría de tokens de entrada/salida y coste estimado en USD)
- [x] Items (Persistencia en tablas `ai_photo_analyses` y `ai_photo_analysis_items` con candidatos y confianza)
- [x] Match (Emparejamiento determinista de ingredientes reconocidos con el catálogo canónico si score >= 0.70)
- [x] Nutrition (Cálculo de nutrición por peso usando tablas canónicas oficiales o fallback a estimación por visión)

**Acceptance criteria:**
- Nutrición auditable y determinista con soporte para confirmación y registro en bloque en el diario

**Tests / verification:**
- Tests en Pest (`AiPhotoApiTest.php`, 4 tests pasando; suite completa 93 tests pasando)


### [ ] PH09-T05 — Android capture/upload
**Depends on:** None

**Implementation checklist:**
- [ ] Camera
- [ ] Gallery
- [ ] Resize/compress
- [ ] Progress
- [ ] Retry

**Acceptance criteria:**
- Image within budget

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH09-T06 — Review/correction UI
**Depends on:** None

**Implementation checklist:**
- [ ] Items
- [ ] Confidence
- [ ] Change food/grams
- [ ] Add/remove
- [ ] Confirm

**Acceptance criteria:**
- User controls final diary data

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH09-T07 — Feedback/E2E
**Depends on:** None

**Implementation checklist:**
- [ ] Corrections
- [ ] Diary snapshots
- [ ] Source
- [ ] Evaluation logs

**Acceptance criteria:**
- Photo -> review -> diary complete

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] AI photo stable
- [ ] Status -> Phase 10

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
