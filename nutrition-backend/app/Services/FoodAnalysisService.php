<?php

namespace App\Services;

use App\DTOs\FoodMatchCandidate;
use App\Models\AiPhotoAnalysis;
use App\Models\AiPhotoAnalysisItem;
use App\Models\Food;
use App\Models\User;
use App\Services\Ai\AiVisionManager;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use RuntimeException;

class FoodAnalysisService
{
    public function __construct(
        private ImageStorageService $imageStorageService,
        private AiVisionManager $aiVisionManager,
        private FoodMatchingService $foodMatchingService,
        private NutritionCalculatorService $nutritionCalculatorService,
        private DiaryService $diaryService
    ) {}

    /**
     * Complete pipeline: upload image -> AI Vision analysis -> Food matching & nutrition calculation -> Save.
     *
     * @param  array<string, mixed>  $context
     */
    public function analyzePhoto(UploadedFile $file, User $user, array $context = []): AiPhotoAnalysis
    {
        $upload = $this->imageStorageService->storePrivateUpload($file, $user, 24);
        $base64 = $this->imageStorageService->getImageBase64($upload);
        $mimeType = $upload->mime_type;

        $analysis = AiPhotoAnalysis::create([
            'user_id' => $user->id,
            'upload_id' => $upload->id,
            'status' => 'processing',
            'provider' => 'openai',
            'model' => 'gpt-4o-mini',
            'context' => $context,
        ]);

        try {
            $visionResult = $this->aiVisionManager->driver()->analyzeFoodImage($base64, $mimeType, $context);

            return DB::transaction(function () use ($analysis, $visionResult, $context) {
                $totalCals = 0;
                $totalProtein = 0.0;
                $totalCarbs = 0.0;
                $totalFat = 0.0;

                foreach ($visionResult->items as $item) {
                    $candidates = $this->foodMatchingService->findCandidates(
                        name: $item->name,
                        preparation: $item->preparationMethod,
                        locale: $context['locale'] ?? 'DO',
                        limit: 3
                    );

                    $bestMatch = ! empty($candidates) && $candidates[0]->score >= 0.70 ? $candidates[0] : null;

                    // If canonical food matched, calculate nutrients deterministically based on weight
                    if ($bestMatch) {
                        $food = $bestMatch->food;
                        $weightG = $item->estimatedWeightGrams > 0 ? $item->estimatedWeightGrams : 100.0;
                        $cals100g = $food->getNutrientAmount('ENERGY_KCAL');
                        $prot100g = $food->getNutrientAmount('PROTEIN_G');
                        $carbs100g = $food->getNutrientAmount('CARBS_G');
                        $fat100g = $food->getNutrientAmount('FAT_G');

                        $itemCals = (int) round(($cals100g * $weightG) / 100.0);
                        $itemProt = round(($prot100g * $weightG) / 100.0, 2);
                        $itemCarbs = round(($carbs100g * $weightG) / 100.0, 2);
                        $itemFat = round(($fat100g * $weightG) / 100.0, 2);
                        $matchedFoodId = $food->id;
                        $matchedName = $food->canonical_name;
                    } else {
                        // Use AI Vision estimated numbers
                        $itemCals = $item->estimatedCalories;
                        $itemProt = $item->estimatedProteinG;
                        $itemCarbs = $item->estimatedCarbsG;
                        $itemFat = $item->estimatedFatG;
                        $matchedFoodId = null;
                        $matchedName = null;
                    }

                    $candidatesPayload = array_map(fn (FoodMatchCandidate $c) => $c->toArray(), $candidates);

                    AiPhotoAnalysisItem::create([
                        'analysis_id' => $analysis->id,
                        'food_id' => $matchedFoodId,
                        'name' => $item->name,
                        'matched_name' => $matchedName,
                        'estimated_weight_grams' => $item->estimatedWeightGrams,
                        'portion_description' => $item->portionDescription,
                        'preparation_method' => $item->preparationMethod,
                        'confidence' => $item->confidence,
                        'calories' => $itemCals,
                        'protein_g' => $itemProt,
                        'carbs_g' => $itemCarbs,
                        'fat_g' => $itemFat,
                        'candidates' => $candidatesPayload,
                    ]);

                    $totalCals += $itemCals;
                    $totalProtein += $itemProt;
                    $totalCarbs += $itemCarbs;
                    $totalFat += $itemFat;
                }

                $analysis->update([
                    'status' => 'completed',
                    'dish_name' => $visionResult->dishName,
                    'summary' => $visionResult->summary,
                    'confidence_score' => $visionResult->confidenceScore,
                    'provider' => $visionResult->provider,
                    'model' => $visionResult->model,
                    'prompt_tokens' => $visionResult->promptTokens,
                    'completion_tokens' => $visionResult->completionTokens,
                    'estimated_cost_usd' => $visionResult->estimatedCostUsd,
                    'total_calories' => $totalCals,
                    'total_protein_g' => round($totalProtein, 2),
                    'total_carbs_g' => round($totalCarbs, 2),
                    'total_fat_g' => round($totalFat, 2),
                ]);

                return $analysis->load(['items.food.brand', 'items.food.portions']);
            });
        } catch (\Throwable $e) {
            $analysis->update(['status' => 'failed', 'summary' => $e->getMessage()]);
            throw $e;
        }
    }

    /**
     * Get analysis by ID for user.
     */
    public function getAnalysis(int $id, User $user): AiPhotoAnalysis
    {
        return AiPhotoAnalysis::where('id', $id)
            ->where('user_id', $user->id)
            ->with(['items.food.brand', 'items.food.portions', 'upload'])
            ->firstOrFail();
    }

    /**
     * Confirm analysis items with user adjustments and log directly into daily diary.
     *
     * @param  array<int, array<string, mixed>>  $items
     * @return array<int, mixed>
     */
    public function confirmAnalysisAndLogToDiary(
        int $analysisId,
        User $user,
        array $items,
        string $date,
        string $mealType
    ): array {
        $analysis = $this->getAnalysis($analysisId, $user);
        $loggedEntries = [];

        foreach ($items as $itemData) {
            $foodId = isset($itemData['food_id']) ? (int) $itemData['food_id'] : null;
            $name = (string) ($itemData['name'] ?? 'Alimento IA');
            $quantity = (float) ($itemData['quantity'] ?? 1.0);
            $weightGrams = (float) ($itemData['weight_grams'] ?? 100.0);
            $cals = (int) ($itemData['calories'] ?? 0);
            $protein = (float) ($itemData['protein_g'] ?? 0.0);
            $carbs = (float) ($itemData['carbs_g'] ?? 0.0);
            $fat = (float) ($itemData['fat_g'] ?? 0.0);
            $portionId = isset($itemData['portion_id']) ? (int) $itemData['portion_id'] : null;

            $entry = $this->diaryService->addEntry(
                user: $user,
                date: $date,
                data: [
                    'meal_type' => $mealType,
                    'food_id' => $foodId,
                    'portion_id' => $portionId,
                    'quantity' => $quantity,
                    'unit' => 'g',
                    'grams' => $weightGrams,
                    'custom_name' => $name,
                    'calories' => $cals,
                    'protein_g' => $protein,
                    'carbs_g' => $carbs,
                    'fat_g' => $fat,
                    'source' => 'ai_photo',
                ]
            );

            $loggedEntries[] = $entry;
        }

        return $loggedEntries;
    }
}

