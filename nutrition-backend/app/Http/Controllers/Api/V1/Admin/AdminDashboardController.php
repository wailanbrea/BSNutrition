<?php

namespace App\Http\Controllers\Api\V1\Admin;

use App\Http\Controllers\Controller;
use App\Models\AiPhotoAnalysis;
use App\Models\AuditLog;
use App\Models\Diary;
use App\Models\Food;
use App\Models\MealEntry;
use App\Models\Recipe;
use App\Models\User;
use App\Models\WaterLog;
use App\Models\WeightLog;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminDashboardController extends Controller
{
    public function stats(Request $request): JsonResponse
    {
        $today = now()->toDateString();

        $stats = [
            'users' => [
                'total' => User::count(),
                'active_today' => Diary::where('diary_date', $today)->distinct('user_id')->count('user_id'),
                'new_this_week' => User::where('created_at', '>=', now()->subDays(7))->count(),
            ],
            'catalog' => [
                'total_foods' => Food::count(),
                'dominican_foods' => Food::where('country_code', 'DO')->count(),
                'verified_foods' => Food::where('verified', true)->count(),
                'unverified_foods' => Food::where('verified', false)->count(),
                'total_recipes' => Recipe::count(),
            ],
            'ai_operations' => [
                'total_photo_analyses' => AiPhotoAnalysis::count(),
                'low_confidence_queue' => AiPhotoAnalysis::whereHas('items', function ($q) {
                    $q->where('confidence', '<', 0.85);
                })->count(),
            ],
            'activity' => [
                'total_meal_entries' => MealEntry::count(),
                'total_water_logs' => WaterLog::count(),
                'total_weight_logs' => WeightLog::count(),
            ],
            'recent_audits' => AuditLog::with('user:id,name,email')
                ->orderBy('created_at', 'desc')
                ->limit(10)
                ->get(),
        ];

        return response()->json([
            'status' => 'success',
            'data' => $stats,
        ]);
    }
}
