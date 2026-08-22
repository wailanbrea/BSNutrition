<?php

namespace App\Services;

use App\Models\Diary;
use App\Models\NutritionGoal;
use App\Models\User;
use App\Models\WaterLog;
use App\Models\WeightLog;
use Carbon\Carbon;

class StatisticsService
{
    /**
     * Compute comprehensive nutrition, hydration and weight statistics for a user.
     *
     * @return array<string, mixed>
     */
    public function getSummary(User $user, string $period = '7d'): array
    {
        $daysCount = match ($period) {
            '30d' => 30,
            '90d' => 90,
            default => 7,
        };

        $endDate = Carbon::today();
        $startDate = Carbon::today()->subDays($daysCount - 1);

        $activeGoal = NutritionGoal::where('user_id', $user->id)
            ->orderBy('effective_from', 'desc')
            ->first();

        $targetCalories = $activeGoal ? $activeGoal->calorie_target : 2000;
        $targetProtein = $activeGoal ? (float) $activeGoal->protein_target_g : 150.0;
        $targetCarbs = $activeGoal ? (float) $activeGoal->carbohydrate_target_g : 200.0;
        $targetFat = $activeGoal ? (float) $activeGoal->fat_target_g : 65.0;
        $targetWaterMl = $activeGoal?->water_target_ml ?? $user->profile?->water_target_ml ?? 2500;


        // Fetch diaries with meals and entries in range
        $diaries = Diary::where('user_id', $user->id)
            ->whereBetween('diary_date', [$startDate->format('Y-m-d'), $endDate->format('Y-m-d')])
            ->with(['meals.entries'])
            ->get()
            ->keyBy(fn ($d) => $d->diary_date->format('Y-m-d'));

        // Fetch water logs
        $waterLogs = WaterLog::where('user_id', $user->id)
            ->whereBetween('log_date', [$startDate->format('Y-m-d'), $endDate->format('Y-m-d')])
            ->get()
            ->groupBy(fn ($w) => $w->log_date->format('Y-m-d'));

        // Fetch weight logs
        $weightLogs = WeightLog::where('user_id', $user->id)
            ->whereBetween('log_date', [$startDate->format('Y-m-d'), $endDate->format('Y-m-d')])
            ->orderBy('log_date', 'asc')
            ->get();

        $dailyBreakdown = [];
        $totalCaloriesSum = 0;
        $totalProteinSum = 0.0;
        $totalCarbsSum = 0.0;
        $totalFatSum = 0.0;
        $totalWaterSum = 0;
        $trackedDaysCount = 0;
        $adherentDaysCount = 0;

        $current = $startDate->copy();
        while ($current->lte($endDate)) {
            $dateStr = $current->format('Y-m-d');
            $diary = $diaries->get($dateStr);
            $dayWater = $waterLogs->get($dateStr);

            $dayCals = 0;
            $dayProt = 0.0;
            $dayCarbs = 0.0;
            $dayFat = 0.0;

            if ($diary) {
                foreach ($diary->meals as $meal) {
                    foreach ($meal->entries as $entry) {
                        $dayCals += $entry->calories_snapshot;
                        $dayProt += (float) $entry->protein_snapshot;
                        $dayCarbs += (float) $entry->carbs_snapshot;
                        $dayFat += (float) $entry->fat_snapshot;
                    }
                }
            }

            $waterMl = $dayWater ? $dayWater->sum('amount_ml') : 0;
            $hasActivity = ($dayCals > 0 || $waterMl > 0);

            if ($hasActivity) {
                $trackedDaysCount++;
                $totalCaloriesSum += $dayCals;
                $totalProteinSum += $dayProt;
                $totalCarbsSum += $dayCarbs;
                $totalFatSum += $dayFat;
                $totalWaterSum += $waterMl;

                // Within 15% range of target
                if (abs($dayCals - $targetCalories) <= ($targetCalories * 0.15)) {
                    $adherentDaysCount++;
                }
            }

            $dailyBreakdown[] = [
                'date' => $dateStr,
                'day_of_week' => $current->format('D'),
                'calories' => $dayCals,
                'protein_g' => round($dayProt, 1),
                'carbs_g' => round($dayCarbs, 1),
                'fat_g' => round($dayFat, 1),
                'water_ml' => $waterMl,
                'target_calories' => $targetCalories,
                'target_water_ml' => $targetWaterMl,
            ];

            $current->addDay();
        }

        $avgCalories = $trackedDaysCount > 0 ? (int) round($totalCaloriesSum / $trackedDaysCount) : 0;
        $avgProtein = $trackedDaysCount > 0 ? round($totalProteinSum / $trackedDaysCount, 1) : 0.0;
        $avgCarbs = $trackedDaysCount > 0 ? round($totalCarbsSum / $trackedDaysCount, 1) : 0.0;
        $avgFat = $trackedDaysCount > 0 ? round($totalFatSum / $trackedDaysCount, 1) : 0.0;
        $avgWater = $trackedDaysCount > 0 ? (int) round($totalWaterSum / $trackedDaysCount) : 0;

        $totalMacroCals = ($avgProtein * 4) + ($avgCarbs * 4) + ($avgFat * 9);
        $macroSplit = [
            'protein_pct' => $totalMacroCals > 0 ? round((($avgProtein * 4) / $totalMacroCals) * 100, 1) : 30.0,
            'carbs_pct' => $totalMacroCals > 0 ? round((($avgCarbs * 4) / $totalMacroCals) * 100, 1) : 45.0,
            'fat_pct' => $totalMacroCals > 0 ? round((($avgFat * 9) / $totalMacroCals) * 100, 1) : 25.0,
        ];

        $adherenceRate = $trackedDaysCount > 0 ? round(($adherentDaysCount / $trackedDaysCount) * 100, 1) : 0.0;

        $startWeight = $weightLogs->first()?->weight_kg;
        $currentWeight = $weightLogs->last()?->weight_kg ?? $user->profile?->current_weight;
        $weightChange = ($startWeight !== null && $currentWeight !== null) ? round(((float) $currentWeight - (float) $startWeight), 2) : 0.0;


        return [
            'period' => $period,
            'start_date' => $startDate->format('Y-m-d'),
            'end_date' => $endDate->format('Y-m-d'),
            'total_days' => $daysCount,
            'tracked_days' => $trackedDaysCount,
            'targets' => [
                'calories' => $targetCalories,
                'protein_g' => $targetProtein,
                'carbs_g' => $targetCarbs,
                'fat_g' => $targetFat,
                'water_ml' => $targetWaterMl,
            ],
            'averages' => [
                'calories' => $avgCalories,
                'protein_g' => $avgProtein,
                'carbs_g' => $avgCarbs,
                'fat_g' => $avgFat,
                'water_ml' => $avgWater,
            ],
            'macro_split' => $macroSplit,
            'adherence_rate' => $adherenceRate,
            'weight_summary' => [
                'start_weight_kg' => $startWeight ? (float) $startWeight : null,
                'current_weight_kg' => $currentWeight ? (float) $currentWeight : null,
                'change_kg' => $weightChange,
            ],
            'daily_breakdown' => $dailyBreakdown,
        ];
    }
}
