# Phase 16 — Billing and Premium

## Goal
Agregar Google Play billing, entitlements y quotas autoritativas.

## Read for this phase
- `15_MONETIZATION.md`
- `11_SECURITY_PRIVACY.md`

## Entry criteria
- [ ] Core product stable
- [ ] Play products configured

## Tasks

### [ ] PH16-T01 — Product/entitlement ADR
**Depends on:** None

**Implementation checklist:**
- [ ] Free/Pro/AI
- [ ] Product IDs
- [ ] Quotas
- [ ] Grace/expiry

**Acceptance criteria:**
- Rules documented server-side

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH16-T02 — Android BillingClient
**Depends on:** None

**Implementation checklist:**
- [ ] Products
- [ ] Purchase
- [ ] Restore/query
- [ ] Send token

**Acceptance criteria:**
- Token reaches backend

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH16-T03 — Backend verification
**Depends on:** None

**Implementation checklist:**
- [ ] Google verification
- [ ] Subscription state
- [ ] Entitlements
- [ ] Revalidation
- [ ] Refund

**Acceptance criteria:**
- Backend authoritative

**Tests / verification:**
- Build/tests relevantes deben pasar

### [ ] PH16-T04 — AI quota enforcement
**Depends on:** None

**Implementation checklist:**
- [ ] Usage
- [ ] Atomic check
- [ ] Limits
- [ ] Exhausted response

**Acceptance criteria:**
- Cannot bypass via local state

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Billing test environment validated
- [ ] Status -> Phase 17

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
