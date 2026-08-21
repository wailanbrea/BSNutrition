# Phase 02 — Android Foundation and Authentication

## Goal
Crear app Kotlin/Compose, arquitectura core, red, almacenamiento y login.

## Read for this phase
- `06_ANDROID_ARCHITECTURE.md`
- `02_TECH_STACK.md`
- `05_API_CONTRACT.md`

## Entry criteria
- [ ] Backend auth ready
- [ ] Android SDK/JDK ready

## Tasks

### [x] PH02-T01 — Android project/version catalog
**Depends on:** None

**Implementation checklist:**
- [x] Create app (`NutritionApp/app`)
- [x] Compose/Material3 (`BSNutritionTheme`, Material 3, dynamic theming)
- [x] libs.versions.toml (`gradle/libs.versions.toml`)
- [x] Debug/release (buildTypes con BuildConfig BASE_URL)

**Acceptance criteria:**
- Build/install works

**Tests / verification:**
- Estructura y catálogo de versiones verificados, Unit test base (`ExampleUnitTest`)

### [x] PH02-T02 — Core architecture
**Depends on:** None

**Implementation checklist:**
- [x] Hilt (`DispatchersModule`, `DataStoreModule`, `NetworkModule`, `DatabaseModule`)
- [x] designsystem (`BsnButton`, `BsnTextField`, `BsnCard`, `BsnLoadingIndicator`)
- [x] model (`User`, `UserProfile`, `AuthToken`, `AuthResponse`, `ApiError`, `ApiException`, `Result<T>`)
- [x] network (Base OkHttp Client, AuthInterceptor, Retrofit, Serialization)
- [x] database (Room `NutritionDatabase`, `UserEntity`, `UserDao`)
- [x] datastore (`AuthTokenDataSource`, `AuthTokenDataSourceImpl`)
- [x] testing (`ResultTest`, `ExampleUnitTest`)

**Acceptance criteria:**
- Modules/dependencies compile y arquitectura core desacoplada

**Tests / verification:**
- Unit tests de Result y arquitectura core (`ResultTest`)

### [x] PH02-T03 — Network layer
**Depends on:** None

**Implementation checklist:**
- [x] Retrofit/OkHttp (`AuthApiService`, `ProfileApiService`, `HealthApiService`)
- [x] Serialization (`AuthDtos`, `ProfileDtos`, `HealthDtos`)
- [x] Auth interceptor (`AuthInterceptor` con inyección automática de token Bearer)
- [x] Typed errors (`safeApiCall`, `parseApiError`, `ApiException` con deserialización de `ApiErrorResponse`)
- [x] Safe logging (`HttpLoggingInterceptor` condicional por `BuildConfig.DEBUG`)

**Acceptance criteria:**
- Conexión e interfaces Retrofit/OkHttp tipadas con manejo de errores

**Tests / verification:**
- Unit tests de `SafeApiCallTest` y parseo de errores tipados

### [x] PH02-T04 — Auth UI/repository
**Depends on:** None

**Implementation checklist:**
- [x] Register (`RegisterScreen`, `AuthRepository.register`, validación de campos)
- [x] Login (`LoginScreen`, `AuthRepository.login`, captura de errores)
- [x] Token persistence (`AuthTokenDataSourceImpl` vía DataStore)
- [x] Session restore (`currentUser`, `isAuthenticated`, cache en Room `UserDao`)
- [x] Logout (`AuthRepository.logout` y limpieza de sesión local)

**Acceptance criteria:**
- Flujo de autenticación completo desacoplado con Repository, ViewModel y UI Compose

**Tests / verification:**
- Tests unitarios en `AuthRepositoryTest` y `AuthViewModelTest`

### [ ] PH02-T05 — Navigation shell
**Depends on:** None

**Implementation checklist:**
- [ ] Single Activity
- [ ] Navigation 3
- [ ] Auth/app graphs
- [ ] Top-level placeholders

**Acceptance criteria:**
- Stable login/logout navigation

**Tests / verification:**
- Build/tests relevantes deben pasar

## Phase exit criteria
- [ ] Android auth working
- [ ] Status -> Phase 03

## Mandatory closeout
- [ ] Actualizar `PROJECT_STATUS.md`
- [ ] Actualizar `CHANGELOG.md`
- [ ] Actualizar `DECISIONS.md` si hubo decisión permanente
- [ ] No dejar tareas `[-]` sin checkpoint
