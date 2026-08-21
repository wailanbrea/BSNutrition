# DevOps and Deployment

## Ambientes
- local;
- staging;
- production.

## Backend production
- PHP 8.4;
- Laravel;
- MySQL 8.4;
- scheduler;
- queue worker cuando aplique;
- Redis cuando aplique;
- R2/S3.

## Config
`.env.example` sin secretos.

## CI
PR:
- Android build/test/lint;
- backend tests/Pint.

Protected branch:
- build artifacts;
- staging deployment cuando se configure.

## DB
- backup antes de migraciones riesgosas;
- restore test;
- migration status.

## Android release
- secure signing;
- internal test;
- R8 rules;
- mapping files;
- Crashlytics mapping.

## Monitoring
- API errors/latency;
- queue failures;
- AI failures/cost;
- sync failures;
- crash/ANR;
- DB/storage.
