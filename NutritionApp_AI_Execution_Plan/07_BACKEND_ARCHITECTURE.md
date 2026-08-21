# Backend Architecture

## Estilo
Laravel modular monolith.

Flujo recomendado:

`Controller -> FormRequest -> Action/Application Service -> Domain -> Eloquent -> API Resource`

Evitar fat controllers.

## Dominios
- Identity
- Profile
- Goals
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

## Auth
Laravel Sanctum:
- token por dispositivo;
- revocable;
- logout revoca token actual;
- eliminación cuenta revoca todos.

## Seguridad
- policies;
- ownership derivado del usuario autenticado;
- rate limiting;
- validation;
- no confiar en `user_id` enviado por cliente.

## Transactions
Usar en operaciones multi-registro.

## Queues
Candidatos:
- AI;
- imports;
- image processing;
- notifications;
- bulk normalization.

## Scheduler
- cleanup;
- subscription verification;
- external refresh;
- stuck jobs;
- housekeeping.

## Admin
Livewire + Alpine + Tailwind.
