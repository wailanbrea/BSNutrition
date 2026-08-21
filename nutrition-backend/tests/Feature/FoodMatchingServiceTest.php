<?php

use App\DTOs\FoodMatchCandidate;
use App\Models\Food;
use App\Models\FoodAlias;
use App\Services\FoodMatchingService;
use Database\Seeders\DominicanFoodDatasetSeeder;
use Database\Seeders\FoodCategorySeeder;
use Database\Seeders\FoodSourceSeeder;
use Database\Seeders\NutrientSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed([
        FoodSourceSeeder::class,
        FoodCategorySeeder::class,
        NutrientSeeder::class,
        DominicanFoodDatasetSeeder::class,
    ]);

    $this->matcher = new FoodMatchingService;
});

test('matchFoodName finds exact Dominican dish canonical match', function () {
    $candidate = $this->matcher->matchFoodName('Mangú de Plátano Verde');

    expect($candidate)->toBeInstanceOf(FoodMatchCandidate::class)
        ->and($candidate->food->canonical_name)->toBe('Mangú de Plátano Verde')
        ->and($candidate->score)->toBe(1.0)
        ->and($candidate->matchType)->toBe('exact');
});

test('matchFoodName matches colloquial alias like Mangu or Platanos Majados', function () {
    $candidate = $this->matcher->matchFoodName('Mangú');

    expect($candidate)->toBeInstanceOf(FoodMatchCandidate::class)
        ->and($candidate->food->canonical_name)->toBe('Mangú de Plátano Verde')
        ->and($candidate->score)->toBeGreaterThanOrEqual(0.90);
});

test('findCandidates returns top candidates for Dominican ingredients', function () {
    $candidates = $this->matcher->findCandidates('Salami frito', 'frito', 'DO', 3);

    expect($candidates)->toBeArray()
        ->and(count($candidates))->toBeGreaterThanOrEqual(1)
        ->and($candidates[0]->food->canonical_name)->toBe('Salami Dominicano Frito')
        ->and($candidates[0]->score)->toBeGreaterThanOrEqual(0.70);
});


test('normalize function strips accents and special characters cleanly', function () {
    $clean = $this->matcher->normalize('¡Habichuelas Guisadas con Dulce & Canela!');
    expect($clean)->toBe('habichuelas guisadas con dulce canela');
});

test('matchFoodName returns null when confidence is below threshold', function () {
    $candidate = $this->matcher->matchFoodName('Plato extraterrestre desconocido 123', null, 'DO', 0.85);
    expect($candidate)->toBeNull();
});
