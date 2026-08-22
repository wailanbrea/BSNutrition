<?php

namespace App\Services;

use App\Models\Diary;
use App\Models\Food;
use App\Models\Meal;
use App\Models\MealEntry;
use App\Models\Recipe;
use App\Models\RecipeIngredient;
use App\Models\RecipeStep;
use App\Models\User;
use Illuminate\Support\Facades\DB;

class RecipeCalculationService
{
    /**
     * Calculate total recipe nutritional values and per-serving amounts.
     *
     * @param  array<int, array<string, mixed>>  $ingredients
     * @return array<string, mixed>
     */
    public function calculateTotals(array $ingredients, int $servings = 1): array
    {
        $servings = max(1, $servings);
        $totalWeight = 0.0;
        $totalCals = 0;
        $totalProt = 0.0;
        $totalCarbs = 0.0;
        $totalFat = 0.0;
        $totalFiber = 0.0;

        $processedIngredients = [];

        foreach ($ingredients as $index => $ing) {
            $foodId = isset($ing['food_id']) ? (int) $ing['food_id'] : null;
            $portionId = isset($ing['portion_id']) ? (int) $ing['portion_id'] : null;
            $name = (string) ($ing['custom_name'] ?? 'Ingrediente');
            $quantity = (float) ($ing['quantity'] ?? 1.0);
            $unit = (string) ($ing['unit'] ?? 'g');
            $grams = (float) ($ing['grams'] ?? 100.0);

            $cals = isset($ing['calories']) ? (int) $ing['calories'] : 0;
            $prot = isset($ing['protein_g']) ? (float) $ing['protein_g'] : 0.0;
            $carbs = isset($ing['carbs_g']) ? (float) $ing['carbs_g'] : 0.0;
            $fat = isset($ing['fat_g']) ? (float) $ing['fat_g'] : 0.0;

            // Auto-calculate from food catalog if calories not supplied
            if ($foodId && $cals === 0) {
                $food = Food::with(['foodNutrients.nutrient'])->find($foodId);
                if ($food) {
                    $cals100 = $food->getNutrientAmount('ENERGY_KCAL');
                    $prot100 = $food->getNutrientAmount('PROTEIN_G');
                    $carbs100 = $food->getNutrientAmount('CARBS_G');
                    $fat100 = $food->getNutrientAmount('FAT_G');

                    $cals = (int) round(($cals100 * $grams) / 100.0);
                    $prot = round(($prot100 * $grams) / 100.0, 1);
                    $carbs = round(($carbs100 * $grams) / 100.0, 1);
                    $fat = round(($fat100 * $grams) / 100.0, 1);
                }
            }

            $totalWeight += $grams;
            $totalCals += $cals;
            $totalProt += $prot;
            $totalCarbs += $carbs;
            $totalFat += $fat;

            $processedIngredients[] = [
                'food_id' => $foodId,
                'portion_id' => $portionId,
                'custom_name' => $name,
                'quantity' => $quantity,
                'unit' => $unit,
                'grams' => $grams,
                'calories' => $cals,
                'protein_g' => $prot,
                'carbs_g' => $carbs,
                'fat_g' => $fat,
                'sort_order' => $index,
            ];
        }

        return [
            'total_weight_grams' => round($totalWeight, 2),
            'total_calories' => $totalCals,
            'total_protein_g' => round($totalProt, 1),
            'total_carbs_g' => round($totalCarbs, 1),
            'total_fat_g' => round($totalFat, 1),
            'servings' => $servings,
            'calories_per_serving' => (int) round($totalCals / $servings),
            'protein_per_serving_g' => round($totalProt / $servings, 1),
            'carbs_per_serving_g' => round($totalCarbs / $servings, 1),
            'fat_per_serving_g' => round($totalFat / $servings, 1),
            'fiber_per_serving_g' => round($totalFiber / $servings, 1),
            'ingredients' => $processedIngredients,
        ];
    }

