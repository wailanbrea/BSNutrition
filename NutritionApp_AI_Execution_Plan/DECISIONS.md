# Decisions / ADR

## ADR-001 — Native Android
**Status:** Accepted  
Kotlin + Jetpack Compose.

## ADR-002 — Backend
**Status:** Accepted  
Laravel modular monolith.

## ADR-003 — Database
**Status:** Accepted  
MySQL 8.4 LTS.

## ADR-004 — Offline-first
**Status:** Accepted  
Diary writes go to Room first and sync later.

## ADR-005 — AI gateway
**Status:** Accepted  
Android never stores provider secrets; calls Laravel.

## ADR-006 — Nutrition authority
**Status:** Accepted  
AI identifies/estimates; Nutrition Engine calculates canonical nutrient values.

## ADR-007 — Repository layout
**Status:** Accepted (Phase 00 / 01)
Estructura de Monorepo unificado (`BSNutrition`) que contiene `NutritionApp/` (Android nativo), `nutrition-backend/` (Laravel API) y `NutritionApp_AI_Execution_Plan/` (paquete de gobernanza y especificaciones).

## ADR-008 — Branching & commits
**Status:** Accepted (Phase 00)
Conventional Commits con task ID en cada commit (`<tipo>(<alcance>): <descripcion> [<task-id>]`); rama `main` protegida y una rama de trabajo por tarea.

## ADR-009 — Nutrition Goals & Energy Expenditure Calculation Algorithm
**Status:** Accepted (Phase 03)
**Versión del Algoritmo:** `mifflin_v1.0`

### 1. Fórmula de Tasa Metabólica Basal (BMR)
Se adopta la ecuación de **Mifflin-St Jeor** como estándar clínico y canónico:
- **Hombres:** $BMR = (10 \times \text{peso en kg}) + (6.25 \times \text{altura en cm}) - (5 \times \text{edad en años}) + 5$
- **Mujeres:** $BMR = (10 \times \text{peso en kg}) + (6.25 \times \text{altura en cm}) - (5 \times \text{edad en años}) - 161$

### 2. Multiplicadores de Gasto Energético Diario Total (TDEE)
$TDEE = BMR \times \text{Factor de Actividad}$
- `sedentary` (Poco o ningún ejercicio, trabajo de escritorio): `1.200`
- `light` (Ejercicio ligero 1-3 días a la semana): `1.375`
- `moderate` (Ejercicio moderado 3-5 días a la semana): `1.550`
- `active` (Ejercicio intenso 6-7 días a la semana): `1.725`
- `very_active` (Ejercicio muy intenso, doble sesión o trabajo físico pesado): `1.900`

### 3. Restricciones y Ajustes por Objetivo
- **Perder peso (`lose_weight`):**
  - Déficit diario = $\text{weekly\_rate (kg/semana)} \times 1100 \text{ kcal}$ (equivalente a $\approx 7700\text{ kcal}/7\text{ días}$).
  - *Límite de seguridad fisiológica:* Nunca descender de **1,200 kcal/día** para mujeres ni de **1,500 kcal/día** para hombres.
- **Mantener peso (`maintain_weight`):**
  - Calorías meta = $TDEE$.
- **Ganar masa muscular / peso (`gain_muscle` / `gain_weight`):**
  - Superávit diario = $\text{weekly\_rate (kg/semana)} \times 1100 \text{ kcal}$ (rango recomendado: $+250$ a $+500\text{ kcal/día}$).

### 4. Distribución Canónica de Macronutrientes
Factores calóricos: Proteína = 4 kcal/g, Carbohidratos = 4 kcal/g, Grasas = 9 kcal/g.
- **`lose_weight`:** 30% Proteínas (alta saciedad/preservación muscular), 40% Carbohidratos, 30% Grasas.
- **`maintain_weight`:** 25% Proteínas, 45% Carbohidratos, 30% Grasas.
- **`gain_muscle` / `gain_weight`:** 30% Proteínas, 45% Carbohidratos, 25% Grasas.
- **Fibra diaria:** $14\text{ g por cada } 1000\text{ kcal}$ consumidas (mínimo 25g mujeres, 38g hombres).
- **Agua objetivo:** $35\text{ ml} \times \text{peso (kg)}$ (mínimo 2,000 ml).

### 5. Versionado y Snapshots
Todo cálculo persistido en `nutrition_goals` debe almacenar `calculation_version = "mifflin_v1.0"`.

## ADR-010 — Offline-First Synchronization & Conflict Resolution Strategy
**Status:** Accepted (Phase 07)

