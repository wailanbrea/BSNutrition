# Phase 11 — Text and Voice

## Goal
Permitir quick logging en lenguaje natural reutilizando matching/review.

## Read for this phase
- `09_AI_PIPELINE.md`
- `05_API_CONTRACT.md`

## Entry criteria
- [x] AI gateway/matcher stable

## Tasks

### [x] PH11-T01 — Meal text parser
**Depends on:** None

**Implementation checklist:**
- [x] Schema (Estructura de respuesta unificada con `AiPhotoAnalysisDataDto`)
- [x] Quantity/unit (Detección de unidades culinarias: taza, gramos, porción, unidades)
- [x] Matching (Vinculación determinista con el catálogo canónico mediante `FoodMatchingService`)
- [x] Confidence (Puntuación de certeza y preservación de candidatos alternativos)
- [x] Quota (Estimación de tokens y coste en USD)

**Acceptance criteria:**
- Oraciones en lenguaje natural en español -> Alimentos reconocidos con desglose nutricional

**Tests / verification:**
- Tests en Pest (`AiTextParserServiceTest.php`, `AiTextVoiceApiTest.php`)

### [x] PH11-T02 — Android text quick-add
**Depends on:** None

**Implementation checklist:**
- [x] Input (Área de texto multilinea con chips de sugerencias rápidas)
- [x] Submit (Envío al endpoint `POST /api/v1/ai/text/parse`)
- [x] Reuse review (Reutilización de pantalla interactiva con controles deslizantes y cambio de candidatos)
- [x] Confirm (Registro directo en el diario en `POST /api/v1/ai/text/confirm/{id}`)

**Acceptance criteria:**
- Flujo Texto -> Revisión -> Diario E2E completo

**Tests / verification:**
- Tests en Kotlin (`TextVoiceLoggingViewModelTest.kt`)

### [x] PH11-T03 — Voice path
**Depends on:** None

**Implementation checklist:**
- [x] ADR SpeechRecognizer/server transcription (Integración de Android `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`)
- [x] Permissions (Control de audio y captura de voz)
- [x] Transcript -> text parser (Transcripción local redirigida automáticamente al pipeline de texto AI)

**Acceptance criteria:**
- El canal de voz reutiliza exactamente el mismo pipeline del backend y la misma UI de revisión

**Tests / verification:**
- Tests en Kotlin (`TextVoiceLoggingViewModelTest.kt`) y Pest (`AiTextVoiceApiTest.php`)

## Phase exit criteria
- [x] Text/voice complete (Backend NLP + UI Compose con entrada de texto, dictado por voz y confirmación en diario)
- [x] Status -> Phase 12

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

