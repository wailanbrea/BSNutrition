# System Architecture

## Vista general

```text
Android Kotlin/Compose
        |
        | HTTPS JSON
        v
Laravel 13 API
   |        |        |
 MySQL     R2      Queue
   |                 |
   |               Redis
   |
   +--> USDA
   +--> Open Food Facts
   +--> Nutrition Engine
   +--> AI Gateway --> OpenAI
```

## Android

```text
Compose
  -> ViewModel
  -> UseCase/Domain
  -> Repository
      -> Room
      -> REST API
```

Room será fuente inmediata de la UI para datos offline-capable.

## Backend

Monolito modular con dominios:

- Identity
- Profile
- NutritionGoals
- Foods
- Nutrition
- Diary
- Hydration
- Progress
- AI
- Health
- Recipes
- Billing
- Administration

## Autoridad de datos

### Servidor
- cuenta;
- suscripción;
- catálogo canónico;
- alimentos verificados;
- configuración IA;
- quotas.

### Local-first + sync
- diario;
- agua;
- peso;
- favoritos;
- ciertas preferencias.

## Imágenes

No guardar blobs en MySQL.

Guardar objeto privado en R2/S3 y metadata/object key en DB.

## Search

Primero MySQL + índices + aliases + ranking. Agregar Meilisearch únicamente si métricas demuestran necesidad.
