<?php

use App\Http\Controllers\Api\V1\AuthController;
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
});
