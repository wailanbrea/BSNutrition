# Phase 15 — Administration

## Goal
Crear herramientas internas de operación y curation.

## Read for this phase
- `14_ADMIN_PANEL.md`
- `11_SECURITY_PRIVACY.md`
- `04_DATABASE_DESIGN.md`

## Entry criteria
- [x] Food/AI domains ready

## Tasks

### [x] PH15-T01 — Admin auth/roles
**Depends on:** None

**Implementation checklist:**
- [x] Role (Campo `role` en `users` con métodos `isAdmin()`, `isCurator()` y `hasAnyRole()`)
- [x] Protected routes (Middleware `EnsureUserHasRole` con alias `role:admin,curator`)
- [x] Audit baseline (Migración `audit_logs`, modelo `AuditLog` y método estático `AuditLog::log()`)

**Acceptance criteria:**
- Acceso denegado con 403 para usuarios normales y permitido para administradores y curadores

**Tests / verification:**
- Tests en Pest (`AdminApiTest.php`)

### [x] PH15-T02 — Food management
**Depends on:** None

**Implementation checklist:**
- [x] CRUD (Controlador `AdminFoodController` con endpoints de listado, creación, edición y eliminación)
- [x] Nutrients (Inyección estructurada de nutrientes por porción y 100g)
- [x] Portions (Soporte de porciones personalizadas)
- [x] Aliases (Soporte de alias multilingües)
- [x] Barcodes (Asociación directa de códigos de barra EAN_13/UPC_A)
- [x] Verify (Endpoint `POST /api/v1/admin/foods/{id}/verify` para promoción a catálogo oficial)
- [x] Sources (Filtro por fuentes oficiales o comunitarias)

**Acceptance criteria:**
- Gestión y curación completa de alimentos sin necesidad de SQL manual

**Tests / verification:**
- Tests en Pest (`AdminApiTest.php`)

### [x] PH15-T03 — Dominican curation
**Depends on:** None

**Implementation checklist:**
- [x] Country queue (Endpoint `GET /api/v1/admin/curation/dominican-queue`)
- [x] Aliases (Endpoint `POST /api/v1/admin/curation/foods/{id}/aliases` para nombres criollos)
- [x] Preparation variants (Asociación con métodos de preparación dominicanos)
- [x] Verification (Endpoint `POST /api/v1/admin/curation/approve/{id}`)

**Acceptance criteria:**
- Flujo de trabajo sistemático y trazable para enriquecimiento del dataset dominicano

**Tests / verification:**
- Tests en Pest (`AdminApiTest.php`)

### [x] PH15-T04 — AI review
**Depends on:** None

**Implementation checklist:**
- [x] Analyses (Controlador `AdminAiReviewController`)
- [x] Low confidence (Filtro automático de análisis de foto con confianza < 85%)
- [x] Corrections (Resolución y corrección manual de items con `POST /api/v1/admin/ai/reviews/{id}/resolve`)
- [x] Failures (Monitoreo de estado de inferencia y metadatos)
- [x] Cost (Seguimiento de costos y tokens consumidos)

**Acceptance criteria:**
- Calidad, calibración y costes de IA 100% observables

**Tests / verification:**
- Tests en Pest (`AdminApiTest.php`)

### [x] PH15-T05 — Operations dashboard
**Depends on:** None

**Implementation checklist:**
- [x] Users (Métricas de usuarios totales, activos hoy y nuevos esta semana)
- [x] Foods (Conteo de alimentos totales, dominicanos, verificados y no verificados)
- [x] AI (Total de análisis fotográficos y en cola de baja confianza)
- [x] Jobs & Activity (Conteo de registros de comidas, agua y peso)
- [x] Subscription hooks (Estructura lista para métricas de planes premium)

**Acceptance criteria:**
- Resumen operacional interactivo completo y en tiempo real

**Tests / verification:**
- Tests en Pest (`AdminApiTest.php`)

## Phase exit criteria
- [x] Admin complete (Roles, auditoría, curación de catálogo, dataset dominicano, cola de revisión de IA y dashboard)
- [x] Status -> Phase 16

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

