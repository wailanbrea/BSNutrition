# Phase 01 — Backend Foundation and Authentication

## Goal
Crear Laravel API, MySQL, errores JSON, Sanctum y perfil.

## Read for this phase
- `07_BACKEND_ARCHITECTURE.md`
- `05_API_CONTRACT.md`
- `04_DATABASE_DESIGN.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [x] Phase 00 complete
- [x] PHP/Composer/MySQL available

## Tasks

### [x] PH01-T01 — Laravel baseline
**Depends on:** None

**Implementation checklist:**
- [x] Initialize Laravel 11/13
- [x] PHP 8.2+
- [x] MySQL (bsnutrition)
- [x] /api/v1 prefix
- [x] Pest/Pint configured
- [x] health/version endpoint (/api/v1/health)

**Acceptance criteria:**
- App boot
- DB works
- JSON health endpoint

**Tests / verification:**
- Backend test suite (Pest)
- DB smoke (migrations on MySQL)

### [x] PH01-T02 — API error contract
**Depends on:** None

**Implementation checklist:**
- [x] Validation errors (422 con formato estándar y fields)
- [x] 401/404/405/429 estructurados en JSON
- [x] Production exception mapping (500 SERVER_ERROR)
- [x] Middleware ForceJsonResponse para garantizar JSON sin HTML

**Acceptance criteria:**
- Android never receives arbitrary HTML errors

**Tests / verification:**
- Feature tests (`tests/Feature/ApiErrorContractTest.php`)

### [ ] PH01-T03 — Sanctum auth
**Depends on:** None

**Implementation checklist:**
- [ ] Register
- [ ] Login
- [ ] Logout
- [ ] Me
- [ ] Rate limits
- [ ] Per-device token

**Acceptance criteria:**
- Bearer auth works
- Logout revokes token

**Tests / verification:**
- Auth feature tests

### [ ] PH01-T04 — Profile API
**Depends on:** None

**Implementation checklist:**
- [ ] Migration/model
- [ ] GET/PUT
- [ ] Validation
- [ ] Ownership

**Acceptance criteria:**
- User can only access own profile

**Tests / verification:**
- Profile/authorization tests

## Phase exit criteria
- [ ] Backend auth/profile green
- [ ] Status -> Phase 02

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
