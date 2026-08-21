<?php

use App\Models\Food;
use App\Models\FoodBarcode;
use App\Services\OpenFoodFactsService;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed(NutrientSeeder::class);
    $this->offService = new OpenFoodFactsService;
});

test('getByBarcode fetches product from Open Food Facts and creates canonical food with brand and nutrients', function () {
    $barcode = '8480000123456';

    Http::fake([
        "https://world.openfoodfacts.org/api/v2/product/{$barcode}.json" => Http::response([
            'status' => 1,
            'code' => $barcode,
            'product' => [
                'code' => $barcode,
                'product_name_es' => 'Leche Semidesnatada',
                'brands' => 'Hacendado, Mercadona',
                'serving_size' => '250 ml (1 vaso)',
                'serving_quantity' => 250,
                'nutriments' => [
                    'energy-kcal_100g' => 46.0,
                    'proteins_100g' => 3.2,
                    'carbohydrates_100g' => 4.8,
                    'fat_100g' => 1.6,
                    'saturated-fat_100g' => 1.0,
                    'sugars_100g' => 4.8,
                    'sodium_100g' => 0.05, // 0.05g -> 50mg
                    'calcium_100g' => 0.12, // 0.12g -> 120mg
                ],
            ],
        ], 200),
    ]);

    $food = $this->offService->getByBarcode($barcode);

    expect($food)->not->toBeNull()
        ->and($food->canonical_name)->toBe('Leche Semidesnatada')
        ->and($food->source)->toBe('openfoodfacts')
        ->and($food->brand)->not->toBeNull()
        ->and($food->brand->name)->toBe('Hacendado');

    // Check Barcode
    expect($food->barcodes)->toHaveCount(1)
        ->and($food->barcodes->first()->barcode)->toBe($barcode);

    // Check Nutrients
    $calories = $food->nutrients()->where('code', 'calories')->first();
    $protein = $food->nutrients()->where('code', 'protein')->first();
    $sodium = $food->nutrients()->where('code', 'sodium')->first();
    $calcium = $food->nutrients()->where('code', 'calcium')->first();

    expect($calories->pivot->amount)->toEqual(46.0);
    expect($protein->pivot->amount)->toEqual(3.2);
    expect($sodium->pivot->amount)->toEqual(50.0); // 0.05g * 1000 = 50mg
    expect($calcium->pivot->amount)->toEqual(120.0); // 0.12g * 1000 = 120mg

    // Check Portions
    expect($food->portions)->toHaveCount(1)
        ->and($food->portions->first()->gram_weight)->toEqual(250.0);
});

test('getByBarcode returns local food immediately if already present', function () {
    $barcode = '5411188110835';

    $localFood = Food::create([
        'canonical_name' => 'Bebida de Avena Alpro',
        'normalized_name' => 'bebida de avena alpro',
        'source' => 'openfoodfacts',
    ]);

    FoodBarcode::create([
        'food_id' => $localFood->id,
        'barcode' => $barcode,
    ]);

    Http::fake(); // No HTTP calls should be made

    $result = $this->offService->getByBarcode($barcode);

    expect($result)->not->toBeNull()
        ->and($result->id)->toBe($localFood->id);

    Http::assertNothingSent();
});

test('artisan command foods:import-off imports barcode successfully', function () {
    $barcode = '737628064502';

    Http::fake([
        "https://world.openfoodfacts.org/api/v2/product/{$barcode}.json" => Http::response([
            'status' => 1,
            'code' => $barcode,
            'product' => [
                'code' => $barcode,
                'product_name' => 'Thai Peanut Noodle Kit',
                'nutriments' => [
                    'energy-kcal_100g' => 380,
                ],
            ],
        ], 200),
    ]);

    $this->artisan('foods:import-off', ['--barcode' => $barcode])
        ->expectsOutputToContain('Alimento importado exitosamente')
        ->assertSuccessful();

    expect(Food::byBarcode($barcode)->exists())->toBeTrue();
});
