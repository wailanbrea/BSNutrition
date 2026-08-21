<?php

use App\Models\Food;
use App\Models\FoodCategory;
use App\Models\User;
use Database\Seeders\DatabaseSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Http;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed(DatabaseSeeder::class);
    $this->user = User::factory()->create();
});

test('unauthenticated users cannot search foods or view food details', function () {
    $this->getJson('/api/v1/foods/search')->assertStatus(401);
    $this->getJson('/api/v1/foods/1')->assertStatus(401);
    $this->getJson('/api/v1/foods/barcode/123456789')->assertStatus(401);
    $this->postJson('/api/v1/foods/1/calculate', ['quantity' => 100])->assertStatus(401);
});

test('search endpoint returns paginated food items with macros per 100g', function () {
    $response = $this->actingAs($this->user)
        ->getJson('/api/v1/foods/search?per_page=10')
        ->assertOk()
        ->assertJsonStructure([
            'data' => [
                '*' => [
                    'id',
                    'canonical_name',
                    'brand',
                    'category' => ['id', 'name', 'slug', 'icon'],
                    'country_code',
                    'verified',
                    'source',
                    'default_basis_amount',
                    'default_basis_unit',
                    'macros_per_100g' => [
                        'calories',
                        'protein_g',
                        'carbs_g',
                        'fat_g',
                        'fiber_g',
                    ],
                    'default_portion',
                ],
            ],
            'links',
            'meta',
        ]);

    expect(count($response->json('data')))->toBeGreaterThanOrEqual(10);
});

test('search endpoint finds foods by alias and canonical name', function () {
    $response = $this->actingAs($this->user)
        ->getJson('/api/v1/foods/search?query=los+tres+golpes')
        ->assertOk();

    $items = $response->json('data');
    expect($items)->not->toBeEmpty()
        ->and($items[0]['canonical_name'])->toBe('Mangú de Plátano Verde');
});

test('search endpoint filters by category', function () {
    $cat = FoodCategory::where('slug', 'carnes-y-aves')->first();

    $response = $this->actingAs($this->user)
        ->getJson("/api/v1/foods/search?category_id={$cat->id}")
        ->assertOk();

    $items = $response->json('data');
    expect($items)->not->toBeEmpty();
    foreach ($items as $item) {
        expect($item['category']['id'])->toBe($cat->id);
    }
});

test('show endpoint returns full food details with all nutrients and portions', function () {
    $mangu = Food::search('mangu')->first();

    $response = $this->actingAs($this->user)
        ->getJson("/api/v1/foods/{$mangu->id}")
        ->assertOk()
        ->assertJsonStructure([
            'data' => [
                'id',
                'canonical_name',
                'category',
                'country_code',
                'language',
                'verified',
                'portions' => [
                    '*' => [
                        'id',
                        'portion_name',
                        'gram_weight',
                        'amount',
                        'unit',
                        'is_default',
                    ],
                ],
                'nutrients' => [
                    '*' => [
                        'id',
                        'code',
                        'name',
                        'unit',
                        'amount',
                        'basis_amount',
                        'basis_unit',
                        'is_macro',
                    ],
                ],
            ],
        ]);

    $data = $response->json('data');
    expect($data['canonical_name'])->toBe('Mangú de Plátano Verde')
        ->and(count($data['portions']))->toBeGreaterThanOrEqual(1)
        ->and(count($data['nutrients']))->toBeGreaterThanOrEqual(5);
});

test('byBarcode endpoint falls back to Open Food Facts if not locally cached', function () {
    $barcode = '8480000778899';

    Http::fake([
        "https://world.openfoodfacts.org/api/v2/product/{$barcode}.json" => Http::response([
            'status' => 1,
            'code' => $barcode,
            'product' => [
                'code' => $barcode,
                'product_name' => 'Atún Claro al Natural',
                'brands' => 'Calvo',
                'nutriments' => [
                    'energy-kcal_100g' => 101,
                    'proteins_100g' => 24.0,
                    'fat_100g' => 0.6,
                ],
            ],
        ], 200),
    ]);

    $response = $this->actingAs($this->user)
        ->getJson("/api/v1/foods/barcode/{$barcode}")
        ->assertOk();

    expect($response->json('data.canonical_name'))->toBe('Atún Claro al Natural')
        ->and($response->json('data.brand.name'))->toBe('Calvo');
});

test('calculate endpoint computes nutritional breakdown for custom portion', function () {
    $mangu = Food::search('mangu')->first();
    $portion = $mangu->portions()->where('is_default', true)->first();

    $response = $this->actingAs($this->user)
        ->postJson("/api/v1/foods/{$mangu->id}/calculate", [
            'quantity' => 1.5,
            'portion_id' => $portion->id,
        ])
        ->assertOk()
        ->assertJsonStructure([
            'data' => [
                'food_id',
                'food_name',
                'quantity',
                'unit',
                'grams',
                'calories_snapshot',
                'protein_snapshot',
                'carbs_snapshot',
                'fat_snapshot',
                'nutrients',
            ],
        ]);

    // 1.5 portions of 200g = 300g (155 * 3 = 465 kcal)
    expect($response->json('data.grams'))->toEqual(300.0)
        ->and($response->json('data.calories_snapshot'))->toBe(465);
});
