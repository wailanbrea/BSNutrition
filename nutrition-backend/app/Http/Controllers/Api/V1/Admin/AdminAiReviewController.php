<?php

namespace App\Http\Controllers\Api\V1\Admin;

use App\Http\Controllers\Controller;
use App\Models\AiPhotoAnalysis;
use App\Models\AuditLog;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminAiReviewController extends Controller
{
    public function queue(Request $request): JsonResponse
    {
        $status = $request->input('status', 'all'); // 'all', 'needs_review', 'completed'

        $q = AiPhotoAnalysis::with(['user:id,name,email', 'upload', 'items.food']);

        if ($status === 'needs_review') {
            $q->whereHas('items', function ($itemQuery) {
                $itemQuery->where('confidence', '<', 0.85);
            });
        }

        $analyses = $q->orderBy('created_at', 'desc')->paginate(20);

        return response()->json([
            'status' => 'success',
            'data' => $analyses,
        ]);
    }

    public function show(int $id): JsonResponse
    {
        $analysis = AiPhotoAnalysis::with(['user:id,name,email', 'upload', 'items.food'])
            ->findOrFail($id);


        return response()->json([
            'status' => 'success',
            'data' => $analysis,
        ]);
    }

    public function resolve(Request $request, int $id): JsonResponse
    {
        $validated = $request->validate([
            'notes' => ['nullable', 'string', 'max:500'],
            'item_corrections' => ['nullable', 'array'],
            'item_corrections.*.id' => ['required', 'integer'],
            'item_corrections.*.food_id' => ['required', 'integer', 'exists:foods,id'],
        ]);

        $analysis = AiPhotoAnalysis::with('items')->findOrFail($id);

        if (! empty($validated['item_corrections'])) {
            foreach ($validated['item_corrections'] as $correction) {
                $item = $analysis->items()->find($correction['id']);
                if ($item) {
                    $item->update(['food_id' => $correction['food_id']]);
                }
            }
        }

        AuditLog::log(
            user: $request->user(),
            action: 'ai_review.resolve',
            targetType: 'AiPhotoAnalysis',
            targetId: $analysis->id,
            newValues: $validated
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Análisis de IA revisado y calibrado.',
            'data' => $analysis->fresh(['items.food']),
        ]);
    }
}
