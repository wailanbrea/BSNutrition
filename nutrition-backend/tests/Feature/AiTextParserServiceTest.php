<?php

use App\Models\User;
use App\Services\Ai\AiTextParserService;
use App\Services\FoodMatchingService;
use Database\Seeders\DominicanFoodDatasetSeeder;
use Database\Seeders\FoodCategorySeeder;
use Database\Seeders\FoodSourceSeeder;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed([
        FoodSourceSeeder::class,
        FoodCategorySeeder::class,
        NutrientSeeder::class,
        DominicanFoodDatasetSeeder::class,
    ]);

    $this->user = User::factory()->create();
    $this->matchingService = app(FoodMatchingService::class);
    $this->parserService = new AiTextParserService($this->matchingService);
});

test('parseMealText extracts Dominican meal and calculates matching nutrients', function () {
    $text = 'Me comí una porción de mangú con salami frito y 2 huevos fritos';

    $result = $this->parserService->parseMealText($this->user, $text, 'DO', 'breakfast');

    expect($result['status'])->toBe('completed')
        ->and($result['items'])->toBeArray()
        ->and(count($result['items']))->toBeGreaterThanOrEqual(2)
        ->and($result['totals']['calories'])->toBeGreaterThan(200)
        ->and($result['provider'])->toBe('text_nlp');
});

test('parseMealText parses quantities and weights from Spanish text', function () {
    $text = '150g de pechuga a la plancha con 1 taza de arroz blanco';

    $result = $this->parserService->parseMealText($this->user, $text, 'DO', 'lunch');

    expect($result['status'])->toBe('completed')
        ->and(count($result['items']))->toBe(2)
        ->and($result['items'][0]['estimated_weight_grams'])->toBe(150.0)
        ->and($result['items'][1]['estimated_weight_grams'])->toBe(180.0);
});
