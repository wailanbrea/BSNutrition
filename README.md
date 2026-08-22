# BSNutrition — Sistema Integral de Nutrición e Inteligencia Artificial

Plataforma integral de nutrición, conteo calórico y de macronutrientes, diario interactivo offline-first, reconocimiento fotográfico de platos con visión artificial, OCR de etiquetas nutricionales, dictado por voz y catálogo gastronómico criollo dominicano e internacional.

---

## Arquitectura del Monorepo

```
BSNutrition/
├── NutritionApp/                     # Aplicación Android Nativa (Kotlin + Jetpack Compose)
│   ├── app/src/main/java/...         # Clean Architecture (core/data, core/network, core/database, feature/*)
│   └── app/proguard-rules.pro        # Reglas ProGuard/R8 para Release
├── nutrition-backend/                # Backend API REST (Laravel 11 / PHP 8.2+ / MySQL 8.4)
│   ├── app/Http/Controllers/Api/V1/  # Controladores REST versionados
│   ├── app/Services/                 # Motores de cálculo, IA, búsqueda y sincronización
│   ├── database/migrations/          # Esquema relacional canónico (28 migraciones)
│   └── tests/Feature/                # Suite de pruebas automatizadas Pest (119+ tests)
└── NutritionApp_AI_Execution_Plan/   # Paquete de Gobernanza y Especificación (18 Fases)
    ├── PROJECT_STATUS.md             # Estado exacto y trazabilidad del proyecto
    ├── CHANGELOG.md                  # Historial cronológico de cambios
    ├── DECISIONS.md                  # Registros de decisiones arquitectónicas (ADRs 001-012)
    └── phases/                       # Checklist de las 18 fases completadas al 100%
```

---

## Características Principales

1. **Catálogo Canónico de Nutrición (USDA + Open Food Facts + Dataset Dominicano):**
   - 9 tablas relacionales con soporte de micronutrientes, porciones personalizadas y alias criollos.
2. **Registro de Alimentos Multimodal:**
   - **Búsqueda Avanzada & Barcode Scanner:** Google ML Kit + CameraX con fallback en tiempo real.
   - **AI Food Photo Recognition:** Inferencia visual estructurada (OpenAI GPT-4o / Gemini Vision) con normalización léxica dominicana.
   - **Nutrition Label OCR:** Escaneo inteligente de tablas nutricionales con retícula de captura y extracción heurística bilingüe.
   - **Voz y Lenguaje Natural:** Reconocimiento por voz y parser NLP en español para platos combinados.
3. **Diario Nutricional Offline-First:**
   - Snapshots inmutables de calorías/macros por comida (`breakfast`, `lunch`, `dinner`, `snacks`).
   - Sincronización bidireccional local-first en Room Database con resolución de conflictos determinista.
4. **Hidratación y Control de Peso:**
   - Seguimiento de consumo de agua con accesos rápidos (+250ml a +1L).
   - Historial de peso con conversiones automáticas kg/lbs y metas visuales.
5. **Google Health Connect:**
   - Importación de pasos, peso y calorías activas quemadas.
   - Exportación de hidratación y nutrición con prevención estricta de bucles de retroalimentación (`clientRecordId`).
6. **Recetas Multi-Ingrediente:**
   - Creación y cálculo automático de rendimiento total y desglose por porción.
   - Inserción instantánea de porciones directas en el diario.
7. **Panel de Administración y Curación:**
   - RBAC (`admin`, `curator`), cola de curación del dataset dominicano, cola de revisión de baja confianza de IA y métricas operacionales.
8. **Monetización y Google Play Billing:**
   - Niveles Free y Pro con autoridad centralizada de backend y cuotas atómicas diarias de IA (`SELECT ... FOR UPDATE`).

---

## Cómo Levantar el Backend y Probar

### 1. Requisitos Previos
- **PHP 8.2+** con extensiones `pdo_mysql`, `mbstring`, `openssl`, `curl`, `fileinfo`.
- **MySQL 8.4 / MariaDB** (ej: XAMPP o servicio local).
- **Composer**.

### 2. Configuración y Migraciones
```bash
cd nutrition-backend

# 1. Configurar archivo de entorno
cp .env.example .env

# 2. Generar clave de aplicación
php artisan key:generate

# 3. Ejecutar migraciones y seeders del catálogo
php artisan migrate
php artisan db:seed
```

### 3. Iniciar el Servidor de Desarrollo
```bash
php artisan serve --host=0.0.0.0 --port=8000
```
La API estará disponible en `http://localhost:8000/api/v1` (y `http://10.0.2.2:8000/api/v1` desde el emulador de Android).

### 4. Ejecutar la Suite de Pruebas Automatizadas
```bash
php ./vendor/bin/pest
```

---

## Compilación y Ejecución en Android (`NutritionApp`)

1. Abrir la carpeta `NutritionApp/` en **Android Studio** (Koala / Ladybug).
2. Sincronizar Gradle (`Sync Project with Gradle Files`).
3. Ejecutar en emulador o dispositivo físico con Android 10+ (API 29+).

---

## Estado del Proyecto

**Estado:** `100% COMPLETADO (MVP RELEASE CANDIDATE)`  
**Versión:** `v1.0.0-mvp`
