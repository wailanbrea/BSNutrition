<?php

use App\Models\User;
use Database\Seeders\DominicanFoodDatasetSeeder;
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
        DominicanFoodDatasetSeeder::class,
    ]);

    $this->user = User::factory()->create();
    Sanctum::actingAs($this->user);
});

test('POST api v1 ai text parse processes meal text and returns items', function () {
    $response = $this->postJson('/api/v1/ai/text/parse', [
        'text' => '2 huevos revueltos con una taza de avena',
        'locale' => 'DO',
        'meal_type' => 'breakfast',
    ]);

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.status', 'completed')
        ->assertJsonStructure([
            'data' => [
                'id',
                'status',
                'dish_name',
                'totals' => ['calories', 'protein_g', 'carbs_g', 'fat_g'],
                'items',
            ],
        ]);
});

test('POST api v1 ai text confirm logs parsed items into daily diary', function () {
    $parseResponse = $this->postJson('/api/v1/ai/text/parse', [
        'text' => '200g de pechuga a la plancha',
        'meal_type' => 'lunch',
    ]);

    $analysisId = $parseResponse->json('data.id');

    $confirmResponse = $this->postJson("/api/v1/ai/text/confirm/{$analysisId}", [
        'date' => '2026-08-21',
        'meal_type' => 'lunch',
        'items' => [
            [
                'name' => 'Pechuga de Pollo a la Plancha',
                'food_id' => null,
                'portion_id' => null,
                'quantity' => 1.0,
                'weight_grams' => 200.0,
                'calories' => 330,
                'protein_g' => 62.0,
                'carbs_g' => 0.0,
                'fat_g' => 7.0,
            ],
        ],
    ]);

    $confirmResponse->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.logged_entries_count', 1);

    $this->assertDatabaseHas('meal_entries', [
        'custom_name' => 'Pechuga de Pollo a la Plancha',
        'calories_snapshot' => 330,
    ]);
});

