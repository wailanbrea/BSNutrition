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

test('unauthenticated users cannot access favorites endpoints', function () {
    $this->getJson('/api/v1/foods/favorites')
        ->assertStatus(401);

    $this->postJson('/api/v1/foods/1/favorite')
        ->assertStatus(401);

    $this->getJson('/api/v1/foods/1/favorite')
        ->assertStatus(401);
});

test('user can toggle a food as favorite and unfavorite it', function () {
    $food = Food::first();

    // 1. Initial check - not favorite
    $this->actingAs($this->user)
        ->getJson("/api/v1/foods/{$food->id}/favorite")
        ->assertOk()
        ->assertJson([
            'is_favorite' => false,
            'food_id' => $food->id,
        ]);

    // 2. Add to favorites
    $this->actingAs($this->user)
        ->postJson("/api/v1/foods/{$food->id}/favorite")
        ->assertOk()
        ->assertJson([
            'is_favorite' => true,
            'message' => 'Alimento agregado a favoritos.',
            'food_id' => $food->id,
        ]);

    expect($this->user->favoriteFoods()->count())->toBe(1);

    // 3. Check is_favorite is true
    $this->actingAs($this->user)
        ->getJson("/api/v1/foods/{$food->id}/favorite")
        ->assertOk()
        ->assertJson([
            'is_favorite' => true,
        ]);

    // 4. Remove from favorites (toggle again)
    $this->actingAs($this->user)
        ->postJson("/api/v1/foods/{$food->id}/favorite")
        ->assertOk()
        ->assertJson([
            'is_favorite' => false,
            'message' => 'Alimento eliminado de favoritos.',
            'food_id' => $food->id,
        ]);

    expect($this->user->favoriteFoods()->count())->toBe(0);
});

test('favorites endpoint returns only authenticated user favorites', function () {
    $food1 = Food::first();
    $food2 = Food::skip(1)->first();

    // User 1 favorites food1
    $this->user->favoriteFoods()->attach($food1->id);

    // User 2 favorites food2
    $this->otherUser->favoriteFoods()->attach($food2->id);

    // User 1 sees only food1
    $response = $this->actingAs($this->user)
        ->getJson('/api/v1/foods/favorites')
        ->assertOk();

    $data = $response->json('data');
    expect($data)->toHaveCount(1);
    expect($data[0]['id'])->toBe($food1->id);
    expect($data[0]['canonical_name'])->toBe($food1->canonical_name);

    // User 2 sees only food2
    $responseOther = $this->actingAs($this->otherUser)
        ->getJson('/api/v1/foods/favorites')
        ->assertOk();

    $dataOther = $responseOther->json('data');
    expect($dataOther)->toHaveCount(1);
    expect($dataOther[0]['id'])->toBe($food2->id);
});
