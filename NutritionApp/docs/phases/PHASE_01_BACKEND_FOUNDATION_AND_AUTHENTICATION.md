# Phase 01 — Backend Foundation and Authentication

## Goal
Crear Laravel API, MySQL, errores JSON, Sanctum y perfil.

## Read for this phase
- `07_BACKEND_ARCHITECTURE.md`
- `05_API_CONTRACT.md`
- `04_DATABASE_DESIGN.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [ ] Phase 00 complete
- [ ] PHP/Composer/MySQL available

## Tasks

### [ ] PH01-T01 — Laravel baseline
**Depends on:** None

**Implementation checklist:**
- [ ] Initialize Laravel 13
- [ ] PHP 8.4
- [ ] MySQL
- [ ] /api/v1
- [ ] Pest/Pint
- [ ] health/version endpoint

**Acceptance criteria:**
- App boot
- DB works
- JSON health endpoint

**Tests / verification:**
- Backend test suite
- DB smoke

### [ ] PH01-T02 — API error contract
**Depends on:** None

**Implementation checklist:**
- [ ] Validation errors
- [ ] 401/404
- [ ] Production exception mapping

**Acceptance criteria:**
- Android never receives arbitrary HTML errors

**Tests / verification:**
- Feature tests

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
