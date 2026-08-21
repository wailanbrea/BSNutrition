# Phase 08 — Barcode

## Goal
Agregar escaneo CameraX/ML Kit y lookup local/externo.

## Read for this phase
- `02_TECH_STACK.md`
- `05_API_CONTRACT.md`
- `04_DATABASE_DESIGN.md`

## Entry criteria
- [x] Catalog/search ready (Fases 04 y 05 completadas)

## Tasks

### [x] PH08-T01 — Barcode lookup API
**Depends on:** None

**Implementation checklist:**
- [x] Local first (`Food::byBarcode($cleanBarcode)->first()`)
- [x] OFF fallback (`OpenFoodFactsService::getByBarcode($cleanBarcode)`)
- [x] Import/cache (Importación y persistencia automática en base de datos de productos OFF)
- [x] Not found (Respuesta JSON 404 estructurada con código `NOT_FOUND`)
- [x] Rate limit (Protección contra abusos con middleware `throttle:api`)

**Acceptance criteria:**
- Productos locales no realizan llamadas externas; productos de Open Food Facts se importan y quedan en caché

**Tests / verification:**
- Tests en Pest (`FoodSearchApiTest.php` y `OpenFoodFactsServiceTest.php`)


### [x] PH08-T02 — Android scanner
**Depends on:** None

**Implementation checklist:**
- [x] CameraX (`PreviewView`, `ImageAnalysis`, `ProcessCameraProvider`)
- [x] ML Kit (`BarcodeScanning`, soporte para EAN-13, EAN-8, UPC-A, UPC-E, QR, Code-128)
- [x] Throttle (Filtro de debouncing de 1.5s para evitar eventos de escaneo redundantes)
- [x] Permissions (Solicitud en tiempo de ejecución de `Manifest.permission.CAMERA` con fallback informativo)
- [x] Lifecycle (Enlace seguro al ciclo de vida del composable con `LocalLifecycleOwner`)

**Acceptance criteria:**
- Escaneo fluido, estable y reactivo con vista previa y visor de alineación

**Tests / verification:**
- Tests en Kotlin (`BarcodeScannerViewModelTest.kt`)

### [x] PH08-T03 — Log scanned product
**Depends on:** None

**Implementation checklist:**
- [x] Detail (Presentación inmediata del bottom sheet `FoodDetailSheet` con macronutrientes al detectar código)
- [x] Portion (Selección dinámica de porción y recálculo calórico interactivo)
- [x] Add diary (Persistencia local-first en Room y backend con registro en alimentos recientes)
- [x] Unknown path (Diálogo de producto no encontrado con opción de reintento o registro manual)

**Acceptance criteria:**
- Flujo completo desde el escaneo del código de barras hasta el registro en el diario nutricional

**Tests / verification:**
- Tests en Kotlin (`BarcodeScannerViewModelTest.kt`)

## Phase exit criteria
- [x] Barcode E2E complete (Escáner CameraX/ML Kit + API Lookup con Open Food Facts fallback + registro en diario)
- [x] Status -> Phase 09

## Mandatory closeout
- [x] Actualizar `PROJECT_STATUS.md`
- [x] Actualizar `CHANGELOG.md`
- [x] Actualizar `DECISIONS.md` si hubo decisión permanente
- [x] No dejar tareas `[-]` sin checkpoint

