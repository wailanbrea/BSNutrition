<?php

use App\Models\Food;
use Database\Seeders\DatabaseSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

test('database seeder populates dominican foods with aliases, portions and nutrients', function () {
    $this->seed(DatabaseSeeder::class);

    // 1. Mangú exists with DO country code and aliases
    $mangu = Food::search('mangu')->first();
    expect($mangu)->not->toBeNull()
        ->and($mangu->canonical_name)->toBe('Mangú de Plátano Verde')
        ->and($mangu->country_code)->toBe('DO')
        ->and($mangu->verified)->toBeTrue();

    // 2. Check search by colloquial alias "los tres golpes"
    $tresGolpes = Food::search('los tres golpes')->first();
    expect($tresGolpes)->not->toBeNull()
        ->and($tresGolpes->id)->toBe($mangu->id);

    // 3. Check portions
    expect($mangu->portions)->not->toBeEmpty()
        ->and($mangu->portions->where('is_default', true)->first()->gram_weight)->toEqual(200.0);

    // 4. Check typical Dominican dishes
    expect(Food::search('pollo guisado')->exists())->toBeTrue()
        ->and(Food::search('habichuelas rojas guisadas')->exists())->toBeTrue()
        ->and(Food::search('sancocho')->exists())->toBeTrue()
        ->and(Food::search('tostones')->exists())->toBeTrue()
        ->and(Food::search('queso frito')->exists())->toBeTrue()
        ->and(Food::search('salami frito')->exists())->toBeTrue()
        ->and(Food::search('morir sonando')->exists())->toBeTrue()
        ->and(Food::search('mofongo')->exists())->toBeTrue();
});
