<?php

use App\Models\User;
use App\Models\UserDailyAiQuota;
use App\Models\UserSubscription;
use App\Services\AiQuotaService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->user = User::factory()->create(['role' => 'user']);
    $this->actingAs($this->user);
});

it('returns free subscription tier and initial daily quotas', function () {
    $response = $this->getJson('/api/v1/billing/status');

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.tier', 'free')
        ->assertJsonPath('data.is_pro', false)
        ->assertJsonPath('data.quotas.photo_analyses.limit', 3)
        ->assertJsonPath('data.quotas.photo_analyses.remaining', 3)
        ->assertJsonPath('data.quotas.text_parses.limit', 5)
        ->assertJsonPath('data.quotas.text_parses.remaining', 5);
});

it('enforces atomic daily photo quota and returns 429 when exhausted', function () {
    $quotaService = app(AiQuotaService::class);

    // Consume 3 times
    $r1 = $quotaService->checkAndConsumePhotoQuota($this->user);
    expect($r1['used'])->toBe(1)->and($r1['remaining'])->toBe(2);

    $r2 = $quotaService->checkAndConsumePhotoQuota($this->user);
    expect($r2['used'])->toBe(2)->and($r2['remaining'])->toBe(1);

    $r3 = $quotaService->checkAndConsumePhotoQuota($this->user);
    expect($r3['used'])->toBe(3)->and($r3['remaining'])->toBe(0);

    // 4th time via endpoint should fail with 429 AI_QUOTA_EXCEEDED
    $file = UploadedFile::fake()->image('food.jpg', 600, 600);
    $response = $this->postJson('/api/v1/ai/photo/analyze', [
        'image' => $file,
    ]);

    $response->assertStatus(429)
        ->assertJsonPath('status', 'error')
        ->assertJsonPath('code', 'AI_QUOTA_EXCEEDED');
});

it('verifies google play purchase token and activates pro subscription', function () {
    $response = $this->postJson('/api/v1/billing/verify-play-purchase', [
        'product_id' => 'bsnutrition_pro_monthly',
        'purchase_token' => 'play-token-xyz-12345',
        'order_id' => 'GPA.1234-5678-9012-34567',
    ]);

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.subscription.plan_id', 'pro_monthly')
        ->assertJsonPath('data.subscription.status', 'active');

    $this->assertDatabaseHas('user_subscriptions', [
        'user_id' => $this->user->id,
        'plan_id' => 'pro_monthly',
        'status' => 'active',
        'order_id' => 'GPA.1234-5678-9012-34567',
    ]);

    expect($this->user->fresh()->isPro())->toBeTrue();
});

it('allows unlimited photo and text analyses for pro subscribers', function () {
    // Activate Pro
    UserSubscription::create([
        'user_id' => $this->user->id,
        'plan_id' => 'pro_monthly',
        'status' => 'active',
        'starts_at' => now(),
        'expires_at' => now()->addMonth(),
    ]);

    $quotaService = app(AiQuotaService::class);

    for ($i = 0; $i < 10; $i++) {
        $res = $quotaService->checkAndConsumePhotoQuota($this->user->fresh());
        expect($res['unlimited'])->toBeTrue()
            ->and($res['is_pro'])->toBeTrue();
    }
});
