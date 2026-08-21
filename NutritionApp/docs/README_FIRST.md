# Nutrition App — AI Execution Plan

Este paquete está diseñado para que una IA local con ~131k de contexto pueda desarrollar el proyecto sin cargar toda la documentación ni todo el código en cada sesión.

## Regla principal de contexto

En una sesión normal leer únicamente:

1. `README_FIRST.md`
2. `PROJECT_STATUS.md`
3. `00_AI_OPERATING_PROTOCOL.md`
4. El archivo de la fase actual en `/phases`
5. Los documentos que esa fase indique en **Read for this phase**
6. Los archivos de código directamente involucrados en la tarea actual

**No cargar todos los documentos ni todo el repositorio a la vez.**

## Producto

Aplicación Android nativa para nutrición y conteo de calorías, con:

- diario de comidas;
- calorías, macros y micronutrientes;
- búsqueda de alimentos;
- favoritos y recientes;
- códigos de barras;
- foto de comida con IA;
- OCR de etiquetas;
- registro por texto y voz;
- agua;
- peso y progreso;
- Health Connect;
- recetas;
- estadísticas;
- operación offline-first;
- alimentos dominicanos;
- administración web;
- suscripciones premium.

## Stack canónico

### Android
Kotlin, Jetpack Compose, Material 3, Navigation 3, Hilt, Coroutines/Flow, Room, DataStore, Retrofit, OkHttp, Kotlinx Serialization, CameraX, ML Kit Barcode, ML Kit Text Recognition, Health Connect, WorkManager, Coil, Credential Manager, FCM, Crashlytics y Google Play Billing.

### Backend
Laravel 13, PHP 8.4, MySQL 8.4 LTS, Sanctum, Laravel Queue, Redis/Horizon cuando sea necesario, almacenamiento S3-compatible/Cloudflare R2, Livewire + Alpine.js + Tailwind para administración.

### Datos e IA
Base nutricional propia + USDA FoodData Central + Open Food Facts. OpenAI Responses API detrás de un AI Gateway del backend. Nunca colocar claves de IA en Android.

## Orden de ejecución

| Fase | Resultado |
|---|---|
| 00 | Bootstrap, repositorios, reglas y CI |
| 01 | Backend base + autenticación |
| 02 | Android base + autenticación |
| 03 | Onboarding + objetivos |
| 04 | Catálogo nutricional + motor |
| 05 | Búsqueda + favoritos + recientes |
| 06 | Diario + dashboard |
| 07 | Offline-first + sync |
| 08 | Barcode |
| 09 | Foto IA |
| 10 | OCR etiqueta |
| 11 | Texto + voz |
| 12 | Health Connect |
| 13 | Agua + peso + estadísticas |
| 14 | Recetas |
| 15 | Admin web |
| 16 | Billing + premium |
| 17 | Hardening + release |

## Definition of Done

Una tarea solo se marca `[x]` cuando:

- está implementada;
- compila;
- sus pruebas requeridas pasan;
- no rompe contratos existentes;
- lint/calidad pasa;
- documentación afectada está actualizada;
- `PROJECT_STATUS.md` está actualizado;
- `CHANGELOG.md` registra cambios de comportamiento/código;
- decisiones de largo plazo se registran en `DECISIONS.md`.

## Archivos de control

- `PROJECT_STATUS.md`: punto único de continuación.
- `CHANGELOG.md`: cambios ejecutados.
- `DECISIONS.md`: decisiones arquitectónicas permanentes.
- `PHASE_INDEX.md`: mapa de fases.
- `LOCAL_AI_START_PROMPT.md`: prompt para iniciar una sesión nueva.
