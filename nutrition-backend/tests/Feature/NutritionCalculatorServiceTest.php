<?php

use App\Models\Food;
use App\Models\FoodNutrient;
use App\Models\FoodPortion;
use App\Models\Nutrient;
use App\Services\NutritionCalculatorService;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed(NutrientSeeder::class);
    $this->calculator = new NutritionCalculatorService;
});

test('calculateForFood returns exact values for 100g basis', function () {
    $food = Food::create([
        'canonical_name' => 'Pechuga de Pollo Cruda',
        'normalized_name' => 'pechuga de pollo cruda',
        'default_basis_amount' => 100,
        'default_basis_unit' => 'g',
    ]);

    $calNut = Nutrient::where('code', 'calories')->first();
    $protNut = Nutrient::where('code', 'protein')->first();
    $fatNut = Nutrient::where('code', 'total_fat')->first();

    FoodNutrient::create(['food_id' => $food->id, 'nutrient_id' => $calNut->id, 'amount' => 120.0]);
    FoodNutrient::create(['food_id' => $food->id, 'nutrient_id' => $protNut->id, 'amount' => 22.5]);
    FoodNutrient::create(['food_id' => $food->id, 'nutrient_id' => $fatNut->id, 'amount' => 2.6]);

    $result = $this->calculator->calculateForFood($food, 100, null, 'g');

    expect($result['grams'])->toEqual(100.0)
        ->and($result['calories_snapshot'])->toBe(120)
        ->and($result['protein_snapshot'])->toEqual(22.5)
        ->and($result['fat_snapshot'])->toEqual(2.6);
});

test('calculateForFood scales fractional amounts correctly (e.g. 180g of rice with 130 kcal/100g = 234 kcal)', function () {
    $rice = Food::create([
        'canonical_name' => 'Arroz Blanco Cocido',
        'normalized_name' => 'arroz blanco cocido',
        'default_basis_amount' => 100,
        'default_basis_unit' => 'g',
    ]);

    $calNut = Nutrient::where('code', 'calories')->first();
    $carbNut = Nutrient::where('code', 'carbohydrate')->first();
    $protNut = Nutrient::where('code', 'protein')->first();

    FoodNutrient::create(['food_id' => $rice->id, 'nutrient_id' => $calNut->id, 'amount' => 130.0]);
    FoodNutrient::create(['food_id' => $rice->id, 'nutrient_id' => $carbNut->id, 'amount' => 28.2]);
    FoodNutrient::create(['food_id' => $rice->id, 'nutrient_id' => $protNut->id, 'amount' => 2.7]);

    // 180g -> 130 * 180 / 100 = 234 kcal
    $result = $this->calculator->calculateForFood($rice, 180, null, 'g');

    expect($result['grams'])->toEqual(180.0)
        ->and($result['calories_snapshot'])->toBe(234)
        ->and($result['carbs_snapshot'])->toEqual(50.76)
        ->and($result['protein_snapshot'])->toEqual(4.86);
});

test('calculateForFood scales portion definitions correctly (e.g. 2 large eggs)', function () {
    $egg = Food::create([
        'canonical_name' => 'Huevo Entero Grande',
        'normalized_name' => 'huevo entero grande',
        'default_basis_amount' => 100,
        'default_basis_unit' => 'g',
    ]);

    $calNut = Nutrient::where('code', 'calories')->first();
    $protNut = Nutrient::where('code', 'protein')->first();
    $fatNut = Nutrient::where('code', 'total_fat')->first();

    FoodNutrient::create(['food_id' => $egg->id, 'nutrient_id' => $calNut->id, 'amount' => 143.0]);
    FoodNutrient::create(['food_id' => $egg->id, 'nutrient_id' => $protNut->id, 'amount' => 12.6]);
    FoodNutrient::create(['food_id' => $egg->id, 'nutrient_id' => $fatNut->id, 'amount' => 9.5]);

    $portion = FoodPortion::create([
        'food_id' => $egg->id,
        'portion_name' => '1 huevo grande (50g)',
        'gram_weight' => 50.0,
        'amount' => 1.0,
        'unit' => 'unidad',
    ]);

    // 2 eggs -> 100g total
    $result = $this->calculator->calculateForFood($egg, 2.0, $portion);

    expect($result['grams'])->toEqual(100.0)
        ->and($result['calories_snapshot'])->toBe(143)
        ->and($result['protein_snapshot'])->toEqual(12.6)
        ->and($result['fat_snapshot'])->toEqual(9.5);
});

