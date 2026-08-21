<?php

namespace Database\Seeders;

use App\Models\FoodSource;
use Illuminate\Database\Seeder;

class FoodSourceSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $sources = [
            [
                'code' => 'usda_fdc',
                'name' => 'USDA FoodData Central',
                'url' => 'https://fdc.nal.usda.gov',
                'version' => '2024_sr_legacy',
            ],
            [
                'code' => 'openfoodfacts',
                'name' => 'Open Food Facts World Database',
                'url' => 'https://world.openfoodfacts.org',
                'version' => 'v3_api',
            ],
            [
                'code' => 'generic',
                'name' => 'BSNutrition Verified Generic Foods',
                'url' => null,
                'version' => 'v1.0',
            ],
            [
                'code' => 'user_custom',
                'name' => 'Alimentos Creados por Usuario',
                'url' => null,
                'version' => 'v1.0',
            ],
        ];

        foreach ($sources as $source) {
            FoodSource::updateOrCreate(['code' => $source['code']], $source);
        }
    }
}
