<?php

use App\Models\Recipe;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->user = User::factory()->create();
    $this->actingAs($this->user);
});

it('can create a multi-ingredient recipe with steps via API', function () {
    $payload = [
        'name' => 'Avena Proteica con Frutas',
        'description' => 'Desayuno balanceado',
        'servings' => 2,
        'prep_time_minutes' => 5,
        'cook_time_minutes' => 10,
        'is_public' => false,
        'ingredients' => [
            [
                'custom_name' => 'Avena en hojuelas',
                'grams' => 100.0,
                'calories' => 380,
                'protein_g' => 13.0,
                'carbs_g' => 67.0,
                'fat_g' => 7.0,
            ],
            [
                'custom_name' => 'Proteína Whey',
                'grams' => 30.0,
                'calories' => 120,
                'protein_g' => 24.0,
                'carbs_g' => 2.0,
                'fat_g' => 1.5,
            ],
        ],
        'steps' => [
            'Hervir la avena con agua.',
            'Agregar el scoop de proteína al final y mezclar.',
        ],
    ];

    $response = $this->postJson('/api/v1/recipes', $payload);

    $response->assertStatus(201)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.name', 'Avena Proteica con Frutas')
        ->assertJsonPath('data.servings', 2)
        ->assertJsonPath('data.calories_per_serving', 250);

    $this->assertDatabaseHas('recipes', [
        'user_id' => $this->user->id,
        'name' => 'Avena Proteica con Frutas',
        'calories_per_serving' => 250,
    ]);
});

it('can list, show, and delete user recipes', function () {
    $recipe = Recipe::create([
        'user_id' => $this->user->id,
        'name' => 'Batida Verde',
        'servings' => 1,
        'calories_per_serving' => 150,
        'protein_per_serving_g' => 5.0,
        'carbs_per_serving_g' => 30.0,
        'fat_per_serving_g' => 1.0,
    ]);

    // List
    $listResponse = $this->getJson('/api/v1/recipes');
    $listResponse->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonCount(1, 'data.data');

    // Show
    $showResponse = $this->getJson("/api/v1/recipes/{$recipe->id}");
    $showResponse->assertStatus(200)
        ->assertJsonPath('data.name', 'Batida Verde');

    // Delete
    $deleteResponse = $this->deleteJson("/api/v1/recipes/{$recipe->id}");
    $deleteResponse->assertStatus(200);

    $this->assertSoftDeleted('recipes', ['id' => $recipe->id]);
});

it('can log recipe serving directly into daily diary via endpoint', function () {
    $recipe = Recipe::create([
        'user_id' => $this->user->id,
        'name' => 'Ensalada de Atún',
        'servings' => 2,
        'total_weight_grams' => 400.0,
        'calories_per_serving' => 220,
        'protein_per_serving_g' => 30.0,
        'carbs_per_serving_g' => 5.0,
        'fat_per_serving_g' => 8.0,
    ]);

    $response = $this->postJson("/api/v1/recipes/{$recipe->id}/log-to-diary", [
        'date' => '2026-08-21',
        'meal_type' => 'dinner',
        'servings' => 1.5,
        'client_id' => 'client-recipe-log-555',
    ]);

    $response->assertStatus(201)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.custom_name', 'Ensalada de Atún')
        ->assertJsonPath('data.calories_snapshot', 330); // 220 * 1.5 = 330

    $this->assertDatabaseHas('meal_entries', [
        'client_id' => 'client-recipe-log-555',
        'calories_snapshot' => 330,
        'source' => 'recipe',
    ]);
});
