<?php

use App\Models\Food;
use App\Services\UsdaFoodDataService;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed(NutrientSeeder::class);
    $this->usdaService = new UsdaFoodDataService;
});

test('usda service search queries USDA API and returns results', function () {
    Http::fake([
        'https://api.nal.usda.gov/fdc/v1/foods/search*' => Http::response([
            'totalHits' => 1,
            'foods' => [
                [
                    'fdcId' => 171688,
                    'description' => 'Apples, raw, with skin',
                    'dataType' => 'SR Legacy',
                ],
            ],
        ], 200),
    ]);

    $results = $this->usdaService->search('apple', 10);

    expect($results)->toHaveKey('foods')
        ->and($results['foods'])->toHaveCount(1)
        ->and($results['foods'][0]['fdcId'])->toBe(171688);
});

test('usda service importFood creates food with mapped canonical nutrients and portions', function () {
    Http::fake([
        'https://api.nal.usda.gov/fdc/v1/food/171688*' => Http::response([
            'fdcId' => 171688,
            'description' => 'Apples, raw, with skin',
            'dataType' => 'SR Legacy',
            'foodNutrients' => [
                [
                    'nutrient' => ['id' => 1008, 'number' => '208', 'name' => 'Energy', 'unitName' => 'kcal'],
                    'amount' => 52.0,
                ],
                [
                    'nutrient' => ['id' => 1003, 'number' => '203', 'name' => 'Protein', 'unitName' => 'g'],
                    'amount' => 0.26,
                ],
                [
                    'nutrient' => ['id' => 1005, 'number' => '205', 'name' => 'Carbohydrate, by difference', 'unitName' => 'g'],
                    'amount' => 13.81,
                ],
                [
                    'nutrient' => ['id' => 1004, 'number' => '204', 'name' => 'Total lipid (fat)', 'unitName' => 'g'],
                    'amount' => 0.17,
                ],
                [
                    'nutrient' => ['id' => 1079, 'number' => '291', 'name' => 'Fiber, total dietary', 'unitName' => 'g'],
                    'amount' => 2.4,
                ],
                [
                    'nutrient' => ['id' => 2000, 'number' => '269', 'name' => 'Sugars, total including NLEA', 'unitName' => 'g'],
                    'amount' => 10.39,
                ],
            ],
            'foodPortions' => [
                [
                    'id' => 89123,
                    'portionDescription' => '1 medium (3" dia) (182g)',
                    'gramWeight' => 182.0,
                    'amount' => 1.0,
                    'modifier' => 'medium',
                ],
                [
                    'id' => 89124,
                    'portionDescription' => '1 cup, sliced (109g)',
                    'gramWeight' => 109.0,
                    'amount' => 1.0,
                    'modifier' => 'cup, sliced',
                ],
            ],
        ], 200),
    ]);

    $food = $this->usdaService->importFood(171688);

    expect($food)->not->toBeNull()
        ->and($food->canonical_name)->toBe('Apples, raw, with skin')
        ->and($food->source)->toBe('usda_fdc')
        ->and($food->external_source_id)->toBe('171688')
        ->and($food->verified)->toBeTrue();

    // Check mapped nutrients in database
    $calories = $food->nutrients()->where('code', 'calories')->first();
    $protein = $food->nutrients()->where('code', 'protein')->first();
    $fiber = $food->nutrients()->where('code', 'fiber')->first();

    expect($calories)->not->toBeNull()
        ->and($calories->pivot->amount)->toEqual(52.0);

    expect($protein)->not->toBeNull()
        ->and($protein->pivot->amount)->toEqual(0.26);

    expect($fiber)->not->toBeNull()
        ->and($fiber->pivot->amount)->toEqual(2.4);

    // Check portions
    expect($food->portions)->toHaveCount(2)
        ->and($food->portions->first()->gram_weight)->toEqual(182.0);
});

test('artisan command foods:import-usda imports food via CLI', function () {
    Http::fake([
        'https://api.nal.usda.gov/fdc/v1/food/171688*' => Http::response([
            'fdcId' => 171688,
            'description' => 'Apples, raw, with skin',
            'foodNutrients' => [
                [
                    'nutrient' => ['id' => 1008, 'number' => '208', 'name' => 'Energy'],
                    'amount' => 52.0,
                ],
            ],
            'foodPortions' => [],
        ], 200),
    ]);

    $this->artisan('foods:import-usda', ['--fdcId' => 171688])
        ->expectsOutputToContain('Alimento importado exitosamente')
        ->assertSuccessful();

    expect(Food::where('external_source_id', '171688')->exists())->toBeTrue();
});
