<?php

use App\Http\Controllers\Api\V1\AiPhotoController;
use App\Http\Controllers\Api\V1\AuthController;

use App\Http\Controllers\Api\V1\DiaryController;
use App\Http\Controllers\Api\V1\FoodController;
use App\Http\Controllers\Api\V1\GoalController;
use App\Http\Controllers\Api\V1\HealthController;
use App\Http\Controllers\Api\V1\ProfileController;
use Illuminate\Support\Facades\Route;

Route::get('/health', HealthController::class)->name('api.v1.health');

Route::prefix('auth')->group(function () {
    Route::post('/register', [AuthController::class, 'register'])->name('api.v1.auth.register');
    Route::post('/login', [AuthController::class, 'login'])->name('api.v1.auth.login');
    Route::post('/logout', [AuthController::class, 'logout'])->middleware('auth:sanctum')->name('api.v1.auth.logout');
});

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/me', [AuthController::class, 'me'])->name('api.v1.me');
    Route::delete('/me', [AuthController::class, 'destroy'])->name('api.v1.me.destroy');

    Route::get('/profile', [ProfileController::class, 'show'])->name('api.v1.profile.show');
    Route::put('/profile', [ProfileController::class, 'update'])->name('api.v1.profile.update');

    Route::post('/goals/calculate', [GoalController::class, 'calculate'])->name('api.v1.goals.calculate');
    Route::get('/goals/current', [GoalController::class, 'current'])->name('api.v1.goals.current');
    Route::put('/goals', [GoalController::class, 'update'])->name('api.v1.goals.update');

    // Foods Catalog, Calculation, Favorites & Recents
    Route::get('/foods/search', [FoodController::class, 'search'])->name('api.v1.foods.search');
    Route::get('/foods/favorites', [FoodController::class, 'favorites'])->name('api.v1.foods.favorites');
    Route::post('/foods/{id}/favorite', [FoodController::class, 'toggleFavorite'])->whereNumber('id')->name('api.v1.foods.favorite.toggle');
    Route::get('/foods/{id}/favorite', [FoodController::class, 'isFavorite'])->whereNumber('id')->name('api.v1.foods.favorite.check');
    Route::get('/foods/recents', [FoodController::class, 'recents'])->name('api.v1.foods.recents');
    Route::post('/foods/{id}/recent', [FoodController::class, 'recordRecent'])->whereNumber('id')->name('api.v1.foods.recent.record');
    Route::get('/foods/{id}', [FoodController::class, 'show'])->whereNumber('id')->name('api.v1.foods.show');
    Route::get('/foods/barcode/{barcode}', [FoodController::class, 'byBarcode'])->name('api.v1.foods.barcode');
    Route::post('/foods/{id}/calculate', [FoodController::class, 'calculate'])->whereNumber('id')->name('api.v1.foods.calculate');
    Route::post('/foods/ocr/parse-label', [\App\Http\Controllers\Api\V1\NutritionLabelOcrController::class, 'parseLabel'])->name('api.v1.foods.ocr.parse');
    Route::post('/foods/from-label', [\App\Http\Controllers\Api\V1\NutritionLabelOcrController::class, 'createFromLabel'])->name('api.v1.foods.from_label');


    // AI Food Photo Analysis
    Route::post('/ai/photo/analyze', [AiPhotoController::class, 'analyze'])->name('api.v1.ai.photo.analyze');
    Route::get('/ai/photo/analyses/{id}', [AiPhotoController::class, 'show'])->whereNumber('id')->name('api.v1.ai.photo.show');
    Route::post('/ai/photo/analyses/{id}/confirm', [AiPhotoController::class, 'confirm'])->whereNumber('id')->name('api.v1.ai.photo.confirm');

    // AI Text & Voice Meal Logging
    Route::post('/ai/text/parse', [\App\Http\Controllers\Api\V1\AiTextVoiceController::class, 'parseText'])->name('api.v1.ai.text.parse');
    Route::post('/ai/text/confirm/{id}', [\App\Http\Controllers\Api\V1\AiTextVoiceController::class, 'confirm'])->whereNumber('id')->name('api.v1.ai.text.confirm');


    // Daily Diary & Meals Logging
    Route::get('/diary/{date}', [DiaryController::class, 'show'])->where('date', '\d{4}-\d{2}-\d{2}')->name('api.v1.diary.show');
    Route::post('/diary/{date}/entries', [DiaryController::class, 'addEntry'])->where('date', '\d{4}-\d{2}-\d{2}')->name('api.v1.diary.entries.add');
    Route::put('/diary/entries/{id}', [DiaryController::class, 'updateEntry'])->whereNumber('id')->name('api.v1.diary.entries.update');
    Route::delete('/diary/entries/{id}', [DiaryController::class, 'deleteEntry'])->whereNumber('id')->name('api.v1.diary.entries.delete');
    Route::post('/diary/copy-meal', [DiaryController::class, 'copyMeal'])->name('api.v1.diary.copy_meal');
    Route::post('/diary/copy-day', [DiaryController::class, 'copyDay'])->name('api.v1.diary.copy_day');
    Route::get('/diary/{date}/water', [DiaryController::class, 'water'])->where('date', '\d{4}-\d{2}-\d{2}')->name('api.v1.diary.water');
    Route::post('/diary/{date}/water', [DiaryController::class, 'logWater'])->where('date', '\d{4}-\d{2}-\d{2}')->name('api.v1.diary.water.log');
    Route::delete('/diary/water/{id}', [DiaryController::class, 'deleteWater'])->whereNumber('id')->name('api.v1.diary.water.delete');
    Route::get('/diary/{date}/summary', [DiaryController::class, 'summary'])->where('date', '\d{4}-\d{2}-\d{2}')->name('api.v1.diary.summary');

    // Water Tracking Dedicated API
    Route::get('/water/logs', [\App\Http\Controllers\Api\V1\WaterTrackingController::class, 'index'])->name('api.v1.water.index');
    Route::post('/water/logs', [\App\Http\Controllers\Api\V1\WaterTrackingController::class, 'store'])->name('api.v1.water.store');
    Route::delete('/water/logs/{id}', [\App\Http\Controllers\Api\V1\WaterTrackingController::class, 'destroy'])->whereNumber('id')->name('api.v1.water.destroy');

    // Weight Tracking Dedicated API
    Route::get('/weight/logs', [\App\Http\Controllers\Api\V1\WeightTrackingController::class, 'index'])->name('api.v1.weight.index');
    Route::post('/weight/logs', [\App\Http\Controllers\Api\V1\WeightTrackingController::class, 'store'])->name('api.v1.weight.store');
    Route::delete('/weight/logs/{id}', [\App\Http\Controllers\Api\V1\WeightTrackingController::class, 'destroy'])->whereNumber('id')->name('api.v1.weight.destroy');

    // Nutrition & Progress Statistics API
    Route::get('/statistics/summary', [\App\Http\Controllers\Api\V1\StatisticsController::class, 'summary'])->name('api.v1.statistics.summary');

    // Recipes API
    Route::get('/recipes', [\App\Http\Controllers\Api\V1\RecipeController::class, 'index'])->name('api.v1.recipes.index');
    Route::post('/recipes', [\App\Http\Controllers\Api\V1\RecipeController::class, 'store'])->name('api.v1.recipes.store');
    Route::get('/recipes/{id}', [\App\Http\Controllers\Api\V1\RecipeController::class, 'show'])->whereNumber('id')->name('api.v1.recipes.show');
    Route::put('/recipes/{id}', [\App\Http\Controllers\Api\V1\RecipeController::class, 'update'])->whereNumber('id')->name('api.v1.recipes.update');
    Route::delete('/recipes/{id}', [\App\Http\Controllers\Api\V1\RecipeController::class, 'destroy'])->whereNumber('id')->name('api.v1.recipes.destroy');
    Route::post('/recipes/{id}/log-to-diary', [\App\Http\Controllers\Api\V1\RecipeController::class, 'logToDiary'])->whereNumber('id')->name('api.v1.recipes.log_to_diary');
});



