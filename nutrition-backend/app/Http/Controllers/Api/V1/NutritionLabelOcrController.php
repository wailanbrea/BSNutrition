<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\Food;
use App\Models\FoodBarcode;
use App\Models\FoodBrand;
use App\Models\FoodCategory;
use App\Models\FoodNutrient;
use App\Models\FoodPortion;
use App\Models\Nutrient;
use App\Services\DiaryService;
use App\Services\NutritionLabelParserService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class NutritionLabelOcrController extends Controller
{
    public function __construct(
        private NutritionLabelParserService $parserService,
        private DiaryService $diaryService
    ) {}

    /**
     * Parse raw OCR text into structured nutrition label breakdown.
     */
    public function parseLabel(Request $request): JsonResponse
    {
        $request->validate([
            'raw_text' => ['required', 'string', 'min:5'],
        ]);

        $parsed = $this->parserService->parseRawText($request->input('raw_text'));

        return response()->json([
            'status' => 'success',
            'data' => $parsed,
        ]);
    }

    /**
     * Create a permanent canonical food product from parsed/verified label data.
     */
    public function createFromLabel(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'canonical_name' => ['required', 'string', 'max:255'],
            'brand_name' => ['nullable', 'string', 'max:255'],
            'barcode' => ['nullable', 'string', 'max:64'],
            'category_id' => ['nullable', 'integer', 'exists:food_categories,id'],
            'country_code' => ['nullable', 'string', 'max:10'],
            'serving_name' => ['nullable', 'string', 'max:100'],
            'serving_grams' => ['nullable', 'numeric', 'min:1'],
            'calories_100g' => ['required', 'integer', 'min:0'],
            'protein_100g' => ['required', 'numeric', 'min:0'],
            'carbs_100g' => ['required', 'numeric', 'min:0'],
            'fat_100g' => ['required', 'numeric', 'min:0'],
            'saturated_fat_100g' => ['nullable', 'numeric', 'min:0'],
            'trans_fat_100g' => ['nullable', 'numeric', 'min:0'],
            'fiber_100g' => ['nullable', 'numeric', 'min:0'],
            'sodium_100g' => ['nullable', 'numeric', 'min:0'],
            'sugars_100g' => ['nullable', 'numeric', 'min:0'],
            'log_to_diary' => ['nullable', 'boolean'],
            'diary_date' => ['nullable', 'required_if:log_to_diary,true', 'date_format:Y-m-d'],
            'diary_meal_type' => ['nullable', 'required_if:log_to_diary,true', 'string', 'in:breakfast,lunch,dinner,snack'],
        ]);

        $user = $request->user();

        $food = DB::transaction(function () use ($validated, $user) {
            // 1. Resolve Brand
            $brandId = null;
            if (! empty($validated['brand_name'])) {
                $brand = FoodBrand::firstOrCreate(
                    ['normalized_name' => Str::slug($validated['brand_name'])],
                    ['name' => trim($validated['brand_name'])]
                );
                $brandId = $brand->id;
            }

            // 2. Default category fallback
            $categoryId = $validated['category_id'] ?? FoodCategory::first()?->id;

            // 3. Create Food
            $food = Food::create([
                'canonical_name' => trim($validated['canonical_name']),
                'normalized_name' => Str::slug($validated['canonical_name']),
                'brand_id' => $brandId,
                'category_id' => $categoryId,
                'user_id' => $user->id,
                'country_code' => $validated['country_code'] ?? 'DO',
                'language' => 'es',
                'verified' => false,
                'source' => 'ocr_label',
                'default_basis_amount' => 100.0,
                'default_basis_unit' => 'g',
            ]);

            // 4. Attach Barcode if present
            if (! empty($validated['barcode'])) {
                FoodBarcode::create([
                    'food_id' => $food->id,
                    'barcode' => trim($validated['barcode']),
                    'barcode_type' => strlen(trim($validated['barcode'])) <= 8 ? 'EAN-8' : 'EAN-13',
                ]);
            }

            // 5. Create Serving Portion
            $servingGrams = (float) ($validated['serving_grams'] ?? 100.0);
            $servingName = $validated['serving_name'] ?: '1 porción';
            FoodPortion::create([
                'food_id' => $food->id,
                'portion_name' => $servingName,
                'gram_weight' => $servingGrams,
                'is_default' => true,
            ]);

            // 6. Associate Nutrients
            $nutrientsMap = [
                'ENERGY_KCAL' => $validated['calories_100g'],
                'PROTEIN_G' => $validated['protein_100g'],
                'CARBS_G' => $validated['carbs_100g'],
                'FAT_G' => $validated['fat_100g'],
                'SAT_FAT_G' => $validated['saturated_fat_100g'] ?? null,
                'TRANS_FAT_G' => $validated['trans_fat_100g'] ?? null,
                'FIBER_G' => $validated['fiber_100g'] ?? null,
                'SODIUM_MG' => $validated['sodium_100g'] ?? null,
                'SUGAR_G' => $validated['sugars_100g'] ?? null,
            ];

            foreach ($nutrientsMap as $code => $val) {
                if ($val !== null) {
                    $nutrient = Nutrient::where('code', $code)->first();
                    if ($nutrient) {
                        FoodNutrient::create([
                            'food_id' => $food->id,
                            'nutrient_id' => $nutrient->id,
                            'amount' => (float) $val,
                            'basis_amount' => 100.0,
                            'basis_unit' => 'g',
                            'source' => 'ocr_label',
                        ]);
                    }
                }
            }

            return $food->load(['brand', 'category', 'portions', 'foodNutrients.nutrient', 'barcodes']);
        });

        $loggedEntry = null;
        if (! empty($validated['log_to_diary']) && ! empty($validated['diary_date'])) {
            $loggedEntry = $this->diaryService->addEntry(
                user: $user,
                date: $validated['diary_date'],
                data: [
                    'meal_type' => $validated['diary_meal_type'] ?? 'snack',
                    'food_id' => $food->id,
                    'portion_id' => $food->portions->first()?->id,
                    'quantity' => 1.0,
                    'unit' => 'porción',
                    'source' => 'ocr_label',
                ]
            );
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Alimento creado exitosamente a partir de la etiqueta nutricional.',
            'data' => [
                'food' => $food,
                'logged_entry' => $loggedEntry,
            ],
        ], 201);
    }
}
