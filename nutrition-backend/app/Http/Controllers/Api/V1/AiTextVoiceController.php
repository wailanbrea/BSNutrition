<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Services\Ai\AiTextParserService;
use App\Services\AiQuotaService;
use App\Services\FoodAnalysisService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AiTextVoiceController extends Controller
{
    public function __construct(
        private AiTextParserService $textParserService,
        private FoodAnalysisService $analysisService,
        private AiQuotaService $quotaService
    ) {}

    /**
     * Parse natural language text or transcribed speech into structured food items.
     */
    public function parseText(Request $request): JsonResponse
    {
        $this->quotaService->checkAndConsumeTextQuota($request->user());

        $request->validate([

            'text' => ['required', 'string', 'min:3', 'max:1000'],
            'locale' => ['nullable', 'string', 'max:10'],
            'meal_type' => ['nullable', 'string', 'in:breakfast,lunch,dinner,snack'],
        ]);

        $user = $request->user();
        $text = $request->input('text');
        $locale = $request->input('locale', 'DO');
        $mealType = $request->input('meal_type', 'lunch');

        $result = $this->textParserService->parseMealText($user, $text, $locale, $mealType);

        return response()->json([
            'status' => 'success',
            'data' => $result,
        ]);
    }

    /**
     * Confirm analyzed text items and persist into user's daily diary.
     */
    public function confirm(Request $request, int $id): JsonResponse
    {
        $request->validate([
            'date' => ['required', 'date_format:Y-m-d'],
            'meal_type' => ['required', 'string', 'in:breakfast,lunch,dinner,snack'],
            'items' => ['required', 'array', 'min:1'],
            'items.*.name' => ['required', 'string'],
            'items.*.food_id' => ['nullable', 'integer', 'exists:foods,id'],
            'items.*.portion_id' => ['nullable', 'integer', 'exists:food_portions,id'],
            'items.*.quantity' => ['nullable', 'numeric', 'min:0.1'],
            'items.*.weight_grams' => ['nullable', 'numeric', 'min:1'],
            'items.*.calories' => ['required', 'integer', 'min:0'],
            'items.*.protein_g' => ['required', 'numeric', 'min:0'],
            'items.*.carbs_g' => ['required', 'numeric', 'min:0'],
            'items.*.fat_g' => ['required', 'numeric', 'min:0'],
        ]);

        $user = $request->user();
        $loggedEntries = $this->analysisService->confirmAnalysisAndLogToDiary(
            analysisId: $id,
            user: $user,
            items: $request->input('items'),
            date: $request->input('date'),
            mealType: $request->input('meal_type')
        );


        return response()->json([
            'status' => 'success',
            'message' => 'Alimentos registrados exitosamente en el diario.',
            'data' => [
                'analysis_id' => $id,
                'logged_entries_count' => count($loggedEntries),
            ],
        ]);
    }
}
