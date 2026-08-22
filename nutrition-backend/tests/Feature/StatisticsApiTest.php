<?php

use App\Models\Diary;
use App\Models\Meal;
use App\Models\MealEntry;
use App\Models\NutritionGoal;
use App\Models\User;
use App\Models\UserProfile;
use App\Models\WaterLog;
use App\Models\WeightLog;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

beforeEach(function () {

    $this->user = User::factory()->create();
    UserProfile::create([
        'user_id' => $this->user->id,
        'current_weight' => 78.0,
        'goal_weight' => 75.0,
        'water_target_ml' => 2500,
    ]);

    NutritionGoal::create([
        'user_id' => $this->user->id,
        'effective_from' => '2026-08-01',
        'calorie_target' => 2000,
        'protein_target_g' => 150.0,
        'carbohydrate_target_g' => 200.0,
        'fat_target_g' => 65.0,
        'calculation_version' => 'mifflin_v1.0',
    ]);

    $this->actingAs($this->user);
});


it('calculates 7d progress statistics accurately', function () {
    // Create diary entry for today
    $today = now()->format('Y-m-d');
    $diary = Diary::create([
        'user_id' => $this->user->id,
        'diary_date' => $today,
    ]);

    $meal = Meal::create([
        'diary_id' => $diary->id,
        'meal_type' => 'lunch',
    ]);

    MealEntry::create([
        'meal_id' => $meal->id,
        'custom_name' => 'Pollo con Arroz',
        'quantity' => 1.0,
        'grams' => 300.0,
        'calories_snapshot' => 600,
        'protein_snapshot' => 45.0,
        'carbs_snapshot' => 60.0,
        'fat_snapshot' => 15.0,
        'source' => 'catalog',
    ]);

    WaterLog::create([
        'user_id' => $this->user->id,
        'log_date' => $today,
        'amount_ml' => 1500,
        'source' => 'manual',
    ]);

    WeightLog::create([
        'user_id' => $this->user->id,
        'log_date' => $today,
        'weight_kg' => 77.5,
        'source' => 'manual',
    ]);

    $response = $this->getJson('/api/v1/statistics/summary?period=7d');

    $response->assertStatus(200)
        ->assertJsonPath('status', 'success')
        ->assertJsonPath('data.period', '7d')
        ->assertJsonPath('data.total_days', 7)
        ->assertJsonPath('data.tracked_days', 1)
        ->assertJsonPath('data.averages.calories', 600)
        ->assertJsonPath('data.averages.protein_g', 45)
        ->assertJsonPath('data.averages.water_ml', 1500)
        ->assertJsonPath('data.targets.calories', 2000)
        ->assertJsonPath('data.weight_summary.current_weight_kg', 77.5);
});
