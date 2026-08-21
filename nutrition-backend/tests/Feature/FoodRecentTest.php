<?php

use App\Models\Food;
use App\Models\User;
use Database\Seeders\DatabaseSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed(DatabaseSeeder::class);
    $this->user = User::factory()->create();
    $this->otherUser = User::factory()->create();
});

test('unauthenticated users cannot access recents endpoints', function () {
    $this->getJson('/api/v1/foods/recents')
        ->assertStatus(401);

    $this->postJson('/api/v1/foods/1/recent')
        ->assertStatus(401);
});

test('recording a food in recents increments use_count and updates last_used_at', function () {
    $food = Food::first();

    // 1. First record
    $this->actingAs($this->user)
        ->postJson("/api/v1/foods/{$food->id}/recent")
        ->assertOk()
        ->assertJson([
            'message' => 'Alimento registrado en recientes.',
            'food_id' => $food->id,
        ]);

    $recent = $this->user->recentFoods()->where('food_id', $food->id)->first();
    expect($recent)->not->toBeNull();
    expect($recent->pivot->use_count)->toBe(1);

    // 2. Second record increments use_count
    $this->actingAs($this->user)
        ->postJson("/api/v1/foods/{$food->id}/recent")
        ->assertOk();

    $recentUpdated = $this->user->recentFoods()->where('food_id', $food->id)->first();
    expect($recentUpdated->pivot->use_count)->toBe(2);
});

test('recents endpoint returns recent foods ordered by last_used_at', function () {
    $food1 = Food::first();
    $food2 = Food::skip(1)->first();

    // User records food1 then food2
    $this->actingAs($this->user)->postJson("/api/v1/foods/{$food1->id}/recent");
    $this->actingAs($this->user)->postJson("/api/v1/foods/{$food2->id}/recent");

    $response = $this->actingAs($this->user)
        ->getJson('/api/v1/foods/recents')
        ->assertOk();

    $data = $response->json('data');
    expect($data)->toHaveCount(2);
    // food2 is most recent
    expect($data[0]['id'])->toBe($food2->id);
    expect($data[1]['id'])->toBe($food1->id);
});