### 1. Identificadores Únicos Generados en el Cliente (`client_id`)
- Toda mutación iniciada en la aplicación móvil (creación de entradas de diario, registros de agua, registros de peso) genera un `client_id` único (UUIDv4) antes de persistir en Room.
- El backend almacena e indexa `client_id` con restricción de unicidad por usuario.
- En caso de reintentos de red o reenvíos de la cola de sincronización, la API responde de forma **idempotente** devolviendo el registro existente sin duplicar entradas ni calorias.

### 2. Versionado Optimista
- Cada entidad mutable almacena un entero `version` inicializado en 1.
- Toda actualización incrementa el contador de versión (`version = version + 1`) y actualiza el timestamp `updated_at`.
- En caso de modificaciones concurrentes, prevalece la versión con mayor número secuencial o timestamp más reciente (Last-Write-Wins convergente).

### 3. Tombstones (Eliminación Lógica)
- Las operaciones de eliminación aplican un borrado lógico (`is_deleted = true` en local, `deleted_at` vía SoftDeletes en backend).
- El estado "tombstone" asegura que una entidad eliminada localmente no sea reinsertada accidentalmente durante la reconciliación con el servidor.

### 4. Inmutabilidad de Snapshots Nutricionales
- Los macronutrientes y micronutrientes calculados al registrar un alimento en el diario se congelan en snapshots inmutables (`calories_snapshot`, `protein_snapshot`, etc.).
- Las ediciones posteriores del catálogo canónico de alimentos no modifican el historial registrado en días pasados del usuario.

## ADR-011 — Health Connect Synchronization & Loop Prevention Policy
**Status:** Accepted (Phase 12)

### 1. Attributed Source Identification (`clientRecordId`)
- Todo registro de nutrición o hidratación exportado hacia Google Health Connect incluye en sus metadatos un `clientRecordId` con prefijo explícito (ej: `bsnutrition:meal_entry:{id}` o `bsnutrition:water_log:{id}`).
- Al leer registros de Health Connect para importar métricas (pasos, peso, calorías quemadas), el sistema filtra y descarta cualquier registro originado por el paquete `com.bsnutrition.app` o cuyo `clientRecordId` coincida con nuestro espacio de nombres.

### 2. Prevensión de Bucles de Retroalimentación (Feedback Loop Elimination)
- **Lecturas:** Únicamente se importan métricas de actividad física y biométricas (`StepsRecord`, `WeightRecord`, `TotalCaloriesBurnedRecord`).
- **Escrituras:** Solo se exportan consumos consolidados generados por el usuario (`NutritionRecord`, `HydrationRecord`).
- BSNutrition nunca sobreescribe ni reimporta como comida propia los datos de nutrición exportados previamente a Health Connect.

### 3. Sincronización No Bloqueante
- Toda interacción con Health Connect se ejecuta de forma asíncrona en segundo plano sin interrumpir las operaciones del diario local (offline-first).

## ADR-012 — Subscription Tiers, Google Play Billing & Authoritative Quotas
**Status:** Accepted (Phase 16)

### 1. Modelos de Producto y SKUs
- **Free Tier:** 3 análisis de fotos de comida por día, 5 registros por texto/voz por día, catálogo completo, diario nutricional ilimitado.
- **Pro Monthly (`bsnutrition_pro_monthly`):** USD $6.99 / mes con 7 días de prueba gratuita. Análisis fotográfico y por voz ilimitados, analítica avanzada de 30 y 90 días, gestión ilimitada de recetas y soporte prioritario.
- **Pro Yearly (`bsnutrition_pro_yearly`):** USD $49.99 / año (descuento del 40%). Mismos beneficios de nivel Pro.

### 2. Autoridad Servidor (Backend-Authoritative Verification)
- Google Play Billing Client gestiona la transacción en el cliente Android y transmite el `purchaseToken`, `orderId` y `productId` al backend (`POST /api/v1/billing/verify-play-purchase`).
- El backend es la única fuente de verdad para el estado de la suscripción (`active`, `expired`, `in_grace_period`, `canceled`).
- El cliente nunca asume estado Pro localmente sin validación y firma de backend.

### 3. Control Atómico de Cuotas de IA (Atomic AI Quota Enforcement)
- Las cuotas de inferencia gratuita se gestionan a nivel de base de datos en `user_daily_ai_quotas` con transacciones atómicas `SELECT ... FOR UPDATE` para prevenir ataques de condiciones de carrera (race conditions).
- Al superar la cuota diaria gratuita, el backend responde con código HTTP `429 Too Many Requests` o `402 Payment Required` con código `AI_QUOTA_EXCEEDED`, activando la presentación del Paywall en Android.

## Pending ADR
- image retention




