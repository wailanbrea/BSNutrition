<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Services\StatisticsService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class StatisticsController extends Controller
{
    public function __construct(
        private readonly StatisticsService $statisticsService
    ) {}

    public function summary(Request $request): JsonResponse
    {
        $request->validate([
            'period' => ['nullable', 'string', 'in:7d,30d,90d'],
        ]);

        $period = $request->input('period', '7d');
        $summary = $this->statisticsService->getSummary($request->user(), $period);

        return response()->json([
            'status' => 'success',
            'data' => $summary,
        ]);
    }
}
