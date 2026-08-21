<?php

use App\Models\FoodCategory;
use App\Models\FoodSource;
use App\Models\Nutrient;
use Database\Seeders\FoodCategorySeeder;
use Database\Seeders\FoodSourceSeeder;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

test('nutrient seeder populates core macros and micronutrients with exact stable codes', function () {
    $this->seed(NutrientSeeder::class);

    $requiredCodes = [
        'calories' => ['unit' => 'kcal', 'is_macro' => true],
        'protein' => ['unit' => 'g', 'is_macro' => true],
        'carbohydrate' => ['unit' => 'g', 'is_macro' => true],
        'total_fat' => ['unit' => 'g', 'is_macro' => true],
        'fiber' => ['unit' => 'g', 'is_macro' => false],
        'sugar' => ['unit' => 'g', 'is_macro' => false],
        'added_sugars' => ['unit' => 'g', 'is_macro' => false],
        'sodium' => ['unit' => 'mg', 'is_macro' => false],
        'potassium' => ['unit' => 'mg', 'is_macro' => false],
        'calcium' => ['unit' => 'mg', 'is_macro' => false],
        'iron' => ['unit' => 'mg', 'is_macro' => false],
        'vitamin_a' => ['unit' => 'mcg', 'is_macro' => false],
        'vitamin_c' => ['unit' => 'mg', 'is_macro' => false],
        'vitamin_d' => ['unit' => 'mcg', 'is_macro' => false],
        'vitamin_b12' => ['unit' => 'mcg', 'is_macro' => false],
    ];

    expect(Nutrient::count())->toBeGreaterThanOrEqual(30);

    foreach ($requiredCodes as $code => $expected) {
        $nutrient = Nutrient::where('code', $code)->first();
        expect($nutrient)->not->toBeNull("Nutrient with code {$code} must exist");
        expect($nutrient->unit)->toBe($expected['unit'], "Nutrient {$code} unit mismatch");
        expect($nutrient->is_macro)->toBe($expected['is_macro'], "Nutrient {$code} is_macro mismatch");
    }
});

test('category and source seeders populate taxonomy and provider records', function () {
    $this->seed(FoodCategorySeeder::class);
    $this->seed(FoodSourceSeeder::class);

    expect(FoodCategory::where('slug', 'carnes-y-aves')->exists())->toBeTrue()
        ->and(FoodCategory::where('slug', 'frutas')->exists())->toBeTrue()
        ->and(FoodCategory::where('slug', 'lacteos-y-huevos')->exists())->toBeTrue();

    expect(FoodSource::where('code', 'usda_fdc')->exists())->toBeTrue()
        ->and(FoodSource::where('code', 'openfoodfacts')->exists())->toBeTrue();
});
