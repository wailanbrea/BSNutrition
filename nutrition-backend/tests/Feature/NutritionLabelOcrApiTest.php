<?php

use App\Models\Food;
use App\Models\FoodBarcode;
use App\Models\User;
use Database\Seeders\FoodCategorySeeder;
use Database\Seeders\FoodSourceSeeder;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed([
        FoodSourceSeeder::class,
        FoodCategorySeeder::class,
        NutrientSeeder::class,
    ]);

    $this->user = User::factory()->create();
    Sanctum::actingAs($this->user);
});

test('POST api v1 foods ocr parse-label parses raw label text successfully', function () {
    $raw = "Serving size 50g\nCalories 200\nTotal Fat 10g\nTotal Carbohydrate 25g\nProtein 5g\nSodium 100mg";

    $response = $this->postJson('/api/v1/foods/ocr/parse-label', [
        'raw_text' => $raw,
    ]);

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.per_serving.calories', 200)
        ->assertJsonPath('data.per_serving.fat_g', 10)
        ->assertJsonPath('data.per_serving.carbs_g', 25)
        ->assertJsonPath('data.per_serving.protein_g', 5);
});

test('POST api v1 foods from-label creates canonical food with brand, barcode, portions and nutrients', function () {
    $payload = [
        'canonical_name' => 'Galletas de Avena Caseras',
        'brand_name' => 'Repostería Dominicana',
        'barcode' => '7461234567890',
        'serving_name' => '2 galletas',
        'serving_grams' => 40.0,
        'calories_100g' => 450,
        'protein_100g' => 8.0,
        'carbs_100g' => 65.0,
        'fat_100g' => 18.0,
        'fiber_100g' => 5.0,
        'sodium_100g' => 220.0,
        'sugars_100g' => 20.0,
        'log_to_diary' => true,
        'diary_date' => '2026-08-21',
        'diary_meal_type' => 'snack',
    ];

    $response = $this->postJson('/api/v1/foods/from-label', $payload);

    $response->assertStatus(201)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.food.canonical_name', 'Galletas de Avena Caseras')
        ->assertJsonPath('data.food.brand.name', 'Repostería Dominicana');

    $this->assertDatabaseHas('foods', [
        'canonical_name' => 'Galletas de Avena Caseras',
        'source' => 'ocr_label',
    ]);

    $this->assertDatabaseHas('food_barcodes', [
        'barcode' => '7461234567890',
    ]);

    $this->assertDatabaseHas('meal_entries', [
        'source' => 'ocr_label',
    ]);
});
