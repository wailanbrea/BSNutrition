<?php

namespace App\Http\Controllers\Api\V1\Admin;

use App\Http\Controllers\Controller;
use App\Models\AuditLog;
use App\Models\Food;
use App\Models\FoodAlias;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class AdminDominicanCurationController extends Controller
{
    public function queue(Request $request): JsonResponse
    {
        $status = $request->input('status', 'unverified'); // 'all', 'verified', 'unverified'

        $q = Food::where('country_code', 'DO')
            ->with(['brand', 'foodNutrients.nutrient', 'portions', 'aliases', 'barcodes']);

        if ($status === 'unverified') {
            $q->where('verified', false);
        } elseif ($status === 'verified') {
            $q->where('verified', true);
        }

        $foods = $q->orderBy('id', 'asc')->paginate(25);

        return response()->json([
            'status' => 'success',
            'data' => $foods,
        ]);
    }

    public function approve(Request $request, int $id): JsonResponse
    {
        $food = Food::where('country_code', 'DO')->findOrFail($id);

        $food->verified = true;
        $food->save();

        AuditLog::log(
            user: $request->user(),
            action: 'dominican_curation.approve',
            targetType: 'Food',
            targetId: $food->id
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Alimento dominicano verificado y aprobado para el catálogo general.',
            'data' => $food->fresh(['brand', 'foodNutrients.nutrient', 'aliases', 'portions']),
        ]);
    }

    public function addAlias(Request $request, int $id): JsonResponse
    {
        $validated = $request->validate([
            'alias' => ['required', 'string', 'max:150'],
        ]);

        $food = Food::where('country_code', 'DO')->findOrFail($id);

        $alias = FoodAlias::create([
            'food_id' => $food->id,
            'alias' => $validated['alias'],
            'normalized_alias' => Str::slug($validated['alias'], ' '),
            'language' => 'es',
        ]);

        AuditLog::log(
            user: $request->user(),
            action: 'dominican_curation.add_alias',
            targetType: 'FoodAlias',
            targetId: $alias->id,
            newValues: ['food_id' => $food->id, 'alias' => $validated['alias']]
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Alias criollo registrado.',
            'data' => $alias,
        ], 201);
    }
}
