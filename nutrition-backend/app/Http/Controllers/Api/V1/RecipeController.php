<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\Recipe;
use App\Services\RecipeCalculationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class RecipeController extends Controller
{
    public function __construct(
        private readonly RecipeCalculationService $calculationService
    ) {}

    public function index(Request $request): JsonResponse
    {
        $request->validate([
            'query' => ['nullable', 'string', 'max:100'],
            'per_page' => ['nullable', 'integer', 'min:1', 'max:50'],
        ]);

        $userId = $request->user()->id;
        $q = Recipe::where(function ($query) use ($userId) {
            $query->where('user_id', $userId)->orWhere('is_public', true);
        })->with(['ingredients.food.brand', 'steps']);

        if ($request->filled('query')) {
            $term = $request->input('query');
            $q->where('name', 'LIKE', "%{$term}%");
        }

        $perPage = $request->input('per_page', 15);
        $recipes = $q->orderBy('created_at', 'desc')->paginate($perPage);

        return response()->json([
            'status' => 'success',
            'data' => $recipes,
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:200'],
            'description' => ['nullable', 'string', 'max:1000'],
            'servings' => ['required', 'integer', 'min:1', 'max:100'],
            'prep_time_minutes' => ['nullable', 'integer', 'min:0'],
            'cook_time_minutes' => ['nullable', 'integer', 'min:0'],
            'is_public' => ['nullable', 'boolean'],
            'ingredients' => ['required', 'array', 'min:1'],
            'ingredients.*.custom_name' => ['required', 'string'],
            'ingredients.*.food_id' => ['nullable', 'integer', 'exists:foods,id'],
            'ingredients.*.portion_id' => ['nullable', 'integer', 'exists:food_portions,id'],
            'ingredients.*.quantity' => ['nullable', 'numeric', 'min:0.1'],
            'ingredients.*.unit' => ['nullable', 'string', 'max:50'],
            'ingredients.*.grams' => ['required', 'numeric', 'min:0.1'],
            'ingredients.*.calories' => ['nullable', 'integer', 'min:0'],
            'ingredients.*.protein_g' => ['nullable', 'numeric', 'min:0'],
            'ingredients.*.carbs_g' => ['nullable', 'numeric', 'min:0'],
            'ingredients.*.fat_g' => ['nullable', 'numeric', 'min:0'],
            'steps' => ['nullable', 'array'],
        ]);

        $recipe = $this->calculationService->saveRecipe($request->user(), $validated);

        return response()->json([
            'status' => 'success',
            'message' => 'Receta creada exitosamente.',
            'data' => $recipe,
        ], 201);
    }

    public function show(Request $request, int $id): JsonResponse
    {
        $userId = $request->user()->id;
        $recipe = Recipe::where(function ($query) use ($userId) {
            $query->where('user_id', $userId)->orWhere('is_public', true);
        })->with(['ingredients.food.brand', 'ingredients.portion', 'steps'])
            ->findOrFail($id);

        return response()->json([
            'status' => 'success',
            'data' => $recipe,
        ]);
    }

    public function update(Request $request, int $id): JsonResponse
    {
        $recipe = Recipe::where('user_id', $request->user()->id)
            ->findOrFail($id);

        $validated = $request->validate([
            'name' => ['required', 'string', 'max:200'],
            'description' => ['nullable', 'string', 'max:1000'],
            'servings' => ['required', 'integer', 'min:1', 'max:100'],
            'prep_time_minutes' => ['nullable', 'integer', 'min:0'],
            'cook_time_minutes' => ['nullable', 'integer', 'min:0'],
            'is_public' => ['nullable', 'boolean'],
            'ingredients' => ['required', 'array', 'min:1'],
            'ingredients.*.custom_name' => ['required', 'string'],
            'ingredients.*.food_id' => ['nullable', 'integer', 'exists:foods,id'],
            'ingredients.*.portion_id' => ['nullable', 'integer', 'exists:food_portions,id'],
            'ingredients.*.quantity' => ['nullable', 'numeric', 'min:0.1'],
            'ingredients.*.unit' => ['nullable', 'string', 'max:50'],
            'ingredients.*.grams' => ['required', 'numeric', 'min:0.1'],
            'ingredients.*.calories' => ['nullable', 'integer', 'min:0'],
            'ingredients.*.protein_g' => ['nullable', 'numeric', 'min:0'],
            'ingredients.*.carbs_g' => ['nullable', 'numeric', 'min:0'],
            'ingredients.*.fat_g' => ['nullable', 'numeric', 'min:0'],
            'steps' => ['nullable', 'array'],
        ]);

        $updated = $this->calculationService->saveRecipe($request->user(), $validated, $recipe);

        return response()->json([
            'status' => 'success',
            'message' => 'Receta actualizada exitosamente.',
            'data' => $updated,
        ]);
    }

    public function destroy(Request $request, int $id): JsonResponse
    {
        $recipe = Recipe::where('user_id', $request->user()->id)
            ->findOrFail($id);

        $recipe->delete();

        return response()->json([
            'status' => 'success',
            'message' => 'Receta eliminada.',
        ]);
    }

    public function logToDiary(Request $request, int $id): JsonResponse
    {
        $validated = $request->validate([
            'date' => ['required', 'date_format:Y-m-d'],
            'meal_type' => ['required', 'string', 'in:breakfast,lunch,dinner,snack_1,snack_2,snack_3'],
            'servings' => ['nullable', 'numeric', 'min:0.1', 'max:20'],
            'client_id' => ['nullable', 'string', 'max:64'],
        ]);

        $userId = $request->user()->id;
        $recipe = Recipe::where(function ($query) use ($userId) {
            $query->where('user_id', $userId)->orWhere('is_public', true);
        })->findOrFail($id);

        $entry = $this->calculationService->logServingToDiary(
            user: $request->user(),
            recipe: $recipe,
            date: $validated['date'],
            mealType: $validated['meal_type'],
            servings: (float) ($validated['servings'] ?? 1.0),
            clientId: $validated['client_id'] ?? null
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Porción de receta agregada a tu diario.',
            'data' => $entry,
        ], 201);
    }
}
