<?php

namespace App\Services;

use Carbon\Carbon;

class NutritionGoalCalculatorService
{
    public const CALCULATION_VERSION = 'mifflin_v1.0';

    public const ACTIVITY_MULTIPLIERS = [
        'sedentary' => 1.200,
        'light' => 1.375,
        'moderate' => 1.550,
        'active' => 1.725,
        'very_active' => 1.900,
    ];

    public const MACRO_DISTRIBUTIONS = [
        'lose_weight' => [
            'protein' => 0.30,
            'carbs' => 0.40,
            'fat' => 0.30,
        ],
        'maintain_weight' => [
            'protein' => 0.25,
            'carbs' => 0.45,
            'fat' => 0.30,
        ],
        'gain_muscle' => [
            'protein' => 0.30,
            'carbs' => 0.45,
            'fat' => 0.25,
        ],
        'gain_weight' => [
            'protein' => 0.30,
            'carbs' => 0.45,
            'fat' => 0.25,
        ],
    ];

    /**
     * Calculate BMR, TDEE, Calorie targets and Macronutrients according to ADR-009.
     *
     * @param  array<string, mixed>  $params
     * @return array<string, mixed>
     */
    public function calculate(array $params): array
    {
        $sex = strtolower((string) ($params['sex'] ?? 'male'));
        $birthDate = $params['birth_date'] ?? null;
        $heightCm = (float) ($params['height'] ?? 175.0);
        $weightKg = (float) ($params['current_weight'] ?? 70.0);
        $activityLevel = (string) ($params['activity_level'] ?? 'sedentary');
        $goalType = (string) ($params['goal_type'] ?? 'maintain_weight');
        $weeklyGoalRate = isset($params['weekly_goal_rate']) ? (float) $params['weekly_goal_rate'] : 0.5;

        // 1. Calculate Age
        $age = 25;
        if (! empty($birthDate)) {
            try {
                $age = Carbon::parse($birthDate)->age;
            } catch (\Exception) {
                $age = 25;
            }
        }

        // 2. Calculate BMR (Mifflin-St Jeor)
        if ($sex === 'female') {
            $bmr = (10 * $weightKg) + (6.25 * $heightCm) - (5 * $age) - 161;
        } else {
            $bmr = (10 * $weightKg) + (6.25 * $heightCm) - (5 * $age) + 5;
        }

        // 3. TDEE
        $multiplier = self::ACTIVITY_MULTIPLIERS[$activityLevel] ?? self::ACTIVITY_MULTIPLIERS['sedentary'];
        $tdee = $bmr * $multiplier;

        // 4. Target Calories based on Goal
        $dailyAdjustment = $weeklyGoalRate * 1100.0;

        if ($goalType === 'lose_weight') {
            $targetCalories = $tdee - $dailyAdjustment;
            // Physiological minimums
            $minCalories = ($sex === 'female') ? 1200 : 1500;
            if ($targetCalories < $minCalories) {
                $targetCalories = $minCalories;
            }
        } elseif ($goalType === 'gain_muscle' || $goalType === 'gain_weight') {
            $targetCalories = $tdee + $dailyAdjustment;
        } else {
            $targetCalories = $tdee;
        }

        $calorieTarget = (int) round($targetCalories);

        // 5. Macronutrient distribution
        $macroSplit = self::MACRO_DISTRIBUTIONS[$goalType] ?? self::MACRO_DISTRIBUTIONS['maintain_weight'];

        $proteinGrams = round(($calorieTarget * $macroSplit['protein']) / 4, 2);
        $carbsGrams = round(($calorieTarget * $macroSplit['carbs']) / 4, 2);
        $fatGrams = round(($calorieTarget * $macroSplit['fat']) / 9, 2);

        // 6. Fiber & Water
        $minFiber = ($sex === 'female') ? 25.0 : 38.0;
        $calculatedFiber = ($calorieTarget / 1000.0) * 14.0;
        $fiberGrams = round(max($minFiber, $calculatedFiber), 2);

        $waterMl = (int) round(max(2000, $weightKg * 35.0));

        return [
            'bmr' => round($bmr, 2),
            'tdee' => round($tdee, 2),
            'calorie_target' => $calorieTarget,
            'protein_target_g' => $proteinGrams,
            'carbohydrate_target_g' => $carbsGrams,
            'fat_target_g' => $fatGrams,
            'fiber_target_g' => $fiberGrams,
            'water_target_ml' => $waterMl,
            'calculation_version' => self::CALCULATION_VERSION,
        ];
    }
}
