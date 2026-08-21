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
**Status:** Accepted (Phase 00 / 01)
Estructura de Monorepo unificado (`BSNutrition`) que contiene `NutritionApp/` (Android nativo), `nutrition-backend/` (Laravel API) y `NutritionApp_AI_Execution_Plan/` (paquete de gobernanza y especificaciones).

## ADR-008 — Branching & commits
**Status:** Accepted (Phase 00)
Conventional Commits con task ID en cada commit (`<tipo>(<alcance>): <descripcion> [<task-id>]`); rama `main` protegida y una rama de trabajo por tarea.

## Pending ADR
- ULID vs UUID
- BMR/TDEE formula
- conflict rules
- image retention
- subscription tiers/pricing

