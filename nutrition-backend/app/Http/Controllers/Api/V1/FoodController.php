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
}
