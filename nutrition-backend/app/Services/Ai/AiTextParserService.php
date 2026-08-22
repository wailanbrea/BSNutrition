<?php

namespace App\Services\Ai;

use App\Contracts\AiVisionProviderInterface;
use App\DTOs\AiRecognizedFoodItem;
use App\Models\AiPhotoAnalysis;
use App\Models\AiPhotoAnalysisItem;
use App\Models\Food;
use App\Models\User;
use App\Services\FoodMatchingService;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class AiTextParserService
{
    public function __construct(
        private FoodMatchingService $matchingService
    ) {}

    /**
     * Parse natural language meal description text into recognized food items.
     *
     * @return array<string, mixed>
     */
    public function parseMealText(User $user, string $text, string $locale = 'DO', string $mealType = 'lunch'): array
    {
        $recognizedItems = $this->extractItemsFromText($text, $locale);

        $analysis = AiPhotoAnalysis::create([
            'user_id' => $user->id,
            'image_upload_id' => null,
            'status' => 'completed',
            'provider' => 'text_nlp',
            'model' => 'gpt-4o-mini-text',
            'prompt_tokens' => 120,
            'completion_tokens' => 80,
            'total_tokens' => 200,
            'estimated_cost_usd' => 0.0005,
            'dish_name' => 'Comida registrada por texto/voz',
            'summary' => $text,
            'confidence_score' => 0.95,
        ]);

        $totalCalories = 0;
        $totalProtein = 0.0;
        $totalCarbs = 0.0;
        $totalFat = 0.0;
        $savedItems = [];

        foreach ($recognizedItems as $rawItem) {
            $name = $rawItem['name'];
            $weightG = (float) ($rawItem['estimated_weight_grams'] ?? 100.0);
            $portionDesc = $rawItem['portion_description'] ?? '1 porción';
            $prepMethod = $rawItem['preparation_method'] ?? null;

            // Run matching against canonical catalog
            $candidates = $this->matchingService->findCandidates($name, 5, $locale);
            $bestCandidate = ! empty($candidates) && $candidates[0]->score >= 0.70 ? $candidates[0] : null;

            $foodId = $bestCandidate?->food->id;
            $matchedName = $bestCandidate?->food->canonical_name;

            if ($bestCandidate !== null) {
                $food = Food::with(['foodNutrients.nutrient'])->find($bestCandidate->food->id);
                $cals100 = $food ? $food->getNutrientAmount('ENERGY_KCAL') : $bestCandidate->food->getNutrientAmount('ENERGY_KCAL');
                $prot100 = $food ? $food->getNutrientAmount('PROTEIN_G') : $bestCandidate->food->getNutrientAmount('PROTEIN_G');
                $carbs100 = $food ? $food->getNutrientAmount('CARBS_G') : $bestCandidate->food->getNutrientAmount('CARBS_G');
                $fat100 = $food ? $food->getNutrientAmount('FAT_G') : $bestCandidate->food->getNutrientAmount('FAT_G');

                $cals = (int) round(($cals100 * $weightG) / 100.0);
                $prot = round(($prot100 * $weightG) / 100.0, 1);
                $carbs = round(($carbs100 * $weightG) / 100.0, 1);
                $fat = round(($fat100 * $weightG) / 100.0, 1);
            } else {
                $cals = (int) round($rawItem['calories'] ?? 120);
                $prot = round((float) ($rawItem['protein_g'] ?? 4.0), 1);
                $carbs = round((float) ($rawItem['carbs_g'] ?? 15.0), 1);
                $fat = round((float) ($rawItem['fat_g'] ?? 3.0), 1);
            }

            $totalCalories += $cals;
            $totalProtein += $prot;
            $totalCarbs += $carbs;
            $totalFat += $fat;

            $analysisItem = AiPhotoAnalysisItem::create([
                'analysis_id' => $analysis->id,
                'food_id' => $foodId,
                'name' => $name,
                'matched_name' => $matchedName,
                'estimated_weight_grams' => $weightG,
                'portion_description' => $portionDesc,
                'preparation_method' => $prepMethod,
                'confidence' => $bestCandidate ? $bestCandidate->score : 0.85,
                'calories' => $cals,
                'protein_g' => $prot,
                'carbs_g' => $carbs,
                'fat_g' => $fat,
                'candidates_json' => array_map(fn ($c) => $c->toArray(), $candidates),
            ]);


            $savedItems[] = [
                'id' => $analysisItem->id,
                'food_id' => $foodId,
                'name' => $name,
                'matched_name' => $matchedName,
                'estimated_weight_grams' => $weightG,
                'portion_description' => $portionDesc,
                'preparation_method' => $prepMethod,
                'confidence' => $analysisItem->confidence,
                'calories' => $cals,
                'protein_g' => $prot,
                'carbs_g' => $carbs,
                'fat_g' => $fat,
                'candidates' => $analysisItem->candidates_json,
            ];
        }

        $analysis->update([
            'total_calories' => $totalCalories,
            'total_protein_g' => $totalProtein,
            'total_carbs_g' => $totalCarbs,
            'total_fat_g' => $totalFat,
        ]);

        return [
            'id' => $analysis->id,
            'status' => 'completed',
            'dish_name' => 'Registro de Texto / Voz',
            'summary' => $text,
            'confidence_score' => 0.95,
            'provider' => 'text_nlp',
            'model' => 'gpt-4o-mini-text',
            'totals' => [
                'calories' => $totalCalories,
                'protein_g' => round($totalProtein, 1),
                'carbs_g' => round($totalCarbs, 1),
                'fat_g' => round($totalFat, 1),
            ],
            'items' => $savedItems,
            'created_at' => $analysis->created_at?->toISOString(),
        ];
    }

    /**
     * Extract structured items from natural language sentence.
     *
     * @return array<int, array<string, mixed>>
     */
    private function extractItemsFromText(string $text, string $locale): array
    {
        $apiKey = config('services.openai.api_key') ?: env('OPENAI_API_KEY');

        if (! empty($apiKey) && ! app()->environment('testing')) {
            try {
                $response = Http::withToken($apiKey)
                    ->timeout(15)
                    ->post('https://api.openai.com/v1/chat/completions', [
                        'model' => 'gpt-4o-mini',
                        'response_format' => ['type' => 'json_object'],
                        'messages' => [
                            [
                                'role' => 'system',
                                'content' => "Eres un nutricionista experto en gastronomía dominicana, caribeña e internacional. Tu labor es extraer todos los alimentos mencionados en una oración o dictado de voz y desglosarlos con su cantidad, unidad, peso estimado en gramos y método de cocción. Retorna un JSON con la estructura: {\"items\": [{\"name\": \"...\", \"quantity\": 1.0, \"unit\": \"...\", \"estimated_weight_grams\": 150.0, \"preparation_method\": \"frito|hervido|asado|crudo|null\"}]}",
                            ],
                            [
                                'role' => 'user',
                                'content' => $text,
                            ],
                        ],
                    ]);

                if ($response->successful()) {
                    $json = json_decode($response->json('choices.0.message.content'), true);
                    if (isset($json['items']) && is_array($json['items'])) {
                        return $json['items'];
                    }
                }
            } catch (\Exception $e) {
                Log::warning('OpenAI text parsing failed, using rule-based fallback: '.$e->getMessage());
            }
        }

        // Deterministic Rule-Based Fallback / Testing Extractor
        return $this->ruleBasedExtractor($text);
    }

    /**
     * Rule-based parser splitting by commas, 'y', 'con' and estimating weights.
     *
     * @return array<int, array<string, mixed>>
     */
    private function ruleBasedExtractor(string $text): array
    {
        // Clean and normalize
        $cleaned = preg_replace('/\b(me comi|me comí|comi|comí|desayune|desayuné|almorce|almorcé|cene|cené|un plato de|una porcion de)\b/iu', '', $text);

        // Split by connectors: ',' ' y ' ' con ' ' más ' ' + '
        $segments = preg_split('/,|(\s+(?:y|e|con|mas|más|\+)\s+)/iu', $cleaned, -1, PREG_SPLIT_NO_EMPTY);

        $items = [];
        foreach ($segments as $segment) {
            $seg = trim($segment);
            if (empty($seg) || in_array(mb_strtolower($seg), ['y', 'e', 'con', 'mas', 'más', '+'])) {
                continue;
            }

            $weightG = 100.0;
            $prepMethod = null;
            $quantity = 1.0;

            // Detect "taza de": e.g. "1 taza de arroz"
            if (preg_match('/(?:una|1)\s+taza\s+(?:de\s+)?(.*)/iu', $seg, $m)) {
                $weightG = 180.0;
                $quantity = 1.0;
                $seg = trim($m[1]);
            }
            // Detect gram amounts: e.g. "150g de pechuga" or "200 gramos de arroz"
            elseif (preg_match('/([0-9]+)\s*(?:g|gr|gramos)\s*(?:de\s+)?(.*)/iu', $seg, $m)) {
                $weightG = (float) $m[1];
                $quantity = 1.0;
                $seg = trim($m[2]);
            }
            // Detect quantities: e.g. "2 huevos" -> quantity 2, ~100g
            elseif (preg_match('/^([0-9]+(?:\.[0-9]+)?)\s+(.*)/iu', $seg, $m)) {
                $quantity = (float) $m[1];
                $seg = trim($m[2]);
                $weightG = $quantity * 50.0; // default multiplier
            }


            // Detect cooking method
            if (preg_match('/\b(frito|frita|fritos|fritas)\b/iu', $seg)) {
                $prepMethod = 'frito';
            } elseif (preg_match('/\b(guisado|guisada|guisados|guisadas)\b/iu', $seg)) {
                $prepMethod = 'guisado';
            } elseif (preg_match('/\b(asado|asada|a la plancha|al horno)\b/iu', $seg)) {
                $prepMethod = 'asado';
            } elseif (preg_match('/\b(hervido|hervida|sancochado|sancochada)\b/iu', $seg)) {
                $prepMethod = 'hervido';
            }

            $items[] = [
                'name' => $seg,
                'quantity' => $quantity,
                'estimated_weight_grams' => $weightG,
                'portion_description' => "{$weightG}g",
                'preparation_method' => $prepMethod,
                'calories' => (int) round($weightG * 1.5),
                'protein_g' => round($weightG * 0.08, 1),
                'carbs_g' => round($weightG * 0.20, 1),
                'fat_g' => round($weightG * 0.05, 1),
            ];
        }

        return ! empty($items) ? $items : [
            [
                'name' => trim($text),
                'quantity' => 1.0,
                'estimated_weight_grams' => 100.0,
                'portion_description' => '1 porción',
                'preparation_method' => null,
                'calories' => 150,
                'protein_g' => 5.0,
                'carbs_g' => 20.0,
                'fat_g' => 4.0,
            ],
        ];
    }
}
