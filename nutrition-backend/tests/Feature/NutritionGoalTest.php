<?php

use App\Models\User;
use App\Models\UserProfile;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

test('unauthenticated users cannot access goal endpoints', function () {
    $this->postJson('/api/v1/goals/calculate')->assertStatus(401);
    $this->getJson('/api/v1/goals/current')->assertStatus(401);
    $this->putJson('/api/v1/goals', [
        'calorie_target' => 2000,
        'protein_target_g' => 150,
        'carbohydrate_target_g' => 200,
        'fat_target_g' => 60,
    ])->assertStatus(401);
});

test('calculate endpoint returns valid Mifflin-St Jeor math for male lose_weight', function () {
    $user = User::factory()->create();

    // Male, 25 years old (born 2001-01-01), 180cm, 80kg, sedentary (1.2), lose_weight 0.5kg/week (-550 kcal)
    // BMR = 10(80) + 6.25(180) - 5(25) + 5 = 800 + 1125 - 125 + 5 = 1805
    // TDEE = 1805 * 1.2 = 2166
    // Target = 2166 - 550 = 1616 kcal
    // Protein 30% = (1616 * 0.3) / 4 = 121.2 g
    // Carbs 40% = (1616 * 0.4) / 4 = 161.6 g
    // Fat 30% = (1616 * 0.3) / 9 = 53.87 g
    $response = $this->actingAs($user)->postJson('/api/v1/goals/calculate', [
        'birth_date' => now()->subYears(25)->format('Y-m-d'),
        'sex' => 'male',
        'height' => 180,
        'current_weight' => 80,
        'activity_level' => 'sedentary',
        'goal_type' => 'lose_weight',
        'weekly_goal_rate' => 0.5,
    ]);

    $response->assertStatus(200)
        ->assertJsonStructure([
            'calculated_goal' => [
                'bmr',
                'tdee',
                'calorie_target',
                'protein_target_g',
                'carbohydrate_target_g',
                'fat_target_g',
                'fiber_target_g',
                'water_target_ml',
                'calculation_version',
            ],
        ]);

    $data = $response->json('calculated_goal');
    expect($data['bmr'])->toEqual(1805)
        ->and($data['tdee'])->toEqual(2166)
        ->and($data['calorie_target'])->toBe(1616)
        ->and($data['protein_target_g'])->toEqual(121.2)
        ->and($data['carbohydrate_target_g'])->toEqual(161.6)
        ->and($data['fat_target_g'])->toEqual(53.87)
        ->and($data['calculation_version'])->toBe('mifflin_v1.0');
});

test('calculate endpoint enforces minimum safety calorie floor of 1200 kcal for females', function () {
    $user = User::factory()->create();

    // Female, 30 years old, 150cm, 45kg, sedentary, aggressive deficit 1.0kg/week (-1100 kcal)
    // BMR = 10(45) + 6.25(150) - 5(30) - 161 = 450 + 937.5 - 150 - 161 = 1076.5
    // TDEE = 1076.5 * 1.2 = 1291.8
    // Target before floor = 1291.8 - 1100 = 191.8 kcal -> Enforces 1200 kcal floor!
    $response = $this->actingAs($user)->postJson('/api/v1/goals/calculate', [
        'birth_date' => now()->subYears(30)->format('Y-m-d'),
        'sex' => 'female',
        'height' => 150,
        'current_weight' => 45,
        'activity_level' => 'sedentary',
        'goal_type' => 'lose_weight',
        'weekly_goal_rate' => 1.0,
    ]);

    $response->assertStatus(200);
    $data = $response->json('calculated_goal');
    expect($data['calorie_target'])->toBe(1200);
});

test('current endpoint auto-generates initial goal if user has none', function () {
    $user = User::factory()->create();
    UserProfile::create([
        'user_id' => $user->id,
        'birth_date' => now()->subYears(28)->format('Y-m-d'),
        'sex' => 'female',
        'height' => 165,
        'current_weight' => 60,
        'activity_level' => 'moderate',
        'goal_type' => 'maintain_weight',
        'weekly_goal_rate' => 0.0,
    ]);

    $response = $this->actingAs($user)->getJson('/api/v1/goals/current');

    $response->assertStatus(200)
        ->assertJsonStructure([
            'goal' => [
                'id',
                'user_id',
                'effective_from',
                'calorie_target',
                'protein_target_g',
                'carbohydrate_target_g',
                'fat_target_g',
                'fiber_target_g',
                'water_target_ml',
                'source',
                'calculation_version',
            ],
        ]);

    $this->assertDatabaseHas('nutrition_goals', [
        'user_id' => $user->id,
        'source' => 'calculated',
    ]);
});

test('update endpoint persists custom nutrition goals', function () {
    $user = User::factory()->create();

    $payload = [
        'calorie_target' => 2400,
        'protein_target_g' => 180.0,
        'carbohydrate_target_g' => 250.0,
        'fat_target_g' => 70.0,
        'fiber_target_g' => 35.0,
        'water_target_ml' => 3000,
        'source' => 'custom',
    ];

    $response = $this->actingAs($user)->putJson('/api/v1/goals', $payload);

    $response->assertStatus(200)
        ->assertJsonPath('goal.calorie_target', 2400);

    $this->assertDatabaseHas('nutrition_goals', [
        'user_id' => $user->id,
        'calorie_target' => 2400,
        'source' => 'custom',
    ]);
});
