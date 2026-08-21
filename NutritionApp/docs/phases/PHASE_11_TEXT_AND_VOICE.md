# Phase 11 — Text and Voice

## Goal
Permitir quick logging en lenguaje natural reutilizando matching/review.

## Read for this phase
- `09_AI_PIPELINE.md`
- `05_API_CONTRACT.md`

## Entry criteria
- [ ] AI gateway/matcher stable

## Tasks

### [ ] PH11-T01 — Meal text parser
**Depends on:** None

**Implementation checklist:**
- [ ] Schema
- [ ] Quantity/unit
- [ ] Matching
- [ ] Confidence
- [ ] Quota

**Acceptance criteria:**
- Spanish sentence -> candidates

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH11-T02 — Android text quick-add
**Depends on:** None

**Implementation checklist:**
- [ ] Input
- [ ] Submit
- [ ] Reuse review
- [ ] Confirm

**Acceptance criteria:**
- Text -> diary E2E

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH11-T03 — Voice path
**Depends on:** None

**Implementation checklist:**
- [ ] ADR SpeechRecognizer/server transcription
- [ ] Permissions
- [ ] Transcript -> text parser

**Acceptance criteria:**
- Voice reuses same pipeline

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Text/voice complete
- [ ] Status -> Phase 12

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
