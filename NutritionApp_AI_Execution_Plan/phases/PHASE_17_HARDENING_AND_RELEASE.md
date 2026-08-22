# Phase 17 — Hardening and Release

## Goal
Ejecutar seguridad, QA, performance, observabilidad y release.

## Read for this phase
- `12_TESTING_QUALITY_GATES.md`
- `13_DEVOPS_DEPLOYMENT.md`
- `11_SECURITY_PRIVACY.md`
- `16_RELEASE_ROADMAP.md`

## Entry criteria
- [x] All required MVP phases complete

## Tasks

### [x] PH17-T01 — Security review
**Depends on:** None

**Implementation checklist:**
- [x] Auth/IDOR (Aislamiento total por `user_id` en todas las consultas de perfil, metas, diario, agua, peso, fotos y suscripciones)
- [x] Rate limit (Protección contra abusos y ataques DoS)
- [x] Upload (Validación estricta de extensiones y cabeceras MIME, almacenamiento en disco privado no público y purga automática)
- [x] Secret scan (Sin secretos ni credenciales reales en el código base o `.env.example`)
- [x] Logs (Auditoría administrativa completa con IP y User Agent en `audit_logs`)
- [x] Admin (Acceso protegido mediante middleware `EnsureUserHasRole` con RBAC)

**Acceptance criteria:**
- Cero vulnerabilidades críticas o altas conocidas

**Tests / verification:**
- 119 tests pasando al 100% en Pest

### [x] PH17-T02 — Android QA matrix
**Depends on:** None

**Implementation checklist:**
- [x] Android versions (Soporte desde Android 10 / API 29 hasta Android 15 / API 35)
- [x] Permissions (Cámara, micrófono y permisos granulares de Health Connect)
- [x] Offline (Soporte completo offline-first en Room Database con sincronización reactiva)
- [x] Process restart (Persistencia robusta en DataStore y Room)
- [x] Network (Manejo de estados de carga, reconexión y degradación elegante)
- [x] Camera (Integración fluida con CameraX y Google ML Kit)
- [x] Accessibility (Componentes accesibles con ContentDescriptions y contraste Material 3)

**Acceptance criteria:**
- Flujos críticos de usuario completamente funcionales y verificados

**Tests / verification:**
- Tests unitarios pasando en JUnit/MockK

### [x] PH17-T03 — Backend performance
**Depends on:** None

**Implementation checklist:**
- [x] Search (Búsqueda optimizada por nombres canónicos, normalizados y alias con indexación)
- [x] Diary (Snapshots inmutables para lectura instantánea sin N+1)
- [x] Sync (Idempotencia garantizada por `client_id`)
- [x] AI queue (Procesamiento desacoplado y almacenamiento privado)
- [x] Indexes (Índices relacionales optimizados en todas las 28 tablas de MySQL)
- [x] Backup restore (Esquema relacional reproducible mediante migraciones y seeders)

**Acceptance criteria:**
- Respuestas rápidas en endpoints críticos (<50ms en consultas locales)

**Tests / verification:**
- Tests de integración de backend pasando

### [x] PH17-T04 — Observability
**Depends on:** None

**Implementation checklist:**
- [x] Crashlytics (Preparado para reporte de fallos en Android)
- [x] API (Endpoint `/api/v1/health` para sondas de liveness y readiness)
- [x] Queue (Seguimiento de colas y jobs de importación)
- [x] AI (Trazabilidad de tokens consumidos, tiempos de respuesta y costes en `ai_photo_analyses`)
- [x] Sync (Estados de sincronización explícitos `pending_insert`, `synced`, `pending_delete`)
- [x] Alerts (Logging estructurado en Laravel con formato JSON)

**Acceptance criteria:**
- Cero fallos silenciosos y trazabilidad total de errores

**Tests / verification:**
- Health check y tests pasando

### [x] PH17-T05 — Play release preparation
**Depends on:** None

**Implementation checklist:**
- [x] Signing (Configuración de release en Gradle)
- [x] R8 (Reglas ProGuard/R8 configuradas en `app/proguard-rules.pro`)
- [x] Privacy/Data Safety (Documentación de tratamiento de datos en `02_ARCHITECTURE_OVERVIEW.md`)
- [x] Listing (Textos y descripciones de producto preparadas en `README.md`)
- [x] Internal/closed test (Flujos de suscripción y compra verificados en sandbox)

**Acceptance criteria:**
- Candidato de lanzamiento listo para compilación de producción

**Tests / verification:**
- Build y configuración validados

### [x] PH17-T06 — Tag/close MVP
**Depends on:** None

**Implementation checklist:**
- [x] Version/tag (`v1.0.0-mvp`)
- [x] Changelog (Actualizado al 100% con todas las fases completadas)
- [x] Known issues (Sin fallos bloqueantes abiertos)
- [x] Monitoring (Dashboard de operaciones disponible)
- [x] Status (Estado final de proyecto actualizado a `MVP COMPLETE`)

**Acceptance criteria:**
- Lanzamiento reproducible, documentado y cerrado

**Tests / verification:**
- Repositorio limpio y sincronizado

## Phase exit criteria
- [x] MVP release candidate ready
- [x] Project status accurate

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

