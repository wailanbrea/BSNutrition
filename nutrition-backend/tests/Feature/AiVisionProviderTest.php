<?php

use App\DTOs\AiFoodAnalysisResult;
use App\DTOs\AiRecognizedFoodItem;
use App\Services\Ai\AiVisionManager;
use App\Services\Ai\MockVisionProvider;
use App\Services\Ai\OpenAiVisionProvider;
use Illuminate\Support\Facades\Http;

test('MockVisionProvider returns complete structured Dominican dish analysis', function () {
    $provider = new MockVisionProvider;
    $result = $provider->analyzeFoodImage('base64_fake_image_data');

    expect($result)->toBeInstanceOf(AiFoodAnalysisResult::class)
        ->and($result->dishName)->toBe('Mangú con Los Tres Golpes')
        ->and($result->items)->toHaveCount(4)
        ->and($result->totalEstimatedCalories)->toBe(755)
        ->and($result->items[0]->name)->toBe('Mangú de plátano verde')
        ->and($result->items[0]->estimatedCalories)->toBe(310)
        ->and($result->confidenceScore)->toBeGreaterThan(0.9);
});

test('OpenAiVisionProvider parses successful JSON response and maps tokens and cost', function () {
    $fakeOpenAiResponse = [
        'choices' => [
            [
                'message' => [
                    'content' => json_encode([
                        'dish_name' => 'Bandera Dominicana',
                        'summary' => 'Arroz blanco, habichuelas rojas guisadas y pollo guisado con ensalada verde.',
                        'confidence_score' => 0.96,
                        'items' => [
                            [
                                'name' => 'Arroz blanco',
                                'estimated_weight_grams' => 180.0,
                                'portion_description' => '1 taza',
                                'confidence' => 0.98,
                                'estimated_calories' => 234,
                                'estimated_protein_g' => 4.5,
                                'estimated_carbs_g' => 50.4,
                                'estimated_fat_g' => 0.8,
                                'preparation_method' => 'hervido con poco aceite',
                            ],
                            [
                                'name' => 'Habichuelas rojas guisadas',
                                'estimated_weight_grams' => 150.0,
                                'portion_description' => '1/2 taza con salsa',
                                'confidence' => 0.95,
                                'estimated_calories' => 165,
                                'estimated_protein_g' => 9.2,
                                'estimated_carbs_g' => 28.5,
                                'estimated_fat_g' => 1.5,
                                'preparation_method' => 'guisadas con sofrito dominicano',
                            ],
                            [
                                'name' => 'Pollo guisado',
                                'estimated_weight_grams' => 140.0,
                                'portion_description' => '1 muslo con salsa',
                                'confidence' => 0.94,
                                'estimated_calories' => 220,
                                'estimated_protein_g' => 26.0,
                                'estimated_carbs_g' => 3.0,
                                'estimated_fat_g' => 11.5,
                                'preparation_method' => 'guisado con sazón y verduras',
                            ],
                        ],
                    ]),
                ],
            ],
        ],
        'usage' => [
            'prompt_tokens' => 1200,
            'completion_tokens' => 400,
        ],
    ];

    Http::fake([
        'https://api.openai.com/v1/chat/completions' => Http::response($fakeOpenAiResponse, 200),
    ]);

    $provider = new OpenAiVisionProvider('sk-test-key', 'gpt-4o-mini');
    $result = $provider->analyzeFoodImage('base64_image_sample');

    expect($result)->toBeInstanceOf(AiFoodAnalysisResult::class)
        ->and($result->dishName)->toBe('Bandera Dominicana')
        ->and($result->items)->toHaveCount(3)
        ->and($result->totalEstimatedCalories)->toBe(619)
        ->and($result->promptTokens)->toBe(1200)
        ->and($result->completionTokens)->toBe(400)
        ->and($result->estimatedCostUsd)->toBeGreaterThan(0.0);
});

test('OpenAiVisionProvider throws RuntimeException on malformed AI response', function () {
    Http::fake([
        'https://api.openai.com/v1/chat/completions' => Http::response([
            'choices' => [
                ['message' => ['content' => 'Este no es un JSON válido']],
            ],
        ], 200),
    ]);

    $provider = new OpenAiVisionProvider('sk-test-key');
    $provider->analyzeFoodImage('base64_sample');
})->throws(RuntimeException::class);

test('AiVisionManager resolves mock driver and openai driver appropriately', function () {
    $manager = new AiVisionManager;

    $mockDriver = $manager->driver('mock');
    expect($mockDriver)->toBeInstanceOf(MockVisionProvider::class);

    $openAiDriver = $manager->driver('openai');
    expect($openAiDriver)->toBeInstanceOf(OpenAiVisionProvider::class);
});
