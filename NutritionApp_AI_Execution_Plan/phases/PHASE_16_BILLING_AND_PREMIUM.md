# Phase 16 — Billing and Premium

## Goal
Agregar Google Play billing, entitlements y quotas autoritativas.

## Read for this phase
- `15_MONETIZATION.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [x] Core product stable
- [x] Play products configured

## Tasks

### [x] PH16-T01 — Product/entitlement ADR
**Depends on:** None

**Implementation checklist:**
- [x] Free/Pro/AI (Definición de Free: 3 fotos/5 textos diarios vs Pro: ilimitado)
- [x] Product IDs (`bsnutrition_pro_monthly`, `bsnutrition_pro_yearly`)
- [x] Quotas (Límites atómicos y reseteo diario a medianoche)
- [x] Grace/expiry (Manejo de estados `active`, `in_grace_period`, `expired`, `canceled`)

**Acceptance criteria:**
- Reglas formales de monetización documentadas en ADR-012 en `DECISIONS.md`

**Tests / verification:**
- Build/tests relevantes deben pasar

### [x] PH16-T02 — Android BillingClient
**Depends on:** None

**Implementation checklist:**
- [x] Products (Listado de productos mensuales y anuales con descuentos)
- [x] Purchase (Flujo de compra en `PaywallScreen.kt` y `SubscriptionViewModel.kt`)
- [x] Restore/query (Función de restauración de compras)
- [x] Send token (Envío seguro de token al backend para validación criptográfica)

**Acceptance criteria:**
- El token de compra de Google Play se transmite y valida en el backend

**Tests / verification:**
- Tests en Kotlin (`SubscriptionRepositoryTest.kt`, `SubscriptionViewModelTest.kt`)

### [x] PH16-T03 — Backend verification
**Depends on:** None

**Implementation checklist:**
- [x] Google verification (Servicio `SubscriptionVerificationService.php` y endpoint `POST /api/v1/billing/verify-play-purchase`)
- [x] Subscription state (Persistencia en `user_subscriptions` con cálculo de expiración)
- [x] Entitlements (Resolución de permisos de acceso en `SubscriptionController.php`)
- [x] Revalidation (Mantenimiento autoritativo del estado de suscripción)
- [x] Refund (Actualización y revocación inmediata de permisos al expirar o reembolsar)

**Acceptance criteria:**
- El backend es la única fuente de verdad autoritativa para el estado Pro

**Tests / verification:**
- Tests en Pest (`SubscriptionApiTest.php`)

### [x] PH16-T04 — AI quota enforcement
**Depends on:** None

**Implementation checklist:**
- [x] Usage (Tabla `user_daily_ai_quotas` con conteo diario segregado por fotos y textos)
- [x] Atomic check (Bloqueo pesimista `lockForUpdate` en `AiQuotaService.php`)
- [x] Limits (3 fotos / 5 textos al día para cuentas Free, ilimitado para Pro)
- [x] Exhausted response (Excepción `QuotaExceededException` con HTTP 429 y código `AI_QUOTA_EXCEEDED`)

**Acceptance criteria:**
- Imposible evadir el límite diario desde el cliente o mediante concurrencia

**Tests / verification:**
- Tests en Pest (`SubscriptionApiTest.php`)

## Phase exit criteria
- [x] Billing test environment validated (Esquema de suscripciones, Play Billing Client UI, backend verification y control atómico de cuotas IA)
- [x] Status -> Phase 17

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

