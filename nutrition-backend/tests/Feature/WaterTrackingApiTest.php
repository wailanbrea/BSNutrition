<?php

use App\Models\User;
use App\Models\WaterLog;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);


beforeEach(function () {

    $this->user = User::factory()->create();
    $this->actingAs($this->user);
});

it('can log water with idempotency key', function () {
    $clientId = 'water-uuid-1234';

    $response = $this->postJson('/api/v1/water/logs', [
        'client_id' => $clientId,
        'log_date' => '2026-08-21',
        'amount_ml' => 500,
        'occurred_at' => '2026-08-21 10:30:00',
        'source' => 'quick_add',
    ]);

    $response->assertStatus(201)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.amount_ml', 500);

    $this->assertDatabaseHas('water_logs', [
        'client_id' => $clientId,
        'user_id' => $this->user->id,
        'amount_ml' => 500,
    ]);

    // Resend same client_id -> idempotent 200
    $retryResponse = $this->postJson('/api/v1/water/logs', [
        'client_id' => $clientId,
        'log_date' => '2026-08-21',
        'amount_ml' => 500,
    ]);

    $retryResponse->assertStatus(200)
        ->assertJsonPath('status', 'success');

    expect(WaterLog::where('user_id', $this->user->id)->count())->toBe(1);
});

it('can list water logs and calculate totals', function () {
    WaterLog::create([
        'user_id' => $this->user->id,
        'log_date' => '2026-08-21',
        'amount_ml' => 250,
        'source' => 'manual',
    ]);

    WaterLog::create([
        'user_id' => $this->user->id,
        'log_date' => '2026-08-21',
        'amount_ml' => 500,
        'source' => 'manual',
    ]);

    $response = $this->getJson('/api/v1/water/logs?date=2026-08-21');

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.total_ml', 750)
        ->assertJsonCount(2, 'data.logs');
});

it('can delete water log', function () {
    $log = WaterLog::create([
        'user_id' => $this->user->id,
        'log_date' => '2026-08-21',
        'amount_ml' => 250,
        'source' => 'manual',
    ]);

    $response = $this->deleteJson("/api/v1/water/logs/{$log->id}");

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success');

    $this->assertSoftDeleted('water_logs', [
        'id' => $log->id,
    ]);
});
