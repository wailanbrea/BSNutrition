<?php

use App\Models\Food;
use App\Models\FoodAlias;
use App\Models\FoodBarcode;
use App\Models\FoodBrand;
use App\Models\FoodCategory;
use App\Models\FoodNutrient;
use App\Models\FoodPortion;
use App\Models\Nutrient;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

test('can create full food schema with category, brand, portions, barcodes and nutrients', function () {
    $category = FoodCategory::create([
        'name' => 'Lácteos y Derivados',
        'slug' => 'lacteos-y-derivados',
        'icon' => 'milk',
    ]);

    $brand = FoodBrand::create([
        'name' => 'Fage',
        'normalized_name' => 'fage',
        'country_code' => 'GR',
    ]);

    $food = Food::create([
        'canonical_name' => 'Yogur Griego 0% Grasa',
        'normalized_name' => 'yogur griego 0% grasa',
        'brand_id' => $brand->id,
        'category_id' => $category->id,
        'verified' => true,
        'source' => 'generic',
        'default_basis_amount' => 100,
        'default_basis_unit' => 'g',
    ]);

    // Portion
    $portion = FoodPortion::create([
        'food_id' => $food->id,
        'portion_name' => '1 envase (170g)',
        'gram_weight' => 170,
        'amount' => 1,
        'unit' => 'envase',
        'is_default' => true,
    ]);

    // Barcode
    $barcode = FoodBarcode::create([
        'food_id' => $food->id,
        'barcode' => '5201054000001',
        'barcode_type' => 'EAN_13',
        'is_primary' => true,
    ]);

    // Alias
    $alias = FoodAlias::create([
        'food_id' => $food->id,
        'alias' => 'Greek Yogurt Plain Nonfat',
        'normalized_alias' => 'greek yogurt plain nonfat',
        'language' => 'en',
    ]);

    // Nutrients
    $caloriesNutrient = Nutrient::create([
        'code' => 'calories',
        'name' => 'Calorías',
        'unit' => 'kcal',
        'is_macro' => true,
        'sort_order' => 1,
    ]);

    $proteinNutrient = Nutrient::create([
        'code' => 'protein',
        'name' => 'Proteínas',
        'unit' => 'g',
        'is_macro' => true,
        'sort_order' => 2,
    ]);

    FoodNutrient::create([
        'food_id' => $food->id,
        'nutrient_id' => $caloriesNutrient->id,
        'amount' => 59.0, // 59 kcal per 100g
        'basis_amount' => 100,
        'basis_unit' => 'g',
    ]);

    FoodNutrient::create([
        'food_id' => $food->id,
        'nutrient_id' => $proteinNutrient->id,
        'amount' => 10.3, // 10.3g protein per 100g
        'basis_amount' => 100,
        'basis_unit' => 'g',
    ]);

    // Verify relations
    expect($food->category->name)->toBe('Lácteos y Derivados')
        ->and($food->brand->name)->toBe('Fage')
        ->and($food->portions)->toHaveCount(1)
        ->and($food->barcodes)->toHaveCount(1)
        ->and($food->aliases)->toHaveCount(1)
        ->and($food->nutrients)->toHaveCount(2);

    expect($food->nutrients->first()->pivot->amount)->toEqual(59.0);
});

test('food search scope matches canonical name, alias, and brand', function () {
    $brand = FoodBrand::create([
        'name' => 'Quaker',
        'normalized_name' => 'quaker',
    ]);

    $food = Food::create([
        'canonical_name' => 'Avena en Hojuelas Clásica',
        'normalized_name' => 'avena en hojuelas clasica',
        'brand_id' => $brand->id,
        'verified' => true,
    ]);

    FoodAlias::create([
        'food_id' => $food->id,
        'alias' => 'Rolled Oats',
        'normalized_alias' => 'rolled oats',
    ]);

    // Match by canonical
    expect(Food::search('avena')->exists())->toBeTrue();

    // Match by alias
    expect(Food::search('rolled oats')->exists())->toBeTrue();

    // Match by brand
    expect(Food::search('quaker')->exists())->toBeTrue();

    // Non-match
    expect(Food::search('salmon')->exists())->toBeFalse();
});

test('food byBarcode scope finds matching food item', function () {
    $food = Food::create([
        'canonical_name' => 'Leche Entera 1L',
        'normalized_name' => 'leche entera 1l',
        'verified' => true,
    ]);

    FoodBarcode::create([
        'food_id' => $food->id,
        'barcode' => '7750123456789',
        'is_primary' => true,
    ]);

    $found = Food::byBarcode('7750123456789')->first();
    expect($found)->not->toBeNull()
        ->and($found->id)->toBe($food->id);

    expect(Food::byBarcode('0000000000000')->first())->toBeNull();
});

test('soft delete preserves food records and cascades portion/barcode removal on force delete', function () {
    $food = Food::create([
        'canonical_name' => 'Manzana Roja',
        'normalized_name' => 'manzana roja',
    ]);

    FoodPortion::create([
        'food_id' => $food->id,
        'portion_name' => '1 unidad mediana (182g)',
        'gram_weight' => 182,
    ]);

    $food->delete();

    expect(Food::count())->toBe(0)
        ->and(Food::withTrashed()->count())->toBe(1);

    $food->forceDelete();
    expect(FoodPortion::where('food_id', $food->id)->count())->toBe(0);
});
