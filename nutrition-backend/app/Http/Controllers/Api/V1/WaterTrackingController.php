<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\WaterLog;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class WaterTrackingController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $request->validate([
            'date' => ['nullable', 'date_format:Y-m-d'],
            'from' => ['nullable', 'date_format:Y-m-d'],
            'to' => ['nullable', 'date_format:Y-m-d'],
        ]);

        $userId = $request->user()->id;
        $query = WaterLog::where('user_id', $userId);

        if ($request->filled('date')) {
            $query->where('log_date', $request->input('date'));
        } elseif ($request->filled('from') && $request->filled('to')) {
            $query->whereBetween('log_date', [$request->input('from'), $request->input('to')]);
        }

        $logs = $query->orderBy('occurred_at', 'asc')->get();
        $totalMl = $logs->sum('amount_ml');

        return response()->json([
            'status' => 'success',
            'data' => [
                'total_ml' => $totalMl,
                'target_ml' => $request->user()->profile?->water_target_ml ?? 2500,
                'logs' => $logs->map(fn ($log) => [
                    'id' => $log->id,
                    'client_id' => $log->client_id,
                    'log_date' => $log->log_date->format('Y-m-d'),
                    'amount_ml' => $log->amount_ml,
                    'occurred_at' => $log->occurred_at->toIso8601String(),
                    'source' => $log->source,
                ]),
            ],
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'client_id' => ['nullable', 'string', 'max:64'],
            'log_date' => ['required', 'date_format:Y-m-d'],
            'amount_ml' => ['required', 'integer', 'min:1', 'max:5000'],
            'occurred_at' => ['nullable', 'date'],
            'source' => ['nullable', 'string', 'max:30'],
        ]);

        $user = $request->user();

        // Idempotency check via client_id
        if (! empty($validated['client_id'])) {
            $existing = WaterLog::where('user_id', $user->id)
                ->where('client_id', $validated['client_id'])
                ->first();

            if ($existing) {
                return response()->json([
                    'status' => 'success',
                    'message' => 'Registro de agua ya sincronizado.',
                    'data' => $existing,
                ], 200);
            }
        }

        $waterLog = WaterLog::create([
            'user_id' => $user->id,
            'client_id' => $validated['client_id'] ?? null,
            'log_date' => $validated['log_date'],
            'amount_ml' => $validated['amount_ml'],
            'occurred_at' => $validated['occurred_at'] ?? now(),
            'source' => $validated['source'] ?? 'manual',
            'version' => 1,
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Agua registrada exitosamente.',
            'data' => $waterLog,
        ], 201);
    }

    public function destroy(Request $request, int $id): JsonResponse
    {
        $waterLog = WaterLog::where('user_id', $request->user()->id)
            ->where('id', $id)
            ->firstOrFail();

        $waterLog->delete();

        return response()->json([
            'status' => 'success',
            'message' => 'Registro de agua eliminado.',
        ]);
    }
}
