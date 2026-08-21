# Android Environment Strategy

## Principio

Android **nunca** contiene secretos de servidor (ADR-005). La única "secreta"
que puede llevar el APK es la URL pública de la API.

## Base URL por build type

| Build type | API base URL |
|---|---|
| `debug` | `https://staging-api.nutrition-app.dev/api/v1/` |
| `release` | `https://api.nutrition-app.dev/api/v1/` |

La URL se inyecta como `BuildConfig` en `app/build.gradle.kts`:

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL",
                "\"https://staging-api.nutrition-app.dev/api/v1/\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL",
                "\"https://api.nutrition-app.dev/api/v1/\"")
        }
    }
}
```

- `staging` = backend desplegado en staging (misma lógica, datos de prueba).
- `production` = backend real (solo build type `release` apunta aquí).
- Ningún build type apunta a un `.env` de servidor.

## Firebase

- `google-services.json` se genera por proyecto (debug y release tienen
  `applicationId` distinto: `com.nutrition.debug` / `com.nutrition`).
- El archivo se comparte entre el equipo pero **no se commitea** (ver
  `.gitignore`).

## Otros valores por entorno

| Valor | debug | release |
|---|---|---|
| Crashlytics | `google-services.json` debug | `google-services.json` release |
| FCM | token debug | token release |
| Billing | sandbox (Play Billing test) | producción |

## Reglas

- No hardcodear tokens, API keys ni credenciales de ningún proveedor.
- `secrets.properties` (si se usa para valores de build) queda fuera de git.
- Verificar que un build de release no contenga la URL de staging.
