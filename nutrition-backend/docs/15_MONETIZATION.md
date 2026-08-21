# Monetization

## Concepto
Free / Pro / Pro AI.

Los precios se deciden después; la arquitectura usa entitlements.

## Backend authority
Android envía purchase token. Backend verifica con Google y calcula entitlement.

No confiar en `isPremium=true` local.

## Estados
- active;
- grace;
- pending;
- expired;
- revoked/refunded.

## Quotas
AI quota server-side con usage tracking.

## Feature gating
UI puede ocultar/mostrar, pero backend también debe verificar entitlement.
