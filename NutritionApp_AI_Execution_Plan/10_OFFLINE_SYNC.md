# Offline Sync

## Flujo

`User -> Room transaction -> UI -> pending mutation -> WorkManager -> API -> reconcile`

## Queue local

Campos:
- id;
- entity_type;
- entity_id;
- operation;
- payload/version;
- created_at;
- attempts;
- last_error;
- status.

## Idempotencia
Retries nunca crean duplicados.

## Pull incremental
Cursor/timestamp. Nunca bajar toda la historia en cada sync.

## Conflictos
Definir por entidad y documentar en ADR.

## Deletes
Tombstones/soft delete cuando deba propagarse.

## WorkManager
- network constraint;
- backoff;
- transient vs permanent errors;
- auth expiration handling.

## Tests críticos
- create offline;
- edit offline;
- delete offline;
- kill/restart;
- reconnect;
- duplicate retry;
- server 500;
- auth expired;
- ordered queue.
