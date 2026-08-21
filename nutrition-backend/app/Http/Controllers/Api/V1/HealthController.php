<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

class HealthController extends Controller
{
    public function __invoke(): JsonResponse
    {
        $dbStatus = 'ok';
        try {
            DB::connection()->getPdo();
        } catch (\Throwable $e) {
            $dbStatus = 'disconnected';
        }

        $isHealthy = $dbStatus === 'ok';

        return response()->json([
            'status' => $isHealthy ? 'ok' : 'degraded',
            'version' => config('app.version', '1.0.0'),
            'environment' => config('app.env'),
            'timestamp' => now()->toIso8601String(),
            'services' => [
                'database' => $dbStatus,
            ],
        ], $isHealthy ? 200 : 503);
    }
}
