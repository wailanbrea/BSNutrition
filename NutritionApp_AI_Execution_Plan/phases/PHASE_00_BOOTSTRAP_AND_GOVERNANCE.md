# Phase 00 — Bootstrap and Governance

## Goal
Crear bases de repositorios, documentación, convenciones y CI para que múltiples agentes puedan continuar sin perder estado.

## Read for this phase
- `README_FIRST.md`
- `00_AI_OPERATING_PROTOCOL.md`
- `02_TECH_STACK.md`
- `DECISIONS.md`

## Entry criteria
- [ ] Planning package available
- [ ] Git available

## Tasks

### [ ] PH00-T01 — Create project roots and baseline docs
**Depends on:** None

**Implementation checklist:**
- [ ] Crear repo Android
- [ ] Crear repo backend
- [ ] Copiar/integrar docs AI
- [ ] Crear .gitignore
- [ ] README de cada repo apunta al status

**Acceptance criteria:**
- Repos versionados
- Docs accesibles
- Sin secretos

**Tests / verification:**
- git status/estructura verificada

### [ ] PH00-T02 — Define branch/commit conventions
**Depends on:** None

**Implementation checklist:**
- [ ] Definir main/protected strategy
- [ ] Branch names
- [ ] Task IDs en commits
- [ ] Merge gate

**Acceptance criteria:**
- Otro agente sabe cómo versionar cambios

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH00-T03 — Environment templates
**Depends on:** None

**Implementation checklist:**
- [ ] Backend .env.example
- [ ] Android environment/base URL strategy
- [ ] Dev/staging/prod
- [ ] Placeholders provider

**Acceptance criteria:**
- Config requerida documentada sin secretos

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH00-T04 — CI skeleton
**Depends on:** None

**Implementation checklist:**
- [ ] Workflow Android
- [ ] Workflow backend
- [ ] Build/test commands

**Acceptance criteria:**
- CI sintácticamente válida

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Bootstrap completo
- [ ] Status apunta a Phase 01

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
