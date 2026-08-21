# Decisions / ADR

## ADR-001 — Native Android
**Status:** Accepted  
Kotlin + Jetpack Compose.

## ADR-002 — Backend
**Status:** Accepted  
Laravel modular monolith.

## ADR-003 — Database
**Status:** Accepted  
MySQL 8.4 LTS.

## ADR-004 — Offline-first
**Status:** Accepted  
Diary writes go to Room first and sync later.

## ADR-005 — AI gateway
**Status:** Accepted  
Android never stores provider secrets; calls Laravel.

## ADR-006 — Nutrition authority
**Status:** Accepted  
AI identifies/estimates; Nutrition Engine calculates canonical nutrient values.

## ADR-007 — Repository layout
**Status:** Accepted (Phase 00)
Dos repos separados: `NutritionApp` (Android) y `nutrition-backend` (Laravel).
Los docs AI viven en `docs/` de ambos repos (copia espejo); el estado único
de continuación se mantiene sincronizado en ambos.

## ADR-008 — Branching & commits
**Status:** Accepted (Phase 00)
Conventional Commits + task ID en cada commit; `main` protegida; una rama por
tarea. Ver `docs/BRANCHING.md`.

## Pending ADR
- ULID vs UUID
- BMR/TDEE formula
- conflict rules
- image retention
- subscription tiers/pricing
