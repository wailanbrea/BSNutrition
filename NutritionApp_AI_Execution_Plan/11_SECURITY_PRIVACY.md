# Security and Privacy

## Nunca en Android
- OpenAI secret;
- MySQL credentials;
- R2 secret;
- server service-account credentials.

## Backend
- TLS;
- Sanctum;
- ownership policies;
- rate limits;
- request limits;
- validation;
- no stack traces in production;
- least privilege DB account.

## Logs
Redactar tokens, passwords, health payloads e imágenes privadas.

## Uploads
- private bucket;
- signed access;
- MIME/size validation;
- randomized object keys;
- image decode validation.

## Usuario
- eliminar cuenta;
- exportar datos;
- eliminar fotos;
- desconectar Health Connect;
- controlar notificaciones.

## IA
Mostrar estimaciones como estimaciones, permitir corrección y no presentar resultados como diagnóstico médico.

## Revisión
- IDOR;
- auth bypass;
- upload abuse;
- replay/idempotency;
- malformed payload;
- secret scan;
- admin isolation.
