<?php

namespace Database\Seeders;

use App\Models\Nutrient;
use Illuminate\Database\Seeder;

class NutrientSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $nutrients = [
            // Core Macros
            [
                'code' => 'calories',
                'name' => 'Calorías',
                'unit' => 'kcal',
                'description' => 'Energía total aportada por el alimento.',
                'is_macro' => true,
                'sort_order' => 1,
            ],
            [
                'code' => 'protein',
                'name' => 'Proteínas',
                'unit' => 'g',
                'description' => 'Proteínas totales.',
                'is_macro' => true,
                'sort_order' => 2,
            ],
            [
                'code' => 'carbohydrate',
                'name' => 'Carbohidratos',
                'unit' => 'g',
                'description' => 'Carbohidratos totales.',
                'is_macro' => true,
                'sort_order' => 3,
            ],
            [
                'code' => 'total_fat',
                'name' => 'Grasas Totales',
                'unit' => 'g',
                'description' => 'Lípidos totales.',
                'is_macro' => true,
                'sort_order' => 4,
            ],

            // Carbohydrate Subtypes
            [
                'code' => 'fiber',
                'name' => 'Fibra Dietética',
                'unit' => 'g',
                'description' => 'Fibra alimentaria total.',
                'is_macro' => false,
                'sort_order' => 5,
            ],
            [
                'code' => 'sugar',
                'name' => 'Azúcares',
                'unit' => 'g',
                'description' => 'Azúcares totales simples.',
                'is_macro' => false,
                'sort_order' => 6,
            ],
            [
                'code' => 'added_sugars',
                'name' => 'Azúcares Añadidos',
                'unit' => 'g',
                'description' => 'Azúcares incorporados durante el procesamiento.',
                'is_macro' => false,
                'sort_order' => 7,
            ],

            // Lipid Subtypes
            [
                'code' => 'saturated_fat',
                'name' => 'Grasas Saturadas',
                'unit' => 'g',
                'description' => 'Ácidos grasos saturados.',
                'is_macro' => false,
                'sort_order' => 8,
            ],
            [
                'code' => 'trans_fat',
                'name' => 'Grasas Trans',
                'unit' => 'g',
                'description' => 'Ácidos grasos trans.',
                'is_macro' => false,
                'sort_order' => 9,
            ],
            [
                'code' => 'monounsaturated_fat',
                'name' => 'Grasas Monoinsaturadas',
                'unit' => 'g',
                'description' => 'Ácidos grasos monoinsaturados saludables (Omega-9).',
                'is_macro' => false,
                'sort_order' => 10,
            ],
            [
                'code' => 'polyunsaturated_fat',
                'name' => 'Grasas Poliinsaturadas',
                'unit' => 'g',
                'description' => 'Ácidos grasos poliinsaturados esenciales (Omega-3 y Omega-6).',
                'is_macro' => false,
                'sort_order' => 11,
            ],
            [
                'code' => 'cholesterol',
                'name' => 'Colesterol',
                'unit' => 'mg',
                'description' => 'Colesterol dietético.',
                'is_macro' => false,
                'sort_order' => 12,
            ],

            // Minerals & Electrolytes
            [
                'code' => 'sodium',
                'name' => 'Sodio',
                'unit' => 'mg',
                'description' => 'Sodio (sal dietética).',
                'is_macro' => false,
                'sort_order' => 13,
            ],
            [
                'code' => 'potassium',
                'name' => 'Potasio',
                'unit' => 'mg',
                'description' => 'Potasio mineral.',
                'is_macro' => false,
                'sort_order' => 14,
            ],
            [
                'code' => 'calcium',
                'name' => 'Calcio',
                'unit' => 'mg',
                'description' => 'Calcio para salud ósea y neuromuscular.',
                'is_macro' => false,
                'sort_order' => 15,
            ],
            [
                'code' => 'iron',
                'name' => 'Hierro',
                'unit' => 'mg',
                'description' => 'Hierro para el transporte de oxígeno.',
                'is_macro' => false,
                'sort_order' => 16,
            ],
            [
                'code' => 'magnesium',
                'name' => 'Magnesio',
                'unit' => 'mg',
                'description' => 'Magnesio cofactor enzimático.',
                'is_macro' => false,
                'sort_order' => 17,
            ],
            [
                'code' => 'zinc',
                'name' => 'Zinc',
                'unit' => 'mg',
                'description' => 'Zinc para función inmune.',
                'is_macro' => false,
                'sort_order' => 18,
            ],
            [
                'code' => 'phosphorus',
                'name' => 'Fósforo',
                'unit' => 'mg',
                'description' => 'Fósforo estructural y energético.',
                'is_macro' => false,
                'sort_order' => 19,
            ],

            // Vitamins
            [
                'code' => 'vitamin_a',
                'name' => 'Vitamina A',
                'unit' => 'mcg',
                'description' => 'Equivalentes de actividad de retinol (RAE).',
                'is_macro' => false,
                'sort_order' => 20,
            ],
            [
                'code' => 'vitamin_c',
                'name' => 'Vitamina C',
                'unit' => 'mg',
                'description' => 'Ácido ascórbico antioxidante.',
                'is_macro' => false,
                'sort_order' => 21,
            ],
            [
                'code' => 'vitamin_d',
                'name' => 'Vitamina D',
                'unit' => 'mcg',
                'description' => 'Colecalciferol / Ergocalciferol (D2 + D3).',
                'is_macro' => false,
                'sort_order' => 22,
            ],
            [
                'code' => 'vitamin_e',
                'name' => 'Vitamina E',
                'unit' => 'mg',
                'description' => 'Alfa-tocoferol.',
                'is_macro' => false,
                'sort_order' => 23,
            ],
            [
                'code' => 'vitamin_k',
                'name' => 'Vitamina K',
                'unit' => 'mcg',
                'description' => 'Filoquinona.',
                'is_macro' => false,
                'sort_order' => 24,
            ],
            [
                'code' => 'thiamin_b1',
                'name' => 'Tiamina (B1)',
                'unit' => 'mg',
                'description' => 'Vitamina B1.',
                'is_macro' => false,
                'sort_order' => 25,
            ],
            [
                'code' => 'riboflavin_b2',
                'name' => 'Riboflavina (B2)',
                'unit' => 'mg',
                'description' => 'Vitamina B2.',
                'is_macro' => false,
                'sort_order' => 26,
            ],
            [
                'code' => 'niacin_b3',
                'name' => 'Niacina (B3)',
                'unit' => 'mg',
                'description' => 'Vitamina B3.',
                'is_macro' => false,
                'sort_order' => 27,
            ],
            [
                'code' => 'vitamin_b6',
                'name' => 'Vitamina B6',
                'unit' => 'mg',
                'description' => 'Piridoxina.',
                'is_macro' => false,
                'sort_order' => 28,
            ],
            [
                'code' => 'folate_b9',
                'name' => 'Folato / Ácido Fólico (B9)',
                'unit' => 'mcg',
                'description' => 'Equivalentes de folato dietético (DFE).',
                'is_macro' => false,
                'sort_order' => 29,
            ],
            [
                'code' => 'vitamin_b12',
                'name' => 'Vitamina B12',
                'unit' => 'mcg',
                'description' => 'Cobalamina.',
                'is_macro' => false,
                'sort_order' => 30,
            ],

            // Other
            [
                'code' => 'water',
                'name' => 'Agua',
                'unit' => 'g',
                'description' => 'Contenido de humedad en gramos.',
                'is_macro' => false,
                'sort_order' => 31,
            ],
            [
                'code' => 'caffeine',
                'name' => 'Cafeína',
                'unit' => 'mg',
                'description' => 'Cafeína estimulante.',
                'is_macro' => false,
                'sort_order' => 32,
            ],
            [
                'code' => 'alcohol',
                'name' => 'Alcohol',
                'unit' => 'g',
                'description' => 'Etanol.',
                'is_macro' => false,
                'sort_order' => 33,
            ],
        ];

        foreach ($nutrients as $nutrient) {
            Nutrient::updateOrCreate(
                ['code' => $nutrient['code']],
                $nutrient
            );
        }
    }
}