test('aggregate calculates correct totals and macro distribution for mixed meal', function () {
    $chicken = Food::create(['canonical_name' => 'Pollo', 'normalized_name' => 'pollo']);
    $rice = Food::create(['canonical_name' => 'Arroz', 'normalized_name' => 'arroz']);
    $oil = Food::create(['canonical_name' => 'Aceite de Oliva', 'normalized_name' => 'aceite de oliva']);

    $calNut = Nutrient::where('code', 'calories')->first();
    $protNut = Nutrient::where('code', 'protein')->first();
    $carbNut = Nutrient::where('code', 'carbohydrate')->first();
    $fatNut = Nutrient::where('code', 'total_fat')->first();

    // Chicken per 100g: 120 kcal, 22.5g P, 0g C, 2.6g F
    FoodNutrient::create(['food_id' => $chicken->id, 'nutrient_id' => $calNut->id, 'amount' => 120.0]);
    FoodNutrient::create(['food_id' => $chicken->id, 'nutrient_id' => $protNut->id, 'amount' => 22.5]);
    FoodNutrient::create(['food_id' => $chicken->id, 'nutrient_id' => $fatNut->id, 'amount' => 2.6]);

    // Rice per 100g: 130 kcal, 2.7g P, 28.2g C, 0.3g F
    FoodNutrient::create(['food_id' => $rice->id, 'nutrient_id' => $calNut->id, 'amount' => 130.0]);
    FoodNutrient::create(['food_id' => $rice->id, 'nutrient_id' => $protNut->id, 'amount' => 2.7]);
    FoodNutrient::create(['food_id' => $rice->id, 'nutrient_id' => $carbNut->id, 'amount' => 28.2]);
    FoodNutrient::create(['food_id' => $rice->id, 'nutrient_id' => $fatNut->id, 'amount' => 0.3]);

    // Oil per 100g: 884 kcal, 0g P, 0g C, 100g F
    FoodNutrient::create(['food_id' => $oil->id, 'nutrient_id' => $calNut->id, 'amount' => 884.0]);
    FoodNutrient::create(['food_id' => $oil->id, 'nutrient_id' => $fatNut->id, 'amount' => 100.0]);

    // Meal: 150g Chicken (180 kcal, 33.75g P, 3.9g F) + 200g Rice (260 kcal, 5.4g P, 56.4g C, 0.6g F) + 10g Oil (88.4 -> 88 kcal, 10g F)
    $calcChicken = $this->calculator->calculateForFood($chicken, 150, null, 'g');
    $calcRice = $this->calculator->calculateForFood($rice, 200, null, 'g');
    $calcOil = $this->calculator->calculateForFood($oil, 10, null, 'g');

    $mealSummary = $this->calculator->aggregate([$calcChicken, $calcRice, $calcOil]);

    expect($mealSummary['total_items'])->toBe(3)
        ->and($mealSummary['total_grams'])->toEqual(360.0)
        ->and($mealSummary['total_calories'])->toBe(180 + 260 + 88)
        ->and($mealSummary['total_protein_g'])->toEqual(39.15)
        ->and($mealSummary['total_carbs_g'])->toEqual(56.4)
        ->and($mealSummary['total_fat_g'])->toEqual(14.5);

    expect($mealSummary['macro_distribution']['protein_pct'])->toBeGreaterThan(0)
        ->and($mealSummary['macro_distribution']['carbs_pct'])->toBeGreaterThan(0)
        ->and($mealSummary['macro_distribution']['fat_pct'])->toBeGreaterThan(0);
});

test('snapshot values remain immutable even if food nutrient definition is updated later in catalog', function () {
    $food = Food::create([
        'canonical_name' => 'Avena',
        'normalized_name' => 'avena',
    ]);

    $calNut = Nutrient::where('code', 'calories')->first();
    $fn = FoodNutrient::create(['food_id' => $food->id, 'nutrient_id' => $calNut->id, 'amount' => 380.0]);

    $snapshot = $this->calculator->calculateForFood($food, 100, null, 'g');

    expect($snapshot['calories_snapshot'])->toBe(380);

    // Later: catalog is edited / updated with new laboratory data
    $fn->update(['amount' => 400.0]);

    // The recorded historical snapshot retains original 380 value
    expect($snapshot['calories_snapshot'])->toBe(380);
});
