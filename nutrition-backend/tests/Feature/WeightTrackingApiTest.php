<?php

use App\Models\User;
use App\Models\UserProfile;
use App\Models\WeightLog;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->user = User::factory()->create();
    UserProfile::create([
        'user_id' => $this->user->id,
        'current_weight' => 80.0,
        'goal_weight' => 75.0,
        'water_target_ml' => 2500,
    ]);
    $this->actingAs($this->user);
});

it('can log weight and update user profile current weight', function () {
    $response = $this->postJson('/api/v1/weight/logs', [
        'client_id' => 'weight-uuid-999',
        'log_date' => '2026-08-21',
        'weight_kg' => 78.5,
        'source' => 'manual',
        'notes' => 'Pesaje en ayunas',
    ]);

    $response->assertStatus(201)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.weight_kg', '78.50');

    $this->assertDatabaseHas('weight_logs', [
        'user_id' => $this->user->id,
        'weight_kg' => 78.50,
        'notes' => 'Pesaje en ayunas',
    ]);

    expect($this->user->fresh()->profile->current_weight)->toEqual(78.5);
});

it('can list weight history with lbs conversion', function () {
    WeightLog::create([
        'user_id' => $this->user->id,
        'log_date' => '2026-08-20',
        'weight_kg' => 79.0,
        'source' => 'manual',
    ]);

    WeightLog::create([
        'user_id' => $this->user->id,
        'log_date' => '2026-08-21',
        'weight_kg' => 78.5,
        'source' => 'health_connect',
    ]);

    $response = $this->getJson('/api/v1/weight/logs');

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.current_weight_kg', 78.5)
        ->assertJsonCount(2, 'data.logs')
        ->assertJsonPath('data.logs.0.weight_lbs', 173.1);

    expect((float) $response->json('data.target_weight_kg'))->toEqual(75.0);
});



it('can delete weight log', function () {
    $log = WeightLog::create([
        'user_id' => $this->user->id,
        'log_date' => '2026-08-21',
        'weight_kg' => 78.5,
        'source' => 'manual',
    ]);

    $response = $this->deleteJson("/api/v1/weight/logs/{$log->id}");

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success');

    $this->assertSoftDeleted('weight_logs', [
        'id' => $log->id,
    ]);
});
