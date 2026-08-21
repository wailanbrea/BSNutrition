<?php

namespace App\Services\Ai;

use App\Contracts\AiVisionProviderInterface;
use App\DTOs\AiFoodAnalysisResult;
use App\DTOs\AiRecognizedFoodItem;

class MockVisionProvider implements AiVisionProviderInterface
{
    /**
     * Return mock analysis for tests and simulated offline AI recognition.
     */
    public function analyzeFoodImage(string $base64Image, string $mimeType = 'image/jpeg', array $context = []): AiFoodAnalysisResult
    {
        $mockData = [
            'dish_name' => 'Mangú con Los Tres Golpes',
            'summary' => 'Plato tradicional dominicano compuesto por mangú de plátano verde, salami frito, huevo frito y queso frito con cebollitas encurtidas.',
            'confidence_score' => 0.94,
            'items' => [
                [
                    'name' => 'Mangú de plátano verde',
                    'estimated_weight_grams' => 200.0,
                    'portion_description' => '1 taza colmada',
                    'confidence' => 0.95,
                    'estimated_calories' => 310,
                    'estimated_protein_g' => 3.0,
                    'estimated_carbs_g' => 62.0,
                    'estimated_fat_g' => 6.4,
                    'preparation_method' => 'hervido y majado',
                ],
                [
                    'name' => 'Salami dominicano frito',
                    'estimated_weight_grams' => 60.0,
                    'portion_description' => '2 rodajas',
                    'confidence' => 0.92,
                    'estimated_calories' => 195,
                    'estimated_protein_g' => 9.5,
                    'estimated_carbs_g' => 1.5,
                    'estimated_fat_g' => 17.0,
                    'preparation_method' => 'frito',
                ],
                [
                    'name' => 'Huevo frito',
                    'estimated_weight_grams' => 50.0,
                    'portion_description' => '1 unidad',
                    'confidence' => 0.96,
                    'estimated_calories' => 90,
                    'estimated_protein_g' => 6.3,
                    'estimated_carbs_g' => 0.4,
                    'estimated_fat_g' => 7.0,
                    'preparation_method' => 'frito',
                ],
                [
                    'name' => 'Queso frito dominicano',
                    'estimated_weight_grams' => 50.0,
                    'portion_description' => '1 rebanada',
                    'confidence' => 0.91,
                    'estimated_calories' => 160,
                    'estimated_protein_g' => 11.0,
                    'estimated_carbs_g' => 1.0,
                    'estimated_fat_g' => 13.0,
                    'preparation_method' => 'frito',
                ],
            ],
            'prompt_tokens' => 850,
            'completion_tokens' => 350,
            'estimated_cost_usd' => 0.00033,
        ];

        return AiFoodAnalysisResult::fromArray($mockData, 'mock', 'mock-vision-v1');
    }
}
