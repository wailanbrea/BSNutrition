<?php

namespace App\Services;

use App\Models\Food;
use App\Models\FoodBarcode;
use App\Models\FoodBrand;
use App\Models\FoodNutrient;
use App\Models\FoodPortion;
use App\Models\Nutrient;
use Illuminate\Support\Collection;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class OpenFoodFactsService
{
    protected string $baseUrl;

    protected string $userAgent;

    public function __construct()
    {
        $this->baseUrl = rtrim((string) config('services.openfoodfacts.base_url', 'https://world.openfoodfacts.org/api/v2'), '/');
        $this->userAgent = (string) config('services.openfoodfacts.user_agent', 'BSNutrition - Android - Version 1.0');
    }

    /**
     * Look up a product by barcode in Open Food Facts.
     */
    public function getByBarcode(string $barcode): ?Food
    {
        $cleanBarcode = trim($barcode);

        // 1. Check local DB first
        $localFood = Food::byBarcode($cleanBarcode)->with(['nutrients', 'portions', 'barcodes', 'brand'])->first();
        if ($localFood) {
            return $localFood;
        }

        // 2. Query Open Food Facts API
        $cacheKey = "off_barcode_{$cleanBarcode}";

        $productData = Cache::remember($cacheKey, 86400, function () use ($cleanBarcode) {
            try {
                $response = Http::withHeaders([
                    'User-Agent' => $this->userAgent,
                ])->timeout(10)->get("{$this->baseUrl}/product/{$cleanBarcode}.json");

                if ($response->successful()) {
                    $json = $response->json();
                    if (($json['status'] ?? 0) === 1 && isset($json['product'])) {
                        return $json['product'];
                    }
                }

                return null;
            } catch (\Exception $e) {
                Log::error("Open Food Facts lookup error for barcode {$cleanBarcode}", ['error' => $e->getMessage()]);

                return null;
            }
        });

        if (! $productData) {
            return null;
        }

        return $this->importProduct($productData, $cleanBarcode);
    }

    /**
     * Search products in Open Food Facts by keyword.
     *
     * @return array<string, mixed>
     */
    public function search(string $query, int $pageSize = 20, int $page = 1): array
    {
        $cacheKey = 'off_search_'.md5("{$query}_{$pageSize}_{$page}");

        return Cache::remember($cacheKey, 3600, function () use ($query, $pageSize, $page) {
            try {
                $response = Http::withHeaders([
                    'User-Agent' => $this->userAgent,
                ])->timeout(10)->get("{$this->baseUrl}/search", [
                    'search_terms' => $query,
                    'page_size' => $pageSize,
                    'page' => $page,
                    'json' => 1,
                ]);

                if ($response->successful()) {
                    return $response->json() ?? [];
                }

                return [];
            } catch (\Exception $e) {
                Log::error('Open Food Facts search error', ['error' => $e->getMessage()]);

                return [];
            }
        });
    }

    /**
     * Import raw Open Food Facts product structure into local Food model.
     *
     * @param  array<string, mixed>  $product
     */
    public function importProduct(array $product, ?string $barcode = null): ?Food
    {
        $barcode = $barcode ?: ($product['code'] ?? null);
        $name = $product['product_name_es']
            ?? $product['product_name']
            ?? $product['product_name_en']
            ?? "Producto {$barcode}";

        $normalizedName = mb_strtolower(trim($name), 'UTF-8');

        // Brand resolution
        $brandId = null;
        if (! empty($product['brands'])) {
            $brandName = trim(explode(',', $product['brands'])[0]);
            if (! empty($brandName)) {
                $brand = FoodBrand::firstOrCreate(
                    ['normalized_name' => mb_strtolower($brandName, 'UTF-8')],
                    ['name' => $brandName]
                );
                $brandId = $brand->id;
            }
        }

        // Create or update Food
        $food = Food::updateOrCreate(
            [
                'source' => 'openfoodfacts',
                'external_source_id' => (string) ($barcode ?: ($product['id'] ?? uniqid())),
            ],
            [
                'canonical_name' => $name,
                'normalized_name' => $normalizedName,
                'brand_id' => $brandId,
                'country_code' => isset($product['countries_tags'][0]) ? substr(str_replace('en:', '', $product['countries_tags'][0]), 0, 8) : null,
                'language' => isset($product['lang']) ? substr($product['lang'], 0, 8) : 'es',
                'verified' => true,
                'default_basis_amount' => 100.00,
                'default_basis_unit' => 'g',
            ]
        );

        // Attach Barcode
        if ($barcode) {
            FoodBarcode::updateOrCreate(
                ['barcode' => $barcode],
                [
                    'food_id' => $food->id,
                    'barcode_type' => strlen($barcode) === 13 ? 'EAN_13' : 'UPC_A',
                    'is_primary' => true,
                ]
            );
        }

        // Map Nutrients
        $nutriments = $product['nutriments'] ?? [];
        $canonicalNutrients = Nutrient::all()->keyBy('code');

        $this->mapAndSaveNutrients($food, $nutriments, $canonicalNutrients);

        // Portion / Serving size
        $servingSize = $product['serving_size'] ?? null;
        $servingQuantity = (float) ($product['serving_quantity'] ?? 0);

        if ($servingQuantity > 0 || ! empty($servingSize)) {
            $gramWeight = $servingQuantity > 0 ? $servingQuantity : 100.0;
            if ($gramWeight <= 0 && preg_match('/([\d\.]+)\s*g/i', (string) $servingSize, $matches)) {
                $gramWeight = (float) $matches[1];
            }

            FoodPortion::updateOrCreate(
                [
                    'food_id' => $food->id,
                    'portion_name' => $servingSize ?: "Porción ({$gramWeight}g)",
                ],
                [
                    'gram_weight' => $gramWeight > 0 ? $gramWeight : 100.0,
                    'amount' => 1.0,
                    'unit' => 'porción',
                    'is_default' => true,
                ]
            );
        }

        return $food->load(['nutrients', 'portions', 'barcodes', 'brand']);
    }

    /**
     * Map Open Food Facts nutriments structure to canonical food nutrients.
     *
     * @param  array<string, mixed>  $nutriments
     * @param  Collection<string, Nutrient>  $canonicalNutrients
     */
    protected function mapAndSaveNutrients(Food $food, array $nutriments, $canonicalNutrients): void
    {
        $mapping = [
            'calories' => $nutriments['energy-kcal_100g'] ?? $nutriments['energy-kcal'] ?? (isset($nutriments['energy_100g']) ? ($nutriments['energy_100g'] / 4.184) : null),
            'protein' => $nutriments['proteins_100g'] ?? $nutriments['proteins'] ?? null,
            'carbohydrate' => $nutriments['carbohydrates_100g'] ?? $nutriments['carbohydrates'] ?? null,
            'total_fat' => $nutriments['fat_100g'] ?? $nutriments['fat'] ?? null,
            'saturated_fat' => $nutriments['saturated-fat_100g'] ?? $nutriments['saturated-fat'] ?? null,
            'trans_fat' => $nutriments['trans-fat_100g'] ?? $nutriments['trans-fat'] ?? null,
            'fiber' => $nutriments['fiber_100g'] ?? $nutriments['fiber'] ?? null,
            'sugar' => $nutriments['sugars_100g'] ?? $nutriments['sugars'] ?? null,
            'added_sugars' => $nutriments['added-sugars_100g'] ?? null,
            'sodium' => isset($nutriments['sodium_100g']) ? ($nutriments['sodium_100g'] * 1000) : (isset($nutriments['salt_100g']) ? (($nutriments['salt_100g'] / 2.5) * 1000) : null),
            'potassium' => isset($nutriments['potassium_100g']) ? ($nutriments['potassium_100g'] < 10 ? $nutriments['potassium_100g'] * 1000 : $nutriments['potassium_100g']) : null,
            'calcium' => isset($nutriments['calcium_100g']) ? ($nutriments['calcium_100g'] < 10 ? $nutriments['calcium_100g'] * 1000 : $nutriments['calcium_100g']) : null,
            'iron' => isset($nutriments['iron_100g']) ? ($nutriments['iron_100g'] < 1 ? $nutriments['iron_100g'] * 1000 : $nutriments['iron_100g']) : null,
            'cholesterol' => $nutriments['cholesterol_100g'] ?? null,
            'vitamin_a' => $nutriments['vitamin-a_100g'] ?? null,
            'vitamin_c' => $nutriments['vitamin-c_100g'] ?? null,
            'vitamin_d' => $nutriments['vitamin-d_100g'] ?? null,
            'vitamin_b12' => $nutriments['vitamin-b12_100g'] ?? null,
        ];

        foreach ($mapping as $code => $value) {
            if ($value !== null && isset($canonicalNutrients[$code])) {
                FoodNutrient::updateOrCreate(
                    [
                        'food_id' => $food->id,
                        'nutrient_id' => $canonicalNutrients[$code]->id,
                    ],
                    [
                        'amount' => (float) $value,
                        'basis_amount' => 100.00,
                        'basis_unit' => 'g',
                        'source' => 'openfoodfacts',
                    ]
                );
            }
        }
    }
}
