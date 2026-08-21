<?php

use App\Models\Food;
use App\Models\MealEntry;
use App\Models\User;
use App\Models\WaterLog;
use App\Services\DiaryService;
use Database\Seeders\DatabaseSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed(DatabaseSeeder::class);
    $this->user = User::factory()->create();
    $this->otherUser = User::factory()->create();
    $this->food = Food::first();
});

test('unauthenticated users cannot access diary endpoints', function () {
    $this->getJson('/api/v1/diary/2026-08-21')->assertStatus(401);
    $this->postJson('/api/v1/diary/2026-08-21/entries', [])->assertStatus(401);
    $this->putJson('/api/v1/diary/entries/1', [])->assertStatus(401);
    $this->deleteJson('/api/v1/diary/entries/1')->assertStatus(401);
    $this->postJson('/api/v1/diary/copy-meal', [])->assertStatus(401);
    $this->postJson('/api/v1/diary/copy-day', [])->assertStatus(401);
    $this->getJson('/api/v1/diary/2026-08-21/water')->assertStatus(401);
    $this->postJson('/api/v1/diary/2026-08-21/water', [])->assertStatus(401);
    $this->getJson('/api/v1/diary/2026-08-21/summary')->assertStatus(401);
});

test('GET /api/v1/diary/{date} returns structured daily diary with meals', function () {
    $response = $this->actingAs($this->user)
        ->getJson('/api/v1/diary/2026-08-21')
        ->assertOk()
        ->assertJsonStructure([
            'data' => [
                'id',
                'user_id',
                'diary_date',
                'timezone',
                'summary' => [
                    'calories',
                    'protein_g',
                    'carbs_g',
                    'fat_g',
                    'water_ml',
                ],
                'meals' => [
                    '*' => [
                        'id',
                        'diary_id',
                        'meal_type',
                        'name',
                        'total_calories',
                        'entries',
                    ],
                ],
            ],
        ]);

    expect($response->json('data.meals'))->toHaveCount(4);
});

test('POST /api/v1/diary/{date}/entries creates entry with calculated snapshot', function () {
    $payload = [
        'meal_type' => 'breakfast',
        'food_id' => $this->food->id,
        'quantity' => 1.5,
        'unit' => 'porción',
    ];

    $response = $this->actingAs($this->user)
        ->postJson('/api/v1/diary/2026-08-21/entries', $payload)
        ->assertCreated()
        ->assertJsonStructure([
            'data' => [
                'id',
                'meal_id',
                'custom_name',
                'quantity',
                'unit',
                'grams',
                'calories_snapshot',
                'protein_snapshot',
                'carbs_snapshot',
                'fat_snapshot',
            ],
        ]);

    expect($response->json('data.calories_snapshot'))->toBeGreaterThan(0);
});

test('PUT /api/v1/diary/entries/{id} updates entry and recalculates calories', function () {
    $entry = app(DiaryService::class)->addEntry($this->user, '2026-08-21', [
        'meal_type' => 'breakfast',
        'food_id' => $this->food->id,
        'quantity' => 1.0,
    ]);

    $initialCalories = $entry->calories_snapshot;

    $response = $this->actingAs($this->user)
        ->putJson("/api/v1/diary/entries/{$entry->id}", [
            'quantity' => 2.0,
        ])
        ->assertOk();

    expect($response->json('data.calories_snapshot'))->toBe($initialCalories * 2);
    expect($response->json('data.version'))->toBe(2);
});

test('DELETE /api/v1/diary/entries/{id} soft-deletes the entry', function () {
    $entry = app(DiaryService::class)->addEntry($this->user, '2026-08-21', [
        'meal_type' => 'breakfast',
        'food_id' => $this->food->id,
        'quantity' => 1.0,
    ]);

    $this->actingAs($this->user)
        ->deleteJson("/api/v1/diary/entries/{$entry->id}")
        ->assertOk()
        ->assertJson(['message' => 'Entrada eliminada correctamente.']);

    expect(MealEntry::find($entry->id))->toBeNull();
    expect(MealEntry::withTrashed()->find($entry->id))->not->toBeNull();
});

test('POST /api/v1/diary/copy-meal duplicates all items to target date', function () {
    $entry = app(DiaryService::class)->addEntry($this->user, '2026-08-21', [
        'meal_type' => 'lunch',
        'food_id' => $this->food->id,
        'quantity' => 1.0,
    ]);

    $response = $this->actingAs($this->user)
        ->postJson('/api/v1/diary/copy-meal', [
            'source_meal_id' => $entry->meal_id,
            'target_date' => '2026-08-22',
            'target_meal_type' => 'lunch',
        ])
        ->assertOk();

    expect($response->json('data.entries'))->toHaveCount(1);
});

test('POST /api/v1/diary/copy-day copies entire day to another date', function () {
    app(DiaryService::class)->addEntry($this->user, '2026-08-21', [
        'meal_type' => 'breakfast',
        'food_id' => $this->food->id,
        'quantity' => 1.0,
    ]);

    $response = $this->actingAs($this->user)
        ->postJson('/api/v1/diary/copy-day', [
            'source_date' => '2026-08-21',
            'target_date' => '2026-08-23',
        ])
        ->assertOk();

    expect($response->json('data.diary_date'))->toBe('2026-08-23');
    expect($response->json('data.summary.calories'))->toBeGreaterThan(0);
});

test('Water logging endpoints create, list, sum, and delete logs', function () {
    // 1. Log water
    $response = $this->actingAs($this->user)
        ->postJson('/api/v1/diary/2026-08-21/water', [
            'amount_ml' => 500,
            'client_id' => 'water-cli-1',
        ])
        ->assertCreated();

    $waterId = $response->json('data.id');

    // 2. List water
    $listResponse = $this->actingAs($this->user)
        ->getJson('/api/v1/diary/2026-08-21/water')
        ->assertOk();

    expect($listResponse->json('data'))->toHaveCount(1);

    // 3. Summary check
    $summary = $this->actingAs($this->user)
        ->getJson('/api/v1/diary/2026-08-21/summary')
        ->assertOk();

    expect($summary->json('data.water_ml'))->toBe(500);

    // 4. Delete water
    $this->actingAs($this->user)
        ->deleteJson("/api/v1/diary/water/{$waterId}")
        ->assertOk();

    expect(WaterLog::find($waterId))->toBeNull();
});
