# Phase 00 — Bootstrap and Governance

## Goal
Crear bases de repositorios, documentación, convenciones y CI para que múltiples agentes puedan continuar sin perder estado.

## Read for this phase
- `README_FIRST.md`
- `00_AI_OPERATING_PROTOCOL.md`
- `02_TECH_STACK.md`
- `DECISIONS.md`

## Entry criteria
- [x] Planning package available
- [x] Git available

## Tasks

### [x] PH00-T01 — Create project roots and baseline docs
**Depends on:** None

**Implementation checklist:**
- [x] Crear repo Android
- [x] Crear repo backend
- [x] Copiar/integrar docs AI
- [x] Crear .gitignore
- [x] README de cada repo apunta al status

**Acceptance criteria:**
- Repos versionados
- Docs accesibles
- Sin secretos

**Tests / verification:**
- git status/estructura verificada

### [x] PH00-T02 — Define branch/commit conventions
**Depends on:** None

**Implementation checklist:**
- [x] Definir main/protected strategy
- [x] Branch names
- [x] Task IDs en commits
- [x] Merge gate

**Acceptance criteria:**
- Otro agente sabe cómo versionar cambios

**Tests / verification:**
- Build/tests relevantes deben pasar

### [x] PH00-T03 — Environment templates
**Depends on:** None

**Implementation checklist:**
- [x] Backend .env.example
- [x] Android environment/base URL strategy
- [x] Dev/staging/prod
- [x] Placeholders provider

**Acceptance criteria:**
- Config requerida documentada sin secretos

**Tests / verification:**
- Build/tests relevantes deben pasar

### [x] PH00-T04 — CI skeleton
**Depends on:** None

**Implementation checklist:**
- [x] Workflow Android
- [x] Workflow backend
- [x] Build/test commands

**Acceptance criteria:**
- CI sintácticamente válida

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [x] Bootstrap completo
- [x] Status apunta a Phase 01

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint
