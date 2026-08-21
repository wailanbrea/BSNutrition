# Nutrition App — Android

Aplicación Android nativa (Kotlin + Jetpack Compose) para nutrición y conteo de calorías.

Este repositorio contiene solo la capa Android. El backend vive en
[`../nutrition-backend`](../nutrition-backend).

## Punto de continuación

El estado único del proyecto está en
[`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md). Antes de trabajar en esta
aplicación, leer:

1. `docs/README_FIRST.md`
2. `docs/PROJECT_STATUS.md`
3. `docs/00_AI_OPERATING_PROTOCOL.md`
4. La fase actual en `docs/phases/`

## Stack

Kotlin, Jetpack Compose, Material 3, Navigation 3, Hilt, Coroutines/Flow, Room,
DataStore, Retrofit, OkHttp, Kotlinx Serialization, CameraX, ML Kit, Health
Connect, WorkManager, Coil, Credential Manager, FCM, Crashlytics, Google Play
Billing. Ver `docs/02_TECH_STACK.md`.

## Reglas

- Sin secretos de servidor en el APK (ADR-005).
- Diario offline-first: UI → Room → SyncQueue → WorkManager → API (ADR-004).
- La IA solo detecta/estima; los valores nutricionales autoritativos los
  calcula el Nutrition Engine del backend (ADR-006).
