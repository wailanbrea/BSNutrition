<?php

namespace App\Services;

use App\Models\Food;
use App\Models\FoodPortion;

class NutritionCalculatorService
{
    /**
     * Calculate scaled nutritional values for a given food and quantity.
     *
     * @return array<string, mixed>
     */
    public function calculateForFood(Food $food, float $quantity, ?FoodPortion $portion = null, string $unit = 'g'): array
    {
        // 1. Determine total grams
        if ($portion) {
            $totalGrams = $quantity * $portion->gram_weight;
            $portionName = $portion->portion_name;
        } elseif (in_array(strtolower($unit), ['g', 'gramos', 'gram', 'grams', 'ml', 'mililitros'])) {
            $totalGrams = $quantity;
            $portionName = "{$quantity} {$unit}";
        } else {
            $totalGrams = $quantity * ($food->default_basis_amount ?: 100.0);
            $portionName = "{$quantity} {$unit}";
        }

        // 2. Determine basis and scaling factor
        $basisAmount = $food->default_basis_amount > 0 ? (float) $food->default_basis_amount : 100.0;
        $scalingFactor = $totalGrams / $basisAmount;

        // 3. Scale all nutrients
        $nutrients = [];
        $calories = 0.0;
        $protein = 0.0;
        $carbohydrates = 0.0;
        $fat = 0.0;
        $fiber = 0.0;
        $sodium = 0.0;
        $sugar = 0.0;

        // Ensure relations loaded
        if (! $food->relationLoaded('foodNutrients')) {
            $food->load(['foodNutrients.nutrient']);
        }

        foreach ($food->foodNutrients as $fn) {
            $nutrient = $fn->nutrient;
            if (! $nutrient) {
                continue;
            }

            $rawAmount = (float) $fn->amount;
            $scaledAmount = $rawAmount * $scalingFactor;

            $nutrients[$nutrient->code] = [
                'code' => $nutrient->code,
                'name' => $nutrient->name,
                'unit' => $nutrient->unit,
                'amount' => round($scaledAmount, 4),
                'is_macro' => (bool) $nutrient->is_macro,
            ];

            // Specific macro extracts
            match ($nutrient->code) {
                'calories' => $calories = $scaledAmount,
                'protein' => $protein = $scaledAmount,
                'carbohydrate' => $carbohydrates = $scaledAmount,
                'total_fat' => $fat = $scaledAmount,
                'fiber' => $fiber = $scaledAmount,
                'sodium' => $sodium = $scaledAmount,
                'sugar' => $sugar = $scaledAmount,
                default => null,
            };
        }

        return [
            'food_id' => $food->id,
            'food_name' => $food->canonical_name,
            'quantity' => round($quantity, 2),
            'unit' => $portionName,
            'grams' => round($totalGrams, 2),
            'calories_snapshot' => (int) round($calories),
            'protein_snapshot' => round($protein, 2),
            'carbs_snapshot' => round($carbohydrates, 2),
            'fat_snapshot' => round($fat, 2),
            'fiber_snapshot' => round($fiber, 2),
            'sodium_snapshot' => round($sodium, 2),
            'sugar_snapshot' => round($sugar, 2),
            'nutrients' => array_values($nutrients),
            'nutrient_snapshot_json' => json_encode(array_values($nutrients)),
        ];
    }

    /**
     * Aggregate multiple calculated items into a combined summary (e.g. for a meal or a whole day).
     *
     * @param  array<int, array<string, mixed>>  $items
     * @return array<string, mixed>
     */
    public function aggregate(array $items): array
    {
        $totalCalories = 0;
        $totalProtein = 0.0;
        $totalCarbs = 0.0;
        $totalFat = 0.0;
        $totalFiber = 0.0;
        $totalSodium = 0.0;
        $totalSugar = 0.0;
        $totalGrams = 0.0;

        $aggregatedNutrients = [];

        foreach ($items as $item) {
            $totalCalories += (int) ($item['calories_snapshot'] ?? 0);
            $totalProtein += (float) ($item['protein_snapshot'] ?? 0.0);
            $totalCarbs += (float) ($item['carbs_snapshot'] ?? 0.0);
            $totalFat += (float) ($item['fat_snapshot'] ?? 0.0);
            $totalFiber += (float) ($item['fiber_snapshot'] ?? 0.0);
            $totalSodium += (float) ($item['sodium_snapshot'] ?? 0.0);
            $totalSugar += (float) ($item['sugar_snapshot'] ?? 0.0);
            $totalGrams += (float) ($item['grams'] ?? 0.0);

            if (isset($item['nutrients']) && is_array($item['nutrients'])) {
                foreach ($item['nutrients'] as $nut) {
                    $code = $nut['code'];
                    if (! isset($aggregatedNutrients[$code])) {
                        $aggregatedNutrients[$code] = [
                            'code' => $code,
                            'name' => $nut['name'],
                            'unit' => $nut['unit'],
                            'amount' => 0.0,
                            'is_macro' => $nut['is_macro'] ?? false,
                        ];
                    }
                    $aggregatedNutrients[$code]['amount'] += (float) $nut['amount'];
                }
            }
        }

        // Round consolidated nutrients
        foreach ($aggregatedNutrients as $code => $nut) {
            $aggregatedNutrients[$code]['amount'] = round($nut['amount'], 4);
        }

        // Calculate Macronutrient caloric percentages
        $macroCaloriesTotal = ($totalProtein * 4) + ($totalCarbs * 4) + ($totalFat * 9);
        $proteinPct = $macroCaloriesTotal > 0 ? round((($totalProtein * 4) / $macroCaloriesTotal) * 100, 1) : 0.0;
        $carbsPct = $macroCaloriesTotal > 0 ? round((($totalCarbs * 4) / $macroCaloriesTotal) * 100, 1) : 0.0;
        $fatPct = $macroCaloriesTotal > 0 ? round((($totalFat * 9) / $macroCaloriesTotal) * 100, 1) : 0.0;

        return [
            'total_items' => count($items),
            'total_grams' => round($totalGrams, 2),
            'total_calories' => $totalCalories,
            'total_protein_g' => round($totalProtein, 2),
            'total_carbs_g' => round($totalCarbs, 2),
            'total_fat_g' => round($totalFat, 2),
            'total_fiber_g' => round($totalFiber, 2),
            'total_sodium_mg' => round($totalSodium, 2),
            'total_sugar_g' => round($totalSugar, 2),
            'macro_distribution' => [
                'protein_pct' => $proteinPct,
                'carbs_pct' => $carbsPct,
                'fat_pct' => $fatPct,
            ],
            'nutrients' => array_values($aggregatedNutrients),
        ];
    }
}
