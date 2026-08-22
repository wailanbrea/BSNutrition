<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\WeightLog;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class WeightTrackingController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $request->validate([
            'from' => ['nullable', 'date_format:Y-m-d'],
            'to' => ['nullable', 'date_format:Y-m-d'],
            'limit' => ['nullable', 'integer', 'min:1', 'max:100'],
        ]);

        $userId = $request->user()->id;
        $query = WeightLog::where('user_id', $userId);

        if ($request->filled('from') && $request->filled('to')) {
            $query->whereBetween('log_date', [$request->input('from'), $request->input('to')]);
        }

        $limit = $request->input('limit', 30);
        $logs = $query->orderBy('log_date', 'desc')->limit($limit)->get();

        $latest = $logs->first();
        $targetWeight = $request->user()->profile?->goal_weight;

        return response()->json([
            'status' => 'success',
            'data' => [
                'current_weight_kg' => $latest ? (float) $latest->weight_kg : null,
                'target_weight_kg' => $targetWeight ? (float) $targetWeight : null,
                'logs' => $logs->map(fn ($log) => [
                    'id' => $log->id,
                    'client_id' => $log->client_id,
                    'log_date' => $log->log_date->format('Y-m-d'),
                    'weight_kg' => (float) $log->weight_kg,
                    'weight_lbs' => round(((float) $log->weight_kg) * 2.20462, 1),
                    'occurred_at' => $log->occurred_at->toIso8601String(),
                    'source' => $log->source,
                    'notes' => $log->notes,
                ]),
            ],
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'client_id' => ['nullable', 'string', 'max:64'],
            'log_date' => ['required', 'date_format:Y-m-d'],
            'weight_kg' => ['required', 'numeric', 'min:20', 'max:400'],
            'occurred_at' => ['nullable', 'date'],
            'source' => ['nullable', 'string', 'max:30'],
            'notes' => ['nullable', 'string', 'max:255'],
        ]);

        $user = $request->user();

        // Idempotency check via client_id
        if (! empty($validated['client_id'])) {
            $existing = WeightLog::where('user_id', $user->id)
                ->where('client_id', $validated['client_id'])
                ->first();

            if ($existing) {
                return response()->json([
                    'status' => 'success',
                    'message' => 'Registro de peso ya sincronizado.',
                    'data' => $existing,
                ], 200);
            }
        }

        $weightLog = WeightLog::create([
            'user_id' => $user->id,
            'client_id' => $validated['client_id'] ?? null,
            'log_date' => $validated['log_date'],
            'weight_kg' => $validated['weight_kg'],
            'occurred_at' => $validated['occurred_at'] ?? now(),
            'source' => $validated['source'] ?? 'manual',
            'notes' => $validated['notes'] ?? null,
            'version' => 1,
        ]);

        // Optionally update user profile current weight
        if ($user->profile) {
            $user->profile->update([
                'current_weight' => $validated['weight_kg'],
            ]);
        }


        return response()->json([
            'status' => 'success',
            'message' => 'Peso registrado exitosamente.',
            'data' => $weightLog,
        ], 201);
    }

    public function destroy(Request $request, int $id): JsonResponse
    {
        $weightLog = WeightLog::where('user_id', $request->user()->id)
            ->where('id', $id)
            ->firstOrFail();

        $weightLog->delete();

        return response()->json([
            'status' => 'success',
            'message' => 'Registro de peso eliminado.',
        ]);
    }
}
