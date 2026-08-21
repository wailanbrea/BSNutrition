# Testing and Quality Gates

## Android
- compile;
- unit tests;
- lint;
- Room migration tests;
- MockWebServer integration;
- Compose UI tests para flujos críticos.

## Backend
- Pint;
- Pest;
- migration tests;
- API feature tests;
- authorization;
- queue/jobs.

## E2E críticos
1. register -> onboarding -> dashboard.
2. search -> portion -> diary -> totals.
3. offline add -> restart -> reconnect -> sync exactly once.
4. barcode -> product -> diary.
5. photo -> AI -> correction -> diary.
6. water/weight -> dashboard/stats.

## Merge gate
- comportamiento funciona;
- no secretos;
- tests pasan;
- documentación de contrato actualizada;
- status/changelog actualizados.
