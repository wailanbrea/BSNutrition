# Database Design

## Identity

`users`
- id
- name
- email
- password
- status
- email_verified_at
- timestamps
- deleted_at

`user_profiles`
- user_id
- birth_date
- sex
- height
- current_weight
- activity_level
- goal_type
- goal_weight
- weekly_goal_rate
- locale
- country_code
- timezone
- unit_system

`user_devices`
- id
- user_id
- platform
- push_token
- app_version
- last_seen_at

## Goals

`nutrition_goals`
- id
- user_id
- effective_from
- calorie_target
- protein_target_g
- carbohydrate_target_g
- fat_target_g
- fiber_target_g nullable
- source
- calculation_version

## Food catalog

- `food_categories`
- `brands`
- `foods`
- `food_aliases`
- `food_barcodes`
- `food_portions`
- `nutrients`
- `food_nutrients`
- `food_sources`
- `food_images`

### foods principales
- id
- canonical_name
- normalized_name
- brand_id nullable
- category_id nullable
- country_code nullable
- language
- verified
- source
- external_source_id nullable
- default_basis_amount
- default_basis_unit
- timestamps
- deleted_at

### nutrients
Usar filas normalizadas con códigos estables, no una columna nueva en `foods` por cada vitamina.

### food_nutrients
- food_id
- nutrient_id
- amount
- basis_amount
- basis_unit
- source

## Diary

`diaries`
- id
- user_id
- diary_date
- timezone
- unique(user_id, diary_date)

`meals`
- id
- diary_id
- meal_type
- sort_order

`meal_entries`
- id
- client_id
- meal_id
- food_id nullable
- custom_name
- quantity
- unit
- grams
- calories_snapshot
- protein_snapshot
- carbs_snapshot
- fat_snapshot
- fiber_snapshot nullable
- nutrient_snapshot_json
- source
- version
- timestamps
- deleted_at

**Regla:** el snapshot preserva el valor histórico aunque el alimento se corrija después.

## Tracking

`water_logs`
- id/client_id/user_id
- amount_ml
- occurred_at
- source
- version
- deleted_at

`weight_logs`
- id/client_id/user_id
- weight
- unit
- occurred_at
- source
- external_id nullable
- version
- deleted_at

`body_measurements`
`progress_photos`

## AI

`ai_analyses`
- id
- user_id
- type
- provider
- model
- status
- image_object_key nullable
- input_text nullable
- structured_output nullable
- estimated_cost nullable
- timestamps/error

`ai_analysis_items`
- analysis_id
- detected_name
- estimated_grams
- food_confidence
- portion_confidence
- matched_food_id nullable
- match_confidence nullable

`ai_corrections`
- item_id
- corrected_food_id/name/grams
- correction_type
- user_id

`ai_usage`
- user_id
- feature
- provider
- model
- image_count
- token usage nullable
- estimated_cost
- created_at

## Billing

`subscriptions`
`entitlements`

## Sync

Entidades offline:
- client-generated stable ID;
- version;
- updated_at;
- deleted_at cuando deba propagarse.

## Índices mínimos a revisar

- foods.normalized_name
- food_aliases.normalized_alias
- food_barcodes.barcode
- diaries(user_id, diary_date)
- weight_logs(user_id, occurred_at)
- water_logs(user_id, occurred_at)
- ai_usage(user_id, created_at)
