<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\Goal\CalculateGoalRequest;
use App\Http\Requests\Goal\SaveGoalRequest;
use App\Http\Resources\NutritionGoalResource;
use App\Models\NutritionGoal;
use App\Services\NutritionGoalCalculatorService;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class GoalController extends Controller
{
    public function calculate(
        CalculateGoalRequest $request,
        NutritionGoalCalculatorService $calculator
    ): JsonResponse {
        $user = $request->user();
        $profile = $user->profile;

        $params = array_filter([
            'birth_date' => $request->input('birth_date') ?? $profile?->birth_date?->format('Y-m-d'),
            'sex' => $request->input('sex') ?? $profile?->sex,
            'height' => $request->input('height') ?? $profile?->height,
            'current_weight' => $request->input('current_weight') ?? $profile?->current_weight,
            'activity_level' => $request->input('activity_level') ?? $profile?->activity_level,
            'goal_type' => $request->input('goal_type') ?? $profile?->goal_type,
            'weekly_goal_rate' => $request->input('weekly_goal_rate') ?? $profile?->weekly_goal_rate,
        ]);

        $calculated = $calculator->calculate($params);

        return response()->json([
            'calculated_goal' => $calculated,
        ]);
    }

    public function current(
        Request $request,
        NutritionGoalCalculatorService $calculator
    ): JsonResponse {
        $user = $request->user();
        $goal = $user->currentNutritionGoal;

        if (! $goal) {
            $profile = $user->profile;
            $params = [
                'birth_date' => $profile?->birth_date?->format('Y-m-d'),
                'sex' => $profile?->sex,
                'height' => $profile?->height,
                'current_weight' => $profile?->current_weight,
                'activity_level' => $profile?->activity_level,
                'goal_type' => $profile?->goal_type,
                'weekly_goal_rate' => $profile?->weekly_goal_rate,
            ];

            $calculated = $calculator->calculate($params);

            $goal = NutritionGoal::create([
                'user_id' => $user->id,
                'effective_from' => Carbon::today(),
                'calorie_target' => $calculated['calorie_target'],
                'protein_target_g' => $calculated['protein_target_g'],
                'carbohydrate_target_g' => $calculated['carbohydrate_target_g'],
                'fat_target_g' => $calculated['fat_target_g'],
                'fiber_target_g' => $calculated['fiber_target_g'],
                'water_target_ml' => $calculated['water_target_ml'],
                'source' => 'calculated',
                'calculation_version' => $calculated['calculation_version'],
            ]);
        }

        return response()->json([
            'goal' => new NutritionGoalResource($goal),
        ]);
    }

    public function update(SaveGoalRequest $request): JsonResponse
    {
        $user = $request->user();
        $effectiveFrom = $request->input('effective_from')
            ? Carbon::parse($request->input('effective_from'))->format('Y-m-d')
            : Carbon::today()->format('Y-m-d');

        $goal = NutritionGoal::updateOrCreate(
            [
                'user_id' => $user->id,
                'effective_from' => $effectiveFrom,
            ],
            [
                'calorie_target' => $request->input('calorie_target'),
                'protein_target_g' => $request->input('protein_target_g'),
                'carbohydrate_target_g' => $request->input('carbohydrate_target_g'),
                'fat_target_g' => $request->input('fat_target_g'),
                'fiber_target_g' => $request->input('fiber_target_g'),
                'water_target_ml' => $request->input('water_target_ml'),
                'source' => $request->input('source', 'custom'),
                'calculation_version' => NutritionGoalCalculatorService::CALCULATION_VERSION,
            ]
        );

        return response()->json([
            'message' => 'Objetivos nutricionales guardados correctamente.',
            'goal' => new NutritionGoalResource($goal),
        ]);
    }
}
