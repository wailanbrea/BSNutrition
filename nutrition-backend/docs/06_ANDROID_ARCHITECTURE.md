# Android Architecture

## Módulos iniciales

No crear 30 módulos Gradle desde el primer día.

```text
:app
:core:model
:core:designsystem
:core:database
:core:network
:core:datastore
:core:common
:core:testing
```

Separar features gradualmente:

```text
:feature:auth
:feature:onboarding
:feature:home
:feature:diary
:feature:foodsearch
:feature:barcode
:feature:aifood
:feature:progress
...
```

## Flujo UI

`Screen -> UiAction -> ViewModel -> Domain/Repository -> Room/API`

Composable no llama Retrofit/DAO directamente.

## Estado

- UI state inmutable;
- `StateFlow`;
- lifecycle-aware collection;
- eventos one-shot controlados.

## Modelos

Separar cuando corresponda:
- API DTO;
- Room Entity;
- Domain Model;
- UI Model.

## Design System

Centralizar:
- colors;
- typography;
- spacing;
- shapes;
- buttons;
- cards;
- macro bars;
- calorie ring;
- nutrient progress;
- empty/loading/error states;
- sheets/dialogs.

## Network
- Retrofit/OkHttp;
- auth interceptor;
- typed error mapper;
- logs redacted;
- no logs sensibles en release.

## Room
Guardar:
- diarios/entries;
- cache alimentos;
- favoritos;
- water;
- weight;
- sync queue;
- metadata sync.

Toda migración Room debe tener prueba.

## DataStore
Solo preferencias pequeñas.

## Navegación
Single Activity + Navigation 3.

Top level:
- Hoy
- Diario
- Add
- Progreso
- Más

Quick Add:
- Buscar
- Barcode
- Foto
- Texto
- Voz
- Manual
