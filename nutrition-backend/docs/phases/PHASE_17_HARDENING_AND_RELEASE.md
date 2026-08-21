# Phase 17 — Hardening and Release

## Goal
Ejecutar seguridad, QA, performance, observabilidad y release.

## Read for this phase
- `12_TESTING_QUALITY_GATES.md`
- `13_DEVOPS_DEPLOYMENT.md`
- `11_SECURITY_PRIVACY.md`
- `16_RELEASE_ROADMAP.md`

## Entry criteria
- [ ] All required MVP phases complete

## Tasks

### [ ] PH17-T01 — Security review
**Depends on:** None

**Implementation checklist:**
- [ ] Auth/IDOR
- [ ] Rate limit
- [ ] Upload
- [ ] Secret scan
- [ ] Logs
- [ ] Admin

**Acceptance criteria:**
- No critical/high known

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH17-T02 — Android QA matrix
**Depends on:** None

**Implementation checklist:**
- [ ] Android versions
- [ ] Permissions
- [ ] Offline
- [ ] Process restart
- [ ] Network
- [ ] Camera
- [ ] Accessibility

**Acceptance criteria:**
- Critical flows pass

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH17-T03 — Backend performance
**Depends on:** None

**Implementation checklist:**
- [ ] Search
- [ ] Diary
- [ ] Sync
- [ ] AI queue
- [ ] Indexes
- [ ] Backup restore

**Acceptance criteria:**
- No obvious production blocker

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH17-T04 — Observability
**Depends on:** None

**Implementation checklist:**
- [ ] Crashlytics
- [ ] API
- [ ] Queue
- [ ] AI
- [ ] Sync
- [ ] Alerts

**Acceptance criteria:**
- Failures diagnosable

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH17-T05 — Play release preparation
**Depends on:** None

**Implementation checklist:**
- [ ] Signing
- [ ] R8
- [ ] Privacy/Data Safety
- [ ] Listing
- [ ] Internal/closed test

**Acceptance criteria:**
- Release candidate accepted

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH17-T06 — Tag/close MVP
**Depends on:** None

**Implementation checklist:**
- [ ] Version/tag
- [ ] Changelog
- [ ] Known issues
- [ ] Monitoring
- [ ] Status

**Acceptance criteria:**
- Reproducible documented release

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] MVP release candidate ready
- [ ] Project status accurate

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
