<?php

use App\Models\AiPhotoAnalysis;
use App\Models\User;
use Database\Seeders\DominicanFoodDatasetSeeder;
use Database\Seeders\FoodCategorySeeder;
use Database\Seeders\FoodSourceSeeder;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Laravel\Sanctum\Sanctum;

uses(RefreshDatabase::class);

beforeEach(function () {
    Storage::fake('local');

    $this->seed([
        FoodSourceSeeder::class,
        FoodCategorySeeder::class,
        NutrientSeeder::class,
        DominicanFoodDatasetSeeder::class,
    ]);

    $this->user = User::factory()->create();
    Sanctum::actingAs($this->user);
});

test('POST api v1 ai photo analyze successfully processes food photo and returns recognized items and nutrition', function () {
    $image = UploadedFile::fake()->image('mangu_tres_golpes.jpg', 640, 480);

    $response = $this->postJson('/api/v1/ai/photo/analyze', [
        'image' => $image,
        'locale' => 'DO',
        'meal_type' => 'breakfast',
    ]);

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonStructure([
            'status',
            'data' => [
                'id',
                'status',
                'dish_name',
                'summary',
                'confidence_score',
                'provider',
                'totals' => ['calories', 'protein_g', 'carbs_g', 'fat_g'],
                'items' => [
                    '*' => [
                        'id',
                        'food_id',
                        'name',
                        'matched_name',
                        'estimated_weight_grams',
                        'portion_description',
                        'confidence',
                        'calories',
                        'protein_g',
                        'carbs_g',
                        'fat_g',
                        'candidates',
                    ],
                ],
            ],
        ]);

    $this->assertDatabaseHas('ai_photo_analyses', [
        'user_id' => $this->user->id,
        'status' => 'completed',
    ]);
});

test('GET api v1 ai photo analyses id returns detailed analysis with matched items', function () {
    $image = UploadedFile::fake()->image('plate.jpg', 640, 480);
    $analyzeRes = $this->postJson('/api/v1/ai/photo/analyze', ['image' => $image]);
    $analysisId = $analyzeRes->json('data.id');

    $response = $this->getJson("/api/v1/ai/photo/analyses/{$analysisId}");

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.id', $analysisId);
});

test('POST api v1 ai photo analyses id confirm logs confirmed items into daily diary', function () {
    $image = UploadedFile::fake()->image('plate.jpg', 640, 480);
    $analyzeRes = $this->postJson('/api/v1/ai/photo/analyze', ['image' => $image]);
    $analysisId = $analyzeRes->json('data.id');
    $items = $analyzeRes->json('data.items');

    $confirmPayload = [
        'date' => '2026-08-21',
        'meal_type' => 'breakfast',
        'items' => array_map(function ($item) {
            return [
                'name' => $item['name'],
                'food_id' => $item['food_id'],
                'portion_id' => null,
                'quantity' => 1.0,
                'weight_grams' => $item['estimated_weight_grams'],
                'calories' => $item['calories'],
                'protein_g' => $item['protein_g'],
                'carbs_g' => $item['carbs_g'],
                'fat_g' => $item['fat_g'],
            ];
        }, $items),
    ];

    $response = $this->postJson("/api/v1/ai/photo/analyses/{$analysisId}/confirm", $confirmPayload);

    $response->assertStatus(201)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.logged_entries_count', count($items));

    $this->assertDatabaseHas('meal_entries', [
        'source' => 'ai_photo',
    ]);
});

test('POST api v1 ai photo analyze rejects non-image or oversized uploads', function () {
    $document = UploadedFile::fake()->create('contract.pdf', 500, 'application/pdf');

    $response = $this->post('/api/v1/ai/photo/analyze', [
        'image' => $document,
    ], ['Accept' => 'application/json']);

    $response->assertStatus(422)
        ->assertJsonPath('error.code', 'VALIDATION_ERROR');
});


