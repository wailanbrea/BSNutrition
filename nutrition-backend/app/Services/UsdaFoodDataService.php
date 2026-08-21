<?php

namespace App\Services;

use App\Models\Food;
use App\Models\FoodNutrient;
use App\Models\FoodPortion;
use App\Models\Nutrient;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class UsdaFoodDataService
{
    /**
     * Mapping from USDA Nutrient IDs (and nutrient numbers) to canonical BSNutrition nutrient codes.
     */
    public const NUTRIENT_MAPPING = [
        1008 => 'calories',            // Energy (kcal)
        208 => 'calories',             // Energy (kcal) legacy
        1003 => 'protein',             // Protein
        203 => 'protein',              // Protein legacy
        1005 => 'carbohydrate',        // Carbohydrate, by difference
        205 => 'carbohydrate',         // Carbohydrate legacy
        1004 => 'total_fat',           // Total lipid (fat)
        204 => 'total_fat',            // Total lipid legacy
        1079 => 'fiber',               // Fiber, total dietary
        291 => 'fiber',                // Fiber legacy
        2000 => 'sugar',               // Total Sugars
        269 => 'sugar',                // Total Sugars legacy
        1235 => 'added_sugars',        // Added Sugars
        1258 => 'saturated_fat',       // Fatty acids, total saturated
        606 => 'saturated_fat',        // Fatty acids, total saturated legacy
        1257 => 'trans_fat',           // Fatty acids, total trans
        605 => 'trans_fat',            // Fatty acids, total trans legacy
        1292 => 'monounsaturated_fat', // Fatty acids, total monounsaturated
        645 => 'monounsaturated_fat',  // Fatty acids, total monounsaturated legacy
        1293 => 'polyunsaturated_fat', // Fatty acids, total polyunsaturated
        646 => 'polyunsaturated_fat',  // Fatty acids, total polyunsaturated legacy
        1253 => 'cholesterol',         // Cholesterol
        601 => 'cholesterol',          // Cholesterol legacy
        1093 => 'sodium',              // Sodium, Na
        307 => 'sodium',               // Sodium legacy
        1092 => 'potassium',           // Potassium, K
        306 => 'potassium',            // Potassium legacy
        1087 => 'calcium',             // Calcium, Ca
        301 => 'calcium',              // Calcium legacy
        1089 => 'iron',                // Iron, Fe
        303 => 'iron',                 // Iron legacy
        1090 => 'magnesium',           // Magnesium, Mg
        304 => 'magnesium',            // Magnesium legacy
        1095 => 'zinc',                // Zinc, Zn
        309 => 'zinc',                 // Zinc legacy
        1091 => 'phosphorus',          // Phosphorus, P
        305 => 'phosphorus',           // Phosphorus legacy
        1106 => 'vitamin_a',           // Vitamin A, RAE
        320 => 'vitamin_a',            // Vitamin A legacy
        1162 => 'vitamin_c',           // Vitamin C, total ascorbic acid
        401 => 'vitamin_c',            // Vitamin C legacy
        1114 => 'vitamin_d',           // Vitamin D (D2 + D3)
        328 => 'vitamin_d',            // Vitamin D legacy
        1109 => 'vitamin_e',           // Vitamin E (alpha-tocopherol)
        323 => 'vitamin_e',            // Vitamin E legacy
        1185 => 'vitamin_k',           // Vitamin K (phylloquinone)
        430 => 'vitamin_k',            // Vitamin K legacy
        1165 => 'thiamin_b1',          // Thiamin
        404 => 'thiamin_b1',           // Thiamin legacy
        1166 => 'riboflavin_b2',       // Riboflavin
        405 => 'riboflavin_b2',        // Riboflavin legacy
        1167 => 'niacin_b3',           // Niacin
        406 => 'niacin_b3',            // Niacin legacy
        1175 => 'vitamin_b6',          // Vitamin B-6
        415 => 'vitamin_b6',           // Vitamin B-6 legacy
        1177 => 'folate_b9',           // Folate, total
        417 => 'folate_b9',            // Folate legacy
        1178 => 'vitamin_b12',         // Vitamin B-12
        418 => 'vitamin_b12',          // Vitamin B-12 legacy
        1051 => 'water',               // Water
        255 => 'water',                // Water legacy
        1057 => 'caffeine',            // Caffeine
        262 => 'caffeine',             // Caffeine legacy
        1018 => 'alcohol',             // Alcohol, ethyl
        221 => 'alcohol',              // Alcohol legacy
    ];

    protected string $apiKey;

    protected string $baseUrl;

    public function __construct()
    {
        $this->apiKey = (string) config('services.usda.api_key', 'DEMO_KEY');
        $this->baseUrl = rtrim((string) config('services.usda.base_url', 'https://api.nal.usda.gov/fdc/v1'), '/');
    }

    /**
     * Search foods in USDA FoodData Central.
     *
     * @param  array<string>  $dataType
     * @return array<string, mixed>
     */
    public function search(
        string $query,
        int $pageSize = 25,
        int $pageNumber = 1,
        array $dataType = ['Foundation', 'SR Legacy', 'Survey (FNDDS)']
    ): array {
        $cacheKey = 'usda_search_'.md5("{$query}_{$pageSize}_{$pageNumber}_".implode('_', $dataType));

        return Cache::remember($cacheKey, 3600, function () use ($query, $pageSize, $pageNumber, $dataType) {
            try {
                $response = Http::timeout(10)->get("{$this->baseUrl}/foods/search", [
                    'api_key' => $this->apiKey,
                    'query' => $query,
                    'pageSize' => $pageSize,
                    'pageNumber' => $pageNumber,
                    'dataType' => $dataType,
                ]);

                if ($response->successful()) {
                    return $response->json() ?? [];
                }

                Log::warning('USDA Search API error', [
                    'status' => $response->status(),
                    'body' => $response->body(),
                ]);

                return [];
            } catch (\Exception $e) {
                Log::error('USDA Search HTTP Exception', ['error' => $e->getMessage()]);

                return [];
            }
        });
    }

    /**
     * Get full food details by FDC ID.
     *
     * @return array<string, mixed>|null
     */
    public function getFoodDetails(int|string $fdcId): ?array
    {
        $cacheKey = "usda_food_details_{$fdcId}";

        return Cache::remember($cacheKey, 86400, function () use ($fdcId) {
            try {
                $response = Http::timeout(10)->get("{$this->baseUrl}/food/{$fdcId}", [
                    'api_key' => $this->apiKey,
                ]);

                if ($response->successful()) {
                    return $response->json();
                }

                Log::warning("USDA Food Details error for FDC {$fdcId}", [
                    'status' => $response->status(),
                ]);

                return null;
            } catch (\Exception $e) {
                Log::error("USDA Food Details Exception for FDC {$fdcId}", ['error' => $e->getMessage()]);

                return null;
            }
        });
    }

    /**
     * Import a USDA Food into local database, creating Food, Portions and FoodNutrients.
     */
    public function importFood(int|string $fdcId): ?Food
    {
        $data = $this->getFoodDetails($fdcId);
        if (! $data) {
            return null;
        }

        $description = $data['description'] ?? "USDA Food {$fdcId}";
        $normalizedName = mb_strtolower(trim($description), 'UTF-8');

        // 1. Create or update Food
        $food = Food::updateOrCreate(
            [
                'source' => 'usda_fdc',
                'external_source_id' => (string) $fdcId,
            ],
            [
                'canonical_name' => $description,
                'normalized_name' => $normalizedName,
                'language' => 'en',
                'country_code' => 'US',
                'verified' => true,
                'default_basis_amount' => 100.00,
                'default_basis_unit' => 'g',
            ]
        );

        // 2. Map & Sync Nutrients
        $rawNutrients = $data['foodNutrients'] ?? [];
        $canonicalNutrients = Nutrient::all()->keyBy('code');

        foreach ($rawNutrients as $fn) {
            $nutrientInfo = $fn['nutrient'] ?? $fn;
            $nutrientId = $nutrientInfo['id'] ?? $nutrientInfo['nutrientId'] ?? null;
            $nutrientNumber = (int) ($nutrientInfo['number'] ?? $nutrientInfo['nutrientNumber'] ?? 0);
            $amount = (float) ($fn['amount'] ?? $fn['value'] ?? 0);

            $code = self::NUTRIENT_MAPPING[$nutrientId] ?? self::NUTRIENT_MAPPING[$nutrientNumber] ?? null;

            if ($code && isset($canonicalNutrients[$code])) {
                $canonical = $canonicalNutrients[$code];

                FoodNutrient::updateOrCreate(
                    [
                        'food_id' => $food->id,
                        'nutrient_id' => $canonical->id,
                    ],
                    [
                        'amount' => $amount,
                        'basis_amount' => 100.00,
                        'basis_unit' => 'g',
                        'source' => 'usda_fdc',
                    ]
                );
            }
        }

        // 3. Portions
        $rawPortions = $data['foodPortions'] ?? [];
        foreach ($rawPortions as $portion) {
            $gramWeight = (float) ($portion['gramWeight'] ?? 0);
            if ($gramWeight <= 0) {
                continue;
            }

            $portionDescription = $portion['portionDescription'] ?? $portion['modifier'] ?? '1 porción';
            $amount = (float) ($portion['amount'] ?? 1.0);
            $unitName = $portion['measureUnit']['name'] ?? 'unidad';

            FoodPortion::updateOrCreate(
                [
                    'food_id' => $food->id,
                    'portion_name' => $portionDescription,
                ],
                [
                    'gram_weight' => $gramWeight,
                    'amount' => $amount,
                    'unit' => $unitName,
                    'is_default' => false,
                ]
            );
        }

        return $food->load(['nutrients', 'portions']);
    }
}
