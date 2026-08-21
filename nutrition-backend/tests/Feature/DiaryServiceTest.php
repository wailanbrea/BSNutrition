<?php

use App\Models\Food;
use App\Models\MealEntry;
use App\Models\User;
use App\Services\DiaryService;
use Database\Seeders\DatabaseSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Validation\ValidationException;

uses(RefreshDatabase::class);

beforeEach(function () {
    $this->seed(DatabaseSeeder::class);
    $this->user = User::factory()->create();
    $this->otherUser = User::factory()->create();
    $this->diaryService = app(DiaryService::class);
});

test('getOrCreateDiaryForDate creates diary and default 4 meals', function () {
    $date = '2026-08-21';
    $diary = $this->diaryService->getOrCreateDiaryForDate($this->user, $date);

    expect($diary)->not->toBeNull();
    expect($diary->user_id)->toBe($this->user->id);
    expect($diary->meals)->toHaveCount(4);

    $mealTypes = $diary->meals->pluck('meal_type')->toArray();
    expect($mealTypes)->toContain('breakfast', 'lunch', 'dinner', 'snack');
});

test('addEntry computes snapshots correctly from food catalog', function () {
    $date = '2026-08-21';
    $mangu = Food::where('canonical_name', 'Mangú de Plátano Verde')->first();

    $entry = $this->diaryService->addEntry($this->user, $date, [
        'meal_type' => 'breakfast',
        'food_id' => $mangu->id,
        'quantity' => 1.0,
        'unit' => 'porción',
    ]);

    expect($entry)->not->toBeNull();
    expect($entry->custom_name)->toBe('Mangú de Plátano Verde');
    expect($entry->calories_snapshot)->toBe(310);
    expect((float) $entry->protein_snapshot)->toBe(3.0);
    expect((float) $entry->carbs_snapshot)->toBe(62.0);
    expect((float) $entry->fat_snapshot)->toBe(6.4);
});

test('addEntry handles client_id idempotency', function () {
    $date = '2026-08-21';
    $clientId = 'client-uuid-12345';
    $mangu = Food::where('canonical_name', 'Mangú de Plátano Verde')->first();

    $entry1 = $this->diaryService->addEntry($this->user, $date, [
        'client_id' => $clientId,
        'meal_type' => 'breakfast',
        'food_id' => $mangu->id,
        'quantity' => 1.0,
    ]);

    // Second call with same client_id should return existing entry without creating duplicate
    $entry2 = $this->diaryService->addEntry($this->user, $date, [
        'client_id' => $clientId,
        'meal_type' => 'breakfast',
        'food_id' => $mangu->id,
        'quantity' => 2.0,
    ]);

    expect($entry1->id)->toBe($entry2->id);
    expect(MealEntry::where('client_id', $clientId)->count())->toBe(1);
});

test('updateEntry modifies entry and updates version', function () {
    $date = '2026-08-21';
    $mangu = Food::where('canonical_name', 'Mangú de Plátano Verde')->first();

    $entry = $this->diaryService->addEntry($this->user, $date, [
        'meal_type' => 'breakfast',
        'food_id' => $mangu->id,
        'quantity' => 1.0,
    ]);

    $updated = $this->diaryService->updateEntry($this->user, $entry->id, [
        'quantity' => 2.0,
    ]);

    expect((float) $updated->quantity)->toBe(2.0);
    expect($updated->calories_snapshot)->toBe(620);
    expect($updated->version)->toBe(2);
});

test('user cannot update or delete entries belonging to another user', function () {
    $date = '2026-08-21';
    $mangu = Food::where('canonical_name', 'Mangú de Plátano Verde')->first();

    $entry = $this->diaryService->addEntry($this->user, $date, [
        'meal_type' => 'breakfast',
        'food_id' => $mangu->id,
        'quantity' => 1.0,
    ]);

    expect(fn () => $this->diaryService->updateEntry($this->otherUser, $entry->id, ['quantity' => 2.0]))
        ->toThrow(ValidationException::class);

    expect(fn () => $this->diaryService->deleteEntry($this->otherUser, $entry->id))
        ->toThrow(ValidationException::class);
});

test('copyMeal duplicates all meal entries to target date/meal', function () {
    $date = '2026-08-21';
    $mangu = Food::where('canonical_name', 'Mangú de Plátano Verde')->first();

    $entry = $this->diaryService->addEntry($this->user, $date, [
        'meal_type' => 'breakfast',
        'food_id' => $mangu->id,
        'quantity' => 1.0,
    ]);

    $sourceMeal = $entry->meal;
    $targetDate = '2026-08-22';

    $copiedMeal = $this->diaryService->copyMeal($this->user, $sourceMeal->id, $targetDate, 'breakfast');

    expect($copiedMeal->entries)->toHaveCount(1);
    expect($copiedMeal->entries[0]->custom_name)->toBe('Mangú de Plátano Verde');
    expect($copiedMeal->entries[0]->calories_snapshot)->toBe(310);
});

test('copyDay duplicates all meals and entries to another date', function () {
    $date = '2026-08-21';
    $mangu = Food::where('canonical_name', 'Mangú de Plátano Verde')->first();
    $pollo = Food::where('canonical_name', 'Pollo Guisado Dominicano')->first();

    $this->diaryService->addEntry($this->user, $date, [
        'meal_type' => 'breakfast',
        'food_id' => $mangu->id,
        'quantity' => 1.0,
    ]);
    $this->diaryService->addEntry($this->user, $date, [
        'meal_type' => 'lunch',
        'food_id' => $pollo->id,
        'quantity' => 1.0,
    ]);

    $targetDate = '2026-08-23';
    $targetDiary = $this->diaryService->copyDay($this->user, $date, $targetDate);

    $summary = $this->diaryService->getDailySummary($this->user, $targetDate);
    expect($summary['calories'])->toBeGreaterThan(0);
    expect($summary['meals'])->toHaveCount(4);
});

test('water logging logs and sums daily amounts', function () {
    $date = '2026-08-21';

    $this->diaryService->logWater($this->user, $date, 250);
    $this->diaryService->logWater($this->user, $date, 500);

    $total = $this->diaryService->getDailyWaterTotal($this->user, $date);
    expect($total)->toBe(750);
});
