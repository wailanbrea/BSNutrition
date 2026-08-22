<?php

namespace App\Http\Controllers\Api\V1\Admin;

use App\Http\Controllers\Controller;
use App\Models\AuditLog;
use App\Models\Food;
use App\Models\FoodAlias;
use App\Models\FoodBarcode;
use App\Models\FoodNutrient;
use App\Models\FoodPortion;
use App\Models\Nutrient;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class AdminFoodController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $request->validate([
            'query' => ['nullable', 'string', 'max:100'],
            'source' => ['nullable', 'string'],
            'country_code' => ['nullable', 'string', 'size:2'],
            'verified' => ['nullable', 'boolean'],
            'per_page' => ['nullable', 'integer', 'min:1', 'max:100'],
        ]);

        $q = Food::with(['brand', 'foodNutrients.nutrient', 'portions', 'aliases', 'barcodes']);

        if ($request->filled('query')) {
            $term = $request->input('query');
            $q->where(function ($query) use ($term) {
                $query->where('canonical_name', 'LIKE', "%{$term}%")
                    ->orWhere('normalized_name', 'LIKE', "%{$term}%");
            });
        }

        if ($request->filled('source')) {
            $q->where('source', $request->input('source'));
        }

        if ($request->filled('country_code')) {
            $q->where('country_code', strtoupper($request->input('country_code')));
        }

        if ($request->has('verified')) {
            $q->where('verified', $request->boolean('verified'));
        }

        $perPage = $request->input('per_page', 20);
        $foods = $q->orderBy('id', 'desc')->paginate($perPage);

        return response()->json([
            'status' => 'success',
            'data' => $foods,
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'canonical_name' => ['required', 'string', 'max:255'],
            'category_id' => ['nullable', 'integer', 'exists:food_categories,id'],
            'brand_id' => ['nullable', 'integer', 'exists:food_brands,id'],
            'country_code' => ['required', 'string', 'size:2'],
            'language' => ['nullable', 'string', 'max:10'],
            'source' => ['required', 'string'],
            'verified' => ['nullable', 'boolean'],
            'default_basis_amount' => ['nullable', 'numeric', 'min:0.1'],
            'default_basis_unit' => ['nullable', 'string', 'max:20'],
            'nutrients' => ['nullable', 'array'],
            'nutrients.*.code' => ['required', 'string', 'exists:nutrients,code'],
            'nutrients.*.amount' => ['required', 'numeric', 'min:0'],
            'aliases' => ['nullable', 'array'],
            'barcodes' => ['nullable', 'array'],
        ]);

        $food = DB::transaction(function () use ($validated, $request) {
            $food = Food::create([
                'canonical_name' => $validated['canonical_name'],
                'normalized_name' => Str::slug($validated['canonical_name'], ' '),
                'category_id' => $validated['category_id'] ?? null,
                'brand_id' => $validated['brand_id'] ?? null,
                'country_code' => strtoupper($validated['country_code']),
                'language' => $validated['language'] ?? 'es',
                'source' => $validated['source'],
                'verified' => $validated['verified'] ?? true,
                'default_basis_amount' => $validated['default_basis_amount'] ?? 100.0,
                'default_basis_unit' => $validated['default_basis_unit'] ?? 'g',
            ]);

            if (! empty($validated['nutrients'])) {
                foreach ($validated['nutrients'] as $item) {
                    $nutrient = Nutrient::where('code', $item['code'])->first();
                    if ($nutrient) {
                        FoodNutrient::create([
                            'food_id' => $food->id,
                            'nutrient_id' => $nutrient->id,
                            'amount' => $item['amount'],
                            'basis_amount' => 100.0,
                            'basis_unit' => 'g',
                            'source' => 'admin_curation',
                        ]);
                    }
                }
            }

            if (! empty($validated['aliases'])) {
                foreach ($validated['aliases'] as $alias) {
                    FoodAlias::create([
                        'food_id' => $food->id,
                        'alias' => $alias,
                        'normalized_alias' => Str::slug($alias, ' '),
                        'language' => 'es',
                    ]);
                }
            }

            if (! empty($validated['barcodes'])) {
                foreach ($validated['barcodes'] as $barcode) {
                    FoodBarcode::create([
                        'food_id' => $food->id,
                        'barcode' => $barcode,
                        'barcode_type' => 'EAN_13',
                        'is_primary' => true,
                    ]);
                }
            }

            AuditLog::log(
                user: $request->user(),
                action: 'food.create',
                targetType: 'Food',
                targetId: $food->id,
                newValues: $food->toArray()
            );

            return $food->load(['brand', 'foodNutrients.nutrient', 'portions', 'aliases', 'barcodes']);
        });

        return response()->json([
            'status' => 'success',
            'message' => 'Alimento creado y curado exitosamente.',
            'data' => $food,
        ], 201);
    }

    public function show(int $id): JsonResponse
    {
        $food = Food::with(['brand', 'foodNutrients.nutrient', 'portions', 'aliases', 'barcodes'])
            ->findOrFail($id);

        return response()->json([
            'status' => 'success',
            'data' => $food,
        ]);
    }

    public function update(Request $request, int $id): JsonResponse
    {
        $food = Food::findOrFail($id);
        $old = $food->toArray();

        $validated = $request->validate([
            'canonical_name' => ['sometimes', 'string', 'max:255'],
            'country_code' => ['sometimes', 'string', 'size:2'],
            'verified' => ['sometimes', 'boolean'],
            'default_basis_amount' => ['sometimes', 'numeric', 'min:0.1'],
            'default_basis_unit' => ['sometimes', 'string', 'max:20'],
        ]);

        if (isset($validated['canonical_name'])) {
            $validated['normalized_name'] = Str::slug($validated['canonical_name'], ' ');
        }

        $food->update($validated);

        AuditLog::log(
            user: $request->user(),
            action: 'food.update',
            targetType: 'Food',
            targetId: $food->id,
            oldValues: $old,
            newValues: $food->toArray()
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Alimento actualizado.',
            'data' => $food->fresh(['brand', 'foodNutrients.nutrient', 'portions', 'aliases', 'barcodes']),
        ]);
    }

    public function destroy(Request $request, int $id): JsonResponse
    {
        $food = Food::findOrFail($id);
        $old = $food->toArray();

        $food->delete();

        AuditLog::log(
            user: $request->user(),
            action: 'food.delete',
            targetType: 'Food',
            targetId: $id,
            oldValues: $old
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Alimento eliminado.',
        ]);
    }

    public function verify(Request $request, int $id): JsonResponse
    {
        $food = Food::findOrFail($id);
        $food->verified = true;
        $food->save();

        AuditLog::log(
            user: $request->user(),
            action: 'food.verify',
            targetType: 'Food',
            targetId: $food->id
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Alimento verificado y promovido al catálogo oficial.',
            'data' => $food,
        ]);
    }
}
