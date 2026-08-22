<?php

use App\Models\AiImageUpload;
use App\Models\AiPhotoAnalysis;
use App\Models\AiPhotoAnalysisItem;
use App\Models\Food;
use App\Models\Nutrient;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->normalUser = User::factory()->create(['role' => 'user']);
    $this->adminUser = User::factory()->create(['role' => 'admin']);
    $this->curatorUser = User::factory()->create(['role' => 'curator']);
});

it('denies access to admin endpoints for normal users with 403', function () {
    $this->actingAs($this->normalUser);

    $response = $this->getJson('/api/v1/admin/dashboard/stats');
    $response->assertStatus(403)
        ->assertJsonPath('status', 'error');
});

it('allows admin users to get operational dashboard stats and audit logs', function () {
    $this->actingAs($this->adminUser);

    $response = $this->getJson('/api/v1/admin/dashboard/stats');
    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonStructure([
            'data' => [
                'users' => ['total', 'active_today', 'new_this_week'],
                'catalog' => ['total_foods', 'dominican_foods', 'verified_foods'],
                'ai_operations' => ['total_photo_analyses', 'low_confidence_queue'],
                'activity' => ['total_meal_entries', 'total_water_logs', 'total_weight_logs'],
                'recent_audits',
            ],
        ]);
});

it('allows admin/curator to curate and verify foods with automatic audit logging', function () {
    $this->actingAs($this->adminUser);

    Nutrient::firstOrCreate(['code' => 'ENERGY_KCAL'], ['name' => 'Energía', 'unit' => 'kcal']);
    Nutrient::firstOrCreate(['code' => 'PROTEIN_G'], ['name' => 'Proteína', 'unit' => 'g']);

    $createResponse = $this->postJson('/api/v1/admin/foods', [
        'canonical_name' => 'Sancocho Criollo Especial',
        'source' => 'dominican_dataset',
        'country_code' => 'DO',
        'verified' => false,
        'default_basis_amount' => 100.0,
        'default_basis_unit' => 'g',
        'nutrients' => [
            ['code' => 'ENERGY_KCAL', 'amount' => 120.0],
            ['code' => 'PROTEIN_G', 'amount' => 8.5],
        ],
        'aliases' => ['Sancocho de siete carnes'],
    ]);

    $createResponse->assertStatus(201)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.canonical_name', 'Sancocho Criollo Especial');

    $foodId = $createResponse->json('data.id');

    // Verify food
    $verifyResponse = $this->postJson("/api/v1/admin/foods/{$foodId}/verify");
    $verifyResponse->assertStatus(200)
        ->assertJsonPath('data.verified', true);

    $this->assertDatabaseHas('audit_logs', [
        'user_id' => $this->adminUser->id,
        'action' => 'food.create',
        'target_id' => $foodId,
    ]);

    $this->assertDatabaseHas('audit_logs', [
        'user_id' => $this->adminUser->id,
        'action' => 'food.verify',
        'target_id' => $foodId,
    ]);
});

it('allows curator to manage dominican curation queue and add colloquial aliases', function () {
    $this->actingAs($this->curatorUser);

    $food = Food::create([
        'canonical_name' => 'Chivo Liniero',
        'normalized_name' => 'chivo liniero',
        'source' => 'dominican_dataset',
        'country_code' => 'DO',
        'verified' => false,
        'default_basis_amount' => 100.0,
        'default_basis_unit' => 'g',
    ]);

    // Queue
    $queueResponse = $this->getJson('/api/v1/admin/curation/dominican-queue?status=unverified');
    $queueResponse->assertStatus(200)
        ->assertJsonCount(1, 'data.data');

    // Add alias
    $aliasResponse = $this->postJson("/api/v1/admin/curation/foods/{$food->id}/aliases", [
        'alias' => 'Chivo guisado al ron',
    ]);
    $aliasResponse->assertStatus(201);

    // Approve
    $approveResponse = $this->postJson("/api/v1/admin/curation/approve/{$food->id}");
    $approveResponse->assertStatus(200)
        ->assertJsonPath('data.verified', true);
});

it('allows admin to review AI low confidence queue and resolve matches', function () {
    $this->actingAs($this->adminUser);

    $food = Food::create([
        'canonical_name' => 'Habichuelas Rojas Guisadas',
        'normalized_name' => 'habichuelas rojas guisadas',
        'source' => 'dominican_dataset',
        'country_code' => 'DO',
        'verified' => true,
        'default_basis_amount' => 100.0,
        'default_basis_unit' => 'g',
    ]);

    $upload = AiImageUpload::create([
        'user_id' => $this->adminUser->id,
        'disk' => 'private',
        'path' => 'private/test.jpg',
        'original_name' => 'test.jpg',
        'mime_type' => 'image/jpeg',
        'file_size_bytes' => 1024,
    ]);


    $analysis = AiPhotoAnalysis::create([
        'user_id' => $this->adminUser->id,
        'upload_id' => $upload->id,
        'provider' => 'openai',
        'model' => 'gpt-4o',
        'status' => 'completed',
    ]);


    $item = AiPhotoAnalysisItem::create([
        'analysis_id' => $analysis->id,
        'name' => 'Frijoles',
        'estimated_weight_grams' => 150.0,
        'confidence' => 0.65,
    ]);

    // Queue should include this analysis
    $queueResponse = $this->getJson('/api/v1/admin/ai/review-queue?status=needs_review');
    $queueResponse->assertStatus(200)
        ->assertJsonCount(1, 'data.data');

    // Resolve analysis item
    $resolveResponse = $this->postJson("/api/v1/admin/ai/reviews/{$analysis->id}/resolve", [
        'item_corrections' => [
            [
                'id' => $item->id,
                'food_id' => $food->id,
            ],
        ],
    ]);

    $resolveResponse->assertStatus(200)
        ->assertJsonPath('status', 'success');

    $this->assertDatabaseHas('ai_photo_analysis_items', [
        'id' => $item->id,
        'food_id' => $food->id,
    ]);
});

