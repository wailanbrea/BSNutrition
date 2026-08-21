# Phase 02 — Android Foundation and Authentication

## Goal
Crear app Kotlin/Compose, arquitectura core, red, almacenamiento y login.

## Read for this phase
- `06_ANDROID_ARCHITECTURE.md`
- `02_TECH_STACK.md`
- `05_API_CONTRACT.md`

## Entry criteria
- [ ] Backend auth ready
- [ ] Android SDK/JDK ready

## Tasks

### [ ] PH02-T01 — Android project/version catalog
**Depends on:** None

**Implementation checklist:**
- [ ] Create app
- [ ] Compose/Material3
- [ ] libs.versions.toml
- [ ] Debug/release

**Acceptance criteria:**
- Build/install works

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH02-T02 — Core architecture
**Depends on:** None

**Implementation checklist:**
- [ ] Hilt
- [ ] designsystem
- [ ] model
- [ ] network
- [ ] database
- [ ] datastore
- [ ] testing

**Acceptance criteria:**
- Modules/dependencies compile

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH02-T03 — Network layer
**Depends on:** None

**Implementation checklist:**
- [ ] Retrofit/OkHttp
- [ ] Serialization
- [ ] Auth interceptor
- [ ] Typed errors
- [ ] Safe logging

**Acceptance criteria:**
- Health endpoint works

**Tests / verification:**
- MockWebServer

### [ ] PH02-T04 — Auth UI/repository
**Depends on:** None

**Implementation checklist:**
- [ ] Register
- [ ] Login
- [ ] Token persistence
- [ ] Session restore
- [ ] Logout

**Acceptance criteria:**
- E2E auth works

**Tests / verification:**
- ViewModel/repository/UI tests

### [ ] PH02-T05 — Navigation shell
**Depends on:** None

**Implementation checklist:**
- [ ] Single Activity
- [ ] Navigation 3
- [ ] Auth/app graphs
- [ ] Top-level placeholders

**Acceptance criteria:**
- Stable login/logout navigation

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Android auth working
- [ ] Status -> Phase 03

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
