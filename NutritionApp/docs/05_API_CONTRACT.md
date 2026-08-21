# API Contract

Base: `/api/v1`

## Error estándar

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Mensaje",
    "fields": {}
  }
}
```

Nunca devolver HTML de excepción a Android.

## Auth
- POST `/auth/register`
- POST `/auth/login`
- POST `/auth/google`
- POST `/auth/logout`
- GET `/me`
- DELETE `/me`

## Profile/Goals
- GET/PUT `/profile`
- GET `/nutrition-goals/current`
- POST `/nutrition-goals/calculate`
- PUT `/nutrition-goals/current`

## Foods
- GET `/foods/search`
- GET `/foods/{id}`
- GET `/foods/barcode/{barcode}`
- POST `/foods/custom`
- GET `/foods/recent`
- GET `/foods/favorites`
- POST/DELETE favorite

## Diary
- GET `/diary/{date}`
- POST `/diary/{date}/entries`
- PUT/DELETE `/diary/entries/{id}`
- POST copy-meal/copy-day

## Water / Weight
CRUD con recursos versionados.

## AI
- POST `/ai/food-photo`
- GET `/ai/analyses/{id}`
- POST `/ai/analyses/{id}/confirm`
- POST `/ai/analyses/{id}/corrections`
- POST `/ai/meal-text`
- POST `/ai/nutrition-label`

## Sync
Inicial:
- POST `/sync/push`
- GET `/sync/pull?cursor=...`

Payloads limitados/paginados.

## Billing
- POST `/billing/google/verify`
- GET `/billing/entitlements`

Cada endpoint debe definir auth, validación, DTO/Resource, errores, paginación, rate limit y pruebas.
