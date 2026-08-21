<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Resources\FoodDetailResource;
use App\Http\Resources\FoodSummaryResource;
use App\Models\Food;
use App\Models\FoodPortion;
use App\Services\NutritionCalculatorService;
use App\Services\OpenFoodFactsService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;
use Illuminate\Support\Facades\DB;

class FoodController extends Controller
{
    /**
     * Search foods with locale boosting, category filtering and pagination.
     */
    public function search(Request $request): AnonymousResourceCollection
    {
        $query = $request->validate([
            'query' => 'nullable|string|max:100',
            'category_id' => 'nullable|integer|exists:food_categories,id',
            'country' => 'nullable|string|max:8',
            'per_page' => 'nullable|integer|min:1|max:100',
        ]);

        $searchTerm = trim((string) ($query['query'] ?? ''));
        $categoryId = $query['category_id'] ?? null;
        $country = $query['country'] ?? 'DO';
        $perPage = (int) ($query['per_page'] ?? 20);

        $foodsQuery = Food::query()
            ->with(['brand', 'category', 'portions', 'nutrients']);

        if ($searchTerm !== '') {
            $foodsQuery->search($searchTerm);
        }

        if ($categoryId) {
            $foodsQuery->where('category_id', $categoryId);
        }

        // Locale boosting: boost exact country match, then verified foods
        $foodsQuery->orderByRaw(
            'CASE WHEN country_code = ? THEN 0 WHEN verified = 1 THEN 1 ELSE 2 END, canonical_name ASC',
            [$country]
        );

        $results = $foodsQuery->paginate($perPage);

        return FoodSummaryResource::collection($results);
    }

    /**
     * Get detailed food information including portions, barcodes and all nutrients.
     */
    public function show(int $id): JsonResponse|FoodDetailResource
    {
        $food = Food::with(['brand', 'category', 'portions', 'nutrients', 'barcodes', 'aliases'])
            ->find($id);

        if (! $food) {
            return response()->json([
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => "No se encontró el alimento con ID {$id}.",
                ],
            ], 404);
        }

        return (new FoodDetailResource($food))->response()->setStatusCode(200);
    }

    /**
     * Find food by barcode (local DB first, fallback to Open Food Facts).
     */
    public function byBarcode(string $barcode, OpenFoodFactsService $offService): JsonResponse
    {
        $cleanBarcode = trim($barcode);

        $food = Food::byBarcode($cleanBarcode)
            ->with(['brand', 'category', 'portions', 'nutrients', 'barcodes', 'aliases'])
            ->first();

        if (! $food) {
            $food = $offService->getByBarcode($cleanBarcode);
        }

        if (! $food) {
            return response()->json([
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => "No se encontró ningún producto con el código de barras {$cleanBarcode}.",
                ],
            ], 404);
        }

        return (new FoodDetailResource($food))->response()->setStatusCode(200);
    }

    /**
     * Calculate nutrition values for a specific food, portion and quantity.
     */
    public function calculate(Request $request, int $id, NutritionCalculatorService $calculator): JsonResponse
    {
        $food = Food::with(['portions', 'foodNutrients.nutrient'])->findOrFail($id);

        $data = $request->validate([
            'quantity' => 'required|numeric|min:0.01',
            'portion_id' => 'nullable|integer|exists:food_portions,id',
            'unit' => 'nullable|string|max:32',
        ]);

        $portion = null;
        if (! empty($data['portion_id'])) {
            $portion = FoodPortion::where('food_id', $food->id)->find($data['portion_id']);
        }

        $unit = $data['unit'] ?? 'g';
        $calculation = $calculator->calculateForFood($food, (float) $data['quantity'], $portion, $unit);

        return response()->json([
            'data' => $calculation,
        ]);
    }

    /**
     * List authenticated user's favorite foods.
     */
    public function favorites(Request $request): AnonymousResourceCollection
    {
        $user = $request->user();

        $favorites = $user->favoriteFoods()
            ->with(['brand', 'category', 'foodNutrients.nutrient', 'portions'])
            ->latest('user_food_favorites.created_at')
            ->paginate((int) $request->input('per_page', 20));

        return FoodSummaryResource::collection($favorites);
    }

    /**
     * Toggle favorite status for a food.
     */
    public function toggleFavorite(Request $request, int $id): JsonResponse
    {
        $user = $request->user();
        $food = Food::findOrFail($id);

        $exists = $user->favoriteFoods()->where('food_id', $food->id)->exists();

        if ($exists) {
            $user->favoriteFoods()->detach($food->id);
            $isFavorite = false;
            $message = 'Alimento eliminado de favoritos.';
        } else {
            $user->favoriteFoods()->attach($food->id);
            $isFavorite = true;
            $message = 'Alimento agregado a favoritos.';
        }

        return response()->json([
            'is_favorite' => $isFavorite,
            'message' => $message,
            'food_id' => $food->id,
        ]);
    }

    /**
     * Check if a food is in the user's favorites.
     */
    public function isFavorite(Request $request, int $id): JsonResponse
    {
        $user = $request->user();
        $isFavorite = $user->favoriteFoods()->where('food_id', $id)->exists();

        return response()->json([
            'is_favorite' => $isFavorite,
            'food_id' => $id,
        ]);
    }

    /**
     * List authenticated user's recently logged or viewed foods.
     */
    public function recents(Request $request): AnonymousResourceCollection
    {
        $user = $request->user();

        $recents = $user->recentFoods()
            ->with(['brand', 'category', 'foodNutrients.nutrient', 'portions'])
            ->orderByDesc('user_food_recents.last_used_at')
            ->paginate((int) $request->input('per_page', 20));

        return FoodSummaryResource::collection($recents);
    }

    /**
     * Record a food consumption or view into recents history.
     */
    public function recordRecent(Request $request, int $id): JsonResponse
    {
        $user = $request->user();
        $food = Food::findOrFail($id);

        $existing = DB::table('user_food_recents')
            ->where('user_id', $user->id)
            ->where('food_id', $food->id)
            ->first();

        if ($existing) {
            DB::table('user_food_recents')
                ->where('id', $existing->id)
                ->update([
                    'use_count' => $existing->use_count + 1,
                    'last_used_at' => now(),
                    'updated_at' => now(),
                ]);
        } else {
            DB::table('user_food_recents')->insert([
                'user_id' => $user->id,
                'food_id' => $food->id,
                'use_count' => 1,
                'last_used_at' => now(),
                'created_at' => now(),
                'updated_at' => now(),
            ]);
        }

        return response()->json([
            'message' => 'Alimento registrado en recientes.',
            'food_id' => $food->id,
        ]);
    }
}
