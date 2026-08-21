<?php

namespace App\Services;

use App\Models\Diary;
use App\Models\Food;
use App\Models\Meal;
use App\Models\MealEntry;
use App\Models\User;
use App\Models\WaterLog;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class DiaryService
{
    public function __construct(
        protected NutritionCalculatorService $calculator
    ) {}

    /**
     * Default meal taxonomy with Spanish titles and display ordering.
     */
    public const DEFAULT_MEALS = [
        'breakfast' => ['name' => 'Desayuno', 'sort_order' => 1],
        'lunch' => ['name' => 'Almuerzo', 'sort_order' => 2],
        'dinner' => ['name' => 'Cena', 'sort_order' => 3],
        'snack' => ['name' => 'Merienda / Snacks', 'sort_order' => 4],
    ];

    /**
     * Get or create diary for a specific date and user, ensuring standard meal sections exist.
     */
    public function getOrCreateDiaryForDate(User $user, string $date, ?string $timezone = null): Diary
    {
        $diary = Diary::firstOrCreate(
            [
                'user_id' => $user->id,
                'diary_date' => $date,
            ],
            [
                'timezone' => $timezone ?? 'America/Santo_Domingo',
            ]
        );

        // Ensure default meals exist
        foreach (self::DEFAULT_MEALS as $type => $config) {
            Meal::firstOrCreate(
                [
                    'diary_id' => $diary->id,
                    'meal_type' => $type,
                ],
                [
                    'name' => $config['name'],
                    'sort_order' => $config['sort_order'],
                ]
            );
        }

        return $diary->load(['meals.entries.food.brand', 'meals.entries.food.category', 'meals.entries.portion']);
    }

    /**
     * Add a food log entry with snapshot calculations and idempotency.
     */
    public function addEntry(User $user, string $date, array $data): MealEntry
    {
        // 1. Check idempotency via client_id if provided
        if (! empty($data['client_id'])) {
            $existing = MealEntry::where('client_id', $data['client_id'])->first();
            if ($existing) {
                return $existing;
            }
        }

        // 2. Get or create diary
        $diary = $this->getOrCreateDiaryForDate($user, $date);

        // 3. Resolve target meal
        $mealType = $data['meal_type'] ?? 'snack';
        $meal = $diary->meals()->firstOrCreate(
            ['meal_type' => $mealType],
            [
                'name' => self::DEFAULT_MEALS[$mealType]['name'] ?? ucfirst($mealType),
                'sort_order' => self::DEFAULT_MEALS[$mealType]['sort_order'] ?? 5,
            ]
        );

        // 4. Resolve food and snapshots
        $foodId = $data['food_id'] ?? null;
        $portionId = $data['portion_id'] ?? null;
        $quantity = (float) ($data['quantity'] ?? 1.0);
        $unit = $data['unit'] ?? 'porción';
        $customName = $data['custom_name'] ?? null;

        $caloriesSnapshot = 0;
        $proteinSnapshot = 0.0;
        $carbsSnapshot = 0.0;
        $fatSnapshot = 0.0;
        $fiberSnapshot = null;
        $sodiumSnapshot = null;
        $sugarSnapshot = null;
        $nutrientSnapshotJson = null;
        $grams = (float) ($data['grams'] ?? 100.0);

        if ($foodId) {
            $food = Food::with(['foodNutrients.nutrient', 'portions'])->findOrFail($foodId);
            $customName = $customName ?: $food->canonical_name;

            $portion = $portionId ? $food->portions->firstWhere('id', $portionId) : null;
            if (! $portion && ! in_array(strtolower($unit), ['g', 'gramos', 'ml', 'mililitros'])) {
                $portion = $food->portions->firstWhere('is_default', true) ?? $food->portions->first();
            }

            $calculation = $this->calculator->calculateForFood(
                food: $food,
                quantity: $quantity,
                portion: $portion,
                unit: $unit
            );

            $grams = $calculation['grams'];
            $caloriesSnapshot = $calculation['calories_snapshot'];
            $proteinSnapshot = $calculation['protein_snapshot'];
            $carbsSnapshot = $calculation['carbs_snapshot'];
            $fatSnapshot = $calculation['fat_snapshot'];
            $nutrientSnapshotJson = $calculation['nutrient_snapshot_json'];

            // Extract fiber, sodium, sugar if present
            if (isset($nutrientSnapshotJson['fiber_g'])) {
                $fiberSnapshot = $nutrientSnapshotJson['fiber_g'];
            }
            if (isset($nutrientSnapshotJson['sodium_mg'])) {
                $sodiumSnapshot = $nutrientSnapshotJson['sodium_mg'];
            }
            if (isset($nutrientSnapshotJson['sugar_g'])) {
                $sugarSnapshot = $nutrientSnapshotJson['sugar_g'];
            }

            // Record recent usage
            $this->recordFoodUsage($user, $food->id);
        } else {
            // Manual quick entry
            $customName = $customName ?: 'Entrada rápida';
            $caloriesSnapshot = (int) ($data['calories'] ?? 0);
            $proteinSnapshot = (float) ($data['protein_g'] ?? 0.0);
            $carbsSnapshot = (float) ($data['carbs_g'] ?? 0.0);
            $fatSnapshot = (float) ($data['fat_g'] ?? 0.0);
        }

        return MealEntry::create([
            'client_id' => $data['client_id'] ?? null,
            'meal_id' => $meal->id,
            'food_id' => $foodId,
            'portion_id' => $portionId,
            'custom_name' => $customName,
            'quantity' => $quantity,
            'unit' => $unit,
            'grams' => $grams,
            'calories_snapshot' => $caloriesSnapshot,
            'protein_snapshot' => $proteinSnapshot,
            'carbs_snapshot' => $carbsSnapshot,
            'fat_snapshot' => $fatSnapshot,
            'fiber_snapshot' => $fiberSnapshot,
            'sodium_snapshot' => $sodiumSnapshot,
            'sugar_snapshot' => $sugarSnapshot,
            'nutrient_snapshot_json' => $nutrientSnapshotJson,
            'source' => $data['source'] ?? ($foodId ? 'catalog' : 'quick_add'),
            'version' => 1,
        ]);
    }

    /**
     * Update an existing entry with ownership validation and snapshot recalculation.
     */
    public function updateEntry(User $user, int $entryId, array $data): MealEntry
    {
        $entry = MealEntry::with('meal.diary')->findOrFail($entryId);

        if ($entry->meal->diary->user_id !== $user->id) {
            throw ValidationException::withMessages(['entry' => 'No autorizado para modificar esta entrada.']);
        }

        $quantity = isset($data['quantity']) ? (float) $data['quantity'] : (float) $entry->quantity;
        $portionId = array_key_exists('portion_id', $data) ? $data['portion_id'] : $entry->portion_id;
        $unit = $data['unit'] ?? $entry->unit;

        if ($entry->food_id && (isset($data['quantity']) || array_key_exists('portion_id', $data) || isset($data['unit']))) {
            $food = Food::with(['foodNutrients.nutrient', 'portions'])->findOrFail($entry->food_id);
            $portion = $portionId ? $food->portions->firstWhere('id', $portionId) : null;
            if (! $portion && ! in_array(strtolower($unit), ['g', 'gramos', 'ml', 'mililitros'])) {
                $portion = $food->portions->firstWhere('is_default', true) ?? $food->portions->first();
            }

            $calculation = $this->calculator->calculateForFood(
                food: $food,
                quantity: $quantity,
                portion: $portion,
                unit: $unit
            );

            $entry->quantity = $quantity;
            $entry->portion_id = $portionId;
            $entry->unit = $unit;
            $entry->grams = $calculation['grams'];
            $entry->calories_snapshot = $calculation['calories_snapshot'];
            $entry->protein_snapshot = $calculation['protein_snapshot'];
            $entry->carbs_snapshot = $calculation['carbs_snapshot'];
            $entry->fat_snapshot = $calculation['fat_snapshot'];
            $entry->nutrient_snapshot_json = $calculation['nutrient_snapshot_json'];
        } else {

            if (isset($data['custom_name'])) {
                $entry->custom_name = $data['custom_name'];
            }
            if (isset($data['quantity'])) {
                $entry->quantity = $quantity;
            }
            if (isset($data['calories'])) {
                $entry->calories_snapshot = (int) $data['calories'];
            }
            if (isset($data['protein_g'])) {
                $entry->protein_snapshot = (float) $data['protein_g'];
            }
            if (isset($data['carbs_g'])) {
                $entry->carbs_snapshot = (float) $data['carbs_g'];
            }
            if (isset($data['fat_g'])) {
                $entry->fat_snapshot = (float) $data['fat_g'];
            }
        }

        $entry->version = $entry->version + 1;
        $entry->save();

        return $entry;
    }

    /**
     * Delete an entry with ownership verification.
     */
    public function deleteEntry(User $user, int $entryId): void
    {
        $entry = MealEntry::with('meal.diary')->findOrFail($entryId);

        if ($entry->meal->diary->user_id !== $user->id) {
            throw ValidationException::withMessages(['entry' => 'No autorizado para eliminar esta entrada.']);
        }

        $entry->delete();
    }

    /**
     * Copy an entire meal to another date or meal type.
     */
    public function copyMeal(User $user, int $sourceMealId, string $targetDate, string $targetMealType): Meal
    {
        $sourceMeal = Meal::with(['diary', 'entries' => function ($query) {
            $query->whereNull('deleted_at');
        }])->findOrFail($sourceMealId);

        if ($sourceMeal->diary->user_id !== $user->id) {
            throw ValidationException::withMessages(['meal' => 'No autorizado para copiar esta comida.']);
        }

        $targetDiary = $this->getOrCreateDiaryForDate($user, $targetDate);
        $targetMeal = $targetDiary->meals()->firstOrCreate(
            ['meal_type' => $targetMealType],
            [
                'name' => self::DEFAULT_MEALS[$targetMealType]['name'] ?? ucfirst($targetMealType),
                'sort_order' => self::DEFAULT_MEALS[$targetMealType]['sort_order'] ?? 5,
            ]
        );

        foreach ($sourceMeal->entries as $entry) {
            MealEntry::create([
                'meal_id' => $targetMeal->id,
                'food_id' => $entry->food_id,
                'portion_id' => $entry->portion_id,
                'custom_name' => $entry->custom_name,
                'quantity' => $entry->quantity,
                'unit' => $entry->unit,
                'grams' => $entry->grams,
                'calories_snapshot' => $entry->calories_snapshot,
                'protein_snapshot' => $entry->protein_snapshot,
                'carbs_snapshot' => $entry->carbs_snapshot,
                'fat_snapshot' => $entry->fat_snapshot,
                'fiber_snapshot' => $entry->fiber_snapshot,
                'sodium_snapshot' => $entry->sodium_snapshot,
                'sugar_snapshot' => $entry->sugar_snapshot,
                'nutrient_snapshot_json' => $entry->nutrient_snapshot_json,
                'source' => $entry->source,
                'version' => 1,
            ]);
        }

        return $targetMeal->load('entries');
    }

    /**
     * Copy all meals from one day to another.
     */
    public function copyDay(User $user, string $sourceDate, string $targetDate): Diary
    {
        $sourceDiary = Diary::where('user_id', $user->id)
            ->where('diary_date', $sourceDate)
            ->with(['meals.entries' => function ($q) {
                $q->whereNull('deleted_at');
            }])
            ->firstOrFail();

        $targetDiary = $this->getOrCreateDiaryForDate($user, $targetDate);

        foreach ($sourceDiary->meals as $sourceMeal) {
            $targetMeal = $targetDiary->meals()->firstOrCreate(
                ['meal_type' => $sourceMeal->meal_type],
                [
                    'name' => $sourceMeal->name,
                    'sort_order' => $sourceMeal->sort_order,
                ]
            );

            foreach ($sourceMeal->entries as $entry) {
                MealEntry::create([
                    'meal_id' => $targetMeal->id,
                    'food_id' => $entry->food_id,
                    'portion_id' => $entry->portion_id,
                    'custom_name' => $entry->custom_name,
                    'quantity' => $entry->quantity,
                    'unit' => $entry->unit,
                    'grams' => $entry->grams,
                    'calories_snapshot' => $entry->calories_snapshot,
                    'protein_snapshot' => $entry->protein_snapshot,
                    'carbs_snapshot' => $entry->carbs_snapshot,
                    'fat_snapshot' => $entry->fat_snapshot,
                    'fiber_snapshot' => $entry->fiber_snapshot,
                    'sodium_snapshot' => $entry->sodium_snapshot,
                    'sugar_snapshot' => $entry->sugar_snapshot,
                    'nutrient_snapshot_json' => $entry->nutrient_snapshot_json,
                    'source' => $entry->source,
                    'version' => 1,
                ]);
            }
        }

        return $targetDiary->load('meals.entries');
    }

    /**
     * Log water consumption.
     */
    public function logWater(User $user, string $date, int $amountMl, ?string $clientId = null, string $source = 'manual'): WaterLog
    {
        if ($clientId) {
            $existing = WaterLog::where('client_id', $clientId)->first();
            if ($existing) {
                return $existing;
            }
        }

        return WaterLog::create([
            'client_id' => $clientId,
            'user_id' => $user->id,
            'log_date' => $date,
            'amount_ml' => $amountMl,
            'occurred_at' => now(),
            'source' => $source,
            'version' => 1,
        ]);
    }

    /**
     * Delete water log.
     */
    public function deleteWaterLog(User $user, int $waterLogId): void
    {
        $log = WaterLog::where('user_id', $user->id)->findOrFail($waterLogId);
        $log->delete();
    }

    /**
     * Get total water logged for date.
     */
    public function getDailyWaterTotal(User $user, string $date): int
    {
        return (int) WaterLog::where('user_id', $user->id)
            ->where('log_date', $date)
            ->sum('amount_ml');
    }

    /**
     * Compute comprehensive daily nutritional totals and macro distributions.
     */
    public function getDailySummary(User $user, string $date): array
    {
        $diary = $this->getOrCreateDiaryForDate($user, $date);

        $totalCalories = 0;
        $totalProtein = 0.0;
        $totalCarbs = 0.0;
        $totalFat = 0.0;
        $totalFiber = 0.0;

        $mealsSummary = [];

        foreach ($diary->meals as $meal) {
            $mealCalories = 0;
            $mealProtein = 0.0;
            $mealCarbs = 0.0;
            $mealFat = 0.0;

            foreach ($meal->entries as $entry) {
                $mealCalories += $entry->calories_snapshot;
                $mealProtein += (float) $entry->protein_snapshot;
                $mealCarbs += (float) $entry->carbs_snapshot;
                $mealFat += (float) $entry->fat_snapshot;
                $totalFiber += (float) ($entry->fiber_snapshot ?? 0.0);
            }

            $totalCalories += $mealCalories;
            $totalProtein += $mealProtein;
            $totalCarbs += $mealCarbs;
            $totalFat += $mealFat;

            $mealsSummary[] = [
                'id' => $meal->id,
                'meal_type' => $meal->meal_type,
                'name' => $meal->name,
                'calories' => $mealCalories,
                'protein_g' => round($mealProtein, 2),
                'carbs_g' => round($mealCarbs, 2),
                'fat_g' => round($mealFat, 2),
                'entries_count' => $meal->entries->count(),
            ];
        }

        $waterMl = $this->getDailyWaterTotal($user, $date);

        return [
            'date' => $date,
            'calories' => $totalCalories,
            'protein_g' => round($totalProtein, 2),
            'carbs_g' => round($totalCarbs, 2),
            'fat_g' => round($totalFat, 2),
            'fiber_g' => round($totalFiber, 2),
            'water_ml' => $waterMl,
            'meals' => $mealsSummary,
        ];
    }

    /**
     * Helper to track recent food consumption.
     */
    protected function recordFoodUsage(User $user, int $foodId): void
    {
        $existing = DB::table('user_food_recents')
            ->where('user_id', $user->id)
            ->where('food_id', $foodId)
            ->first();

        if ($existing) {
            DB::table('user_food_recents')
                ->where('id', $existing->id)
                ->update([
                    'use_count' => $existing->use_count + 1,
                    'last_used_at' => now(),
                    'updated_at' => now(),
                ]);
        } else {
            DB::table('user_food_recents')->insert([
                'user_id' => $user->id,
                'food_id' => $foodId,
                'use_count' => 1,
                'last_used_at' => now(),
                'created_at' => now(),
                'updated_at' => now(),
            ]);
        }
    }
}
