<?php

use App\Models\Food;
use App\Models\Nutrient;
use App\Models\User;
use App\Services\RecipeCalculationService;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->user = User::factory()->create();
    $this->service = new RecipeCalculationService();
});

it('calculates totals and per-serving nutrition correctly', function () {
    $ingredients = [
        [
            'custom_name' => 'Pechuga de Pollo',
            'quantity' => 1.0,
            'unit' => 'g',
            'grams' => 400.0,
            'calories' => 660,
            'protein_g' => 124.0,
            'carbs_g' => 0.0,
            'fat_g' => 14.0,
        ],
        [
            'custom_name' => 'Arroz Blanco',
            'quantity' => 1.0,
            'unit' => 'g',
            'grams' => 200.0,
            'calories' => 260,
            'protein_g' => 5.0,
            'carbs_g' => 56.0,
            'fat_g' => 1.0,
        ],
    ];

    $result = $this->service->calculateTotals($ingredients, servings: 2);

    expect($result['total_weight_grams'])->toBe(600.0)
        ->and($result['total_calories'])->toBe(920)
        ->and($result['total_protein_g'])->toBe(129.0)
        ->and($result['total_carbs_g'])->toBe(56.0)
        ->and($result['total_fat_g'])->toBe(15.0)
        ->and($result['calories_per_serving'])->toBe(460)
        ->and($result['protein_per_serving_g'])->toBe(64.5)
        ->and($result['carbs_per_serving_g'])->toBe(28.0)
        ->and($result['fat_per_serving_g'])->toBe(7.5);
});

it('saves recipe and logs serving directly to diary with immutable snapshot', function () {
    $recipeData = [
        'name' => 'Pollo con Arroz Familiar',
        'description' => 'Receta criolla alta en proteína',
        'servings' => 4,
        'prep_time_minutes' => 15,
        'cook_time_minutes' => 30,
        'ingredients' => [
            [
                'custom_name' => 'Pechuga de Pollo',
                'grams' => 600.0,
                'calories' => 990,
                'protein_g' => 186.0,
                'carbs_g' => 0.0,
                'fat_g' => 21.0,
            ],
            [
                'custom_name' => 'Arroz',
                'grams' => 400.0,
                'calories' => 520,
                'protein_g' => 10.0,
                'carbs_g' => 112.0,
                'fat_g' => 2.0,
            ],
        ],
        'steps' => [
            'Sazonar el pollo.',
            'Cocinar el arroz.',
            'Mezclar y servir.',
        ],
    ];

    $recipe = $this->service->saveRecipe($this->user, $recipeData);

    expect($recipe->id)->not->toBeNull()
        ->and($recipe->servings)->toBe(4)
        ->and($recipe->calories_per_serving)->toBe(378)
        ->and($recipe->ingredients)->toHaveCount(2)
        ->and($recipe->steps)->toHaveCount(3);

    // Log 1 serving to diary
    $entry = $this->service->logServingToDiary(
        user: $this->user,
        recipe: $recipe,
        date: '2026-08-21',
        mealType: 'lunch',
        servings: 1.0,
        clientId: 'recipe-log-uuid-1'
    );

    expect($entry->custom_name)->toBe('Pollo con Arroz Familiar')
        ->and($entry->calories_snapshot)->toBe(378)
        ->and($entry->source)->toBe('recipe');

    $this->assertDatabaseHas('meal_entries', [
        'id' => $entry->id,
        'client_id' => 'recipe-log-uuid-1',
        'calories_snapshot' => 378,
    ]);
});