    /**
     * Create or update a full recipe with ingredients and steps in a transaction.
     *
     * @param  array<string, mixed>  $data
     */
    public function saveRecipe(User $user, array $data, ?Recipe $existing = null): Recipe
    {
        return DB::transaction(function () use ($user, $data, $existing) {
            $servings = max(1, (int) ($data['servings'] ?? 1));
            $calculated = $this->calculateTotals($data['ingredients'] ?? [], $servings);

            $recipeAttributes = [
                'user_id' => $user->id,
                'name' => $data['name'],
                'description' => $data['description'] ?? null,
                'servings' => $servings,
                'prep_time_minutes' => $data['prep_time_minutes'] ?? null,
                'cook_time_minutes' => $data['cook_time_minutes'] ?? null,
                'total_weight_grams' => $calculated['total_weight_grams'],
                'calories_per_serving' => $calculated['calories_per_serving'],
                'protein_per_serving_g' => $calculated['protein_per_serving_g'],
                'carbs_per_serving_g' => $calculated['carbs_per_serving_g'],
                'fat_per_serving_g' => $calculated['fat_per_serving_g'],
                'fiber_per_serving_g' => $calculated['fiber_per_serving_g'],
                'is_public' => (bool) ($data['is_public'] ?? false),
            ];

            if ($existing) {
                $existing->update($recipeAttributes);
                $recipe = $existing;
                $recipe->ingredients()->delete();
                $recipe->steps()->delete();
            } else {
                $recipe = Recipe::create($recipeAttributes);
            }

            foreach ($calculated['ingredients'] as $ing) {
                RecipeIngredient::create(array_merge($ing, ['recipe_id' => $recipe->id]));
            }

            if (! empty($data['steps'])) {
                foreach ($data['steps'] as $index => $step) {
                    RecipeStep::create([
                        'recipe_id' => $recipe->id,
                        'step_number' => $index + 1,
                        'instruction' => is_array($step) ? ($step['instruction'] ?? '') : (string) $step,
                    ]);
                }
            }

            return $recipe->load(['ingredients.food', 'steps']);
        });
    }

    /**
     * Log a serving of a recipe directly into the user daily diary.
     */
    public function logServingToDiary(
        User $user,
        Recipe $recipe,
        string $date,
        string $mealType,
        float $servings = 1.0,
        ?string $clientId = null
    ): MealEntry {
        $servings = max(0.1, $servings);

        $diary = Diary::firstOrCreate(
            ['user_id' => $user->id, 'diary_date' => $date],
            ['timezone' => $user->profile?->timezone ?? 'America/Santo_Domingo']
        );

        $meal = Meal::firstOrCreate(
            ['diary_id' => $diary->id, 'meal_type' => $mealType],
            ['name' => ucfirst($mealType), 'sort_order' => 1]
        );

        $portionWeightG = $recipe->total_weight_grams > 0 ? ($recipe->total_weight_grams / $recipe->servings) * $servings : (100.0 * $servings);
        $cals = (int) round($recipe->calories_per_serving * $servings);
        $prot = round(((float) $recipe->protein_per_serving_g) * $servings, 1);
        $carbs = round(((float) $recipe->carbs_per_serving_g) * $servings, 1);
        $fat = round(((float) $recipe->fat_per_serving_g) * $servings, 1);

        return MealEntry::create([
            'client_id' => $clientId,
            'meal_id' => $meal->id,
            'food_id' => null,
            'portion_id' => null,
            'custom_name' => $recipe->name,
            'quantity' => $servings,
            'unit' => $servings == 1.0 ? 'porción de receta' : 'porciones de receta',
            'grams' => $portionWeightG,
            'calories_snapshot' => $cals,
            'protein_snapshot' => $prot,
            'carbs_snapshot' => $carbs,
            'fat_snapshot' => $fat,
            'source' => 'recipe',
            'version' => 1,
        ]);
    }
}
