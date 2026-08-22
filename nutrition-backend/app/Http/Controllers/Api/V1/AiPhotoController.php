<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Services\FoodAnalysisService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AiPhotoController extends Controller
{
    public function __construct(
        private FoodAnalysisService $analysisService
    ) {}

    /**
     * Upload food photo and perform end-to-end AI vision analysis and food matching.
     */
    public function analyze(Request $request): JsonResponse
    {
        $request->validate([
            'image' => ['required', 'file', 'image', 'max:10240'], // 10MB max
            'locale' => ['nullable', 'string', 'max:10'],
            'meal_type' => ['nullable', 'string', 'in:breakfast,lunch,dinner,snack'],
        ]);

        $analysis = $this->analysisService->analyzePhoto(
            file: $request->file('image'),
            user: $request->user(),
            context: [
                'locale' => $request->input('locale', 'DO'),
                'meal_type' => $request->input('meal_type', 'lunch'),
            ]
        );

        return response()->json([
            'status' => 'success',
            'data' => $this->formatAnalysisResponse($analysis),
        ]);
    }

    /**
     * Get existing analysis result by ID.
     */
    public function show(Request $request, int $id): JsonResponse
    {
        $analysis = $this->analysisService->getAnalysis($id, $request->user());

        return response()->json([
            'status' => 'success',
            'data' => $this->formatAnalysisResponse($analysis),
        ]);
    }

    /**
     * Confirm analysis items with user adjustments and log to daily diary.
     */
    public function confirm(Request $request, int $id): JsonResponse
    {
        $request->validate([
            'date' => ['required', 'date_format:Y-m-d'],
            'meal_type' => ['required', 'string', 'in:breakfast,lunch,dinner,snack'],
            'items' => ['required', 'array', 'min:1'],
            'items.*.name' => ['required', 'string', 'max:255'],
            'items.*.food_id' => ['nullable', 'integer', 'exists:foods,id'],
            'items.*.portion_id' => ['nullable', 'integer', 'exists:food_portions,id'],
            'items.*.quantity' => ['required', 'numeric', 'min:0.01'],
            'items.*.weight_grams' => ['required', 'numeric', 'min:1'],
            'items.*.calories' => ['required', 'integer', 'min:0'],
            'items.*.protein_g' => ['required', 'numeric', 'min:0'],
            'items.*.carbs_g' => ['required', 'numeric', 'min:0'],
            'items.*.fat_g' => ['required', 'numeric', 'min:0'],
        ]);

        $loggedEntries = $this->analysisService->confirmAnalysisAndLogToDiary(
            analysisId: $id,
            user: $request->user(),
            items: $request->input('items'),
            date: $request->input('date'),
            mealType: $request->input('meal_type')
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Alimentos registrados correctamente en el diario.',
            'data' => [
                'analysis_id' => $id,
                'logged_entries_count' => count($loggedEntries),
                'entries' => $loggedEntries,
            ],
        ], 201);
    }

    /**
     * Format analysis model into consistent API response JSON.
     */
    private function formatAnalysisResponse($analysis): array
    {
        return [
            'id' => $analysis->id,
            'status' => $analysis->status,
            'dish_name' => $analysis->dish_name,
            'summary' => $analysis->summary,
            'confidence_score' => $analysis->confidence_score,
            'provider' => $analysis->provider,
            'model' => $analysis->model,
            'totals' => [
                'calories' => $analysis->total_calories,
                'protein_g' => $analysis->total_protein_g,
                'carbs_g' => $analysis->total_carbs_g,
                'fat_g' => $analysis->total_fat_g,
            ],
            'items' => $analysis->items->map(function ($item) {
                return [
                    'id' => $item->id,
                    'food_id' => $item->food_id,
                    'name' => $item->name,
                    'matched_name' => $item->matched_name,
                    'estimated_weight_grams' => $item->estimated_weight_grams,
                    'portion_description' => $item->portion_description,
                    'preparation_method' => $item->preparation_method,
                    'confidence' => $item->confidence,
                    'calories' => $item->calories,
                    'protein_g' => $item->protein_g,
                    'carbs_g' => $item->carbs_g,
                    'fat_g' => $item->fat_g,
                    'candidates' => $item->candidates ?? [],
                ];
            }),
            'created_at' => $analysis->created_at?->toIso8601String(),
        ];
    }
}
