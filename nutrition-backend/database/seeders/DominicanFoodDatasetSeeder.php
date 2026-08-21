<?php

namespace Database\Seeders;

use App\Models\Food;
use App\Models\FoodAlias;
use App\Models\FoodCategory;
use App\Models\FoodNutrient;
use App\Models\FoodPortion;
use App\Models\Nutrient;
use Illuminate\Database\Seeder;

class DominicanFoodDatasetSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $nutrients = Nutrient::all()->keyBy('code');
        $categories = FoodCategory::all()->keyBy('slug');

        $foods = [
            [
                'name' => 'Mangú de Plátano Verde',
                'category' => 'platos-preparados',
                'aliases' => ['mangu', 'plátano majado', 'los tres golpes', 'mangu dominicano'],
                'portions' => [
                    ['name' => '1 porción mediana (1 taza / 200g)', 'grams' => 200.0, 'is_default' => true],
                    ['name' => '1 porción grande (300g)', 'grams' => 300.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 155.0,
                    'protein' => 1.5,
                    'carbohydrate' => 31.0,
                    'total_fat' => 3.2,
                    'fiber' => 2.5,
                    'sugar' => 14.0,
                    'sodium' => 280.0,
                    'potassium' => 450.0,
                    'calcium' => 15.0,
                    'iron' => 0.8,
                ],
            ],
            [
                'name' => 'Habichuelas Rojas Guisadas Dominicanas',
                'category' => 'legumbres-y-frutos-secos',
                'aliases' => ['habichuelas guisadas', 'frijoles rojos dominicanos', 'habichuela de la bandera'],
                'portions' => [
                    ['name' => '1 cucharón hondo (150g)', 'grams' => 150.0, 'is_default' => true],
                    ['name' => '1 taza (200g)', 'grams' => 200.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 110.0,
                    'protein' => 6.8,
                    'carbohydrate' => 18.5,
                    'total_fat' => 1.2,
                    'fiber' => 5.4,
                    'sugar' => 1.5,
                    'sodium' => 380.0,
                    'potassium' => 390.0,
                    'iron' => 2.1,
                ],
            ],
            [
                'name' => 'Pollo Guisado Dominicano',
                'category' => 'carnes-y-aves',
                'aliases' => ['pollo al caldero', 'pollo guisao', 'carne de la bandera'],
                'portions' => [
                    ['name' => '1 presa / porción mediana (150g)', 'grams' => 150.0, 'is_default' => true],
                    ['name' => '1 porción grande (220g)', 'grams' => 220.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 175.0,
                    'protein' => 24.0,
                    'carbohydrate' => 3.5,
                    'total_fat' => 7.0,
                    'saturated_fat' => 1.8,
                    'sodium' => 420.0,
                    'potassium' => 310.0,
                    'iron' => 1.2,
                ],
            ],
            [
                'name' => 'Arroz Blanco Dominicano',
                'category' => 'cereales-y-granos',
                'aliases' => ['arroz blanco', 'arroz con concón', 'arroz de la bandera'],
                'portions' => [
                    ['name' => '1 taza cocida (180g)', 'grams' => 180.0, 'is_default' => true],
                    ['name' => 'Media taza cocida (90g)', 'grams' => 90.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 140.0,
                    'protein' => 2.7,
                    'carbohydrate' => 29.5,
                    'total_fat' => 1.5,
                    'fiber' => 0.4,
                    'sodium' => 210.0,
                    'iron' => 1.4,
                ],
            ],
            [
                'name' => 'Moro de Guandules con Coco',
                'category' => 'platos-preparados',
                'aliases' => ['moro de gandules', 'arroz con guandules', 'moro con coco'],
                'portions' => [
                    ['name' => '1 taza cocida (200g)', 'grams' => 200.0, 'is_default' => true],
                ],
                'nutrients' => [
                    'calories' => 185.0,
                    'protein' => 4.5,
                    'carbohydrate' => 32.0,
                    'total_fat' => 4.8,
                    'fiber' => 3.2,
                    'sodium' => 340.0,
                    'potassium' => 240.0,
                ],
            ],
            [
                'name' => 'Sancocho Dominicano de 7 Carnes',
                'category' => 'platos-preparados',
                'aliases' => ['sancocho', 'sancocho criollo', 'caldo dominicano'],
                'portions' => [
                    ['name' => '1 plato hondo / servicio (350g)', 'grams' => 350.0, 'is_default' => true],
                ],
                'nutrients' => [
                    'calories' => 135.0,
                    'protein' => 11.5,
                    'carbohydrate' => 12.0,
                    'total_fat' => 4.5,
                    'fiber' => 2.0,
                    'sodium' => 460.0,
                    'potassium' => 380.0,
                ],
            ],
            [
                'name' => 'Tostones (Plátano Verde Frito)',
                'category' => 'platos-preparados',
                'aliases' => ['patacones', 'platanitos fritos', 'fritura'],
                'portions' => [
                    ['name' => '1 porción (5 tostones / 120g)', 'grams' => 120.0, 'is_default' => true],
                    ['name' => '1 tostón individual (25g)', 'grams' => 25.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 230.0,
                    'protein' => 1.6,
                    'carbohydrate' => 34.0,
                    'total_fat' => 10.5,
                    'saturated_fat' => 2.2,
                    'fiber' => 2.8,
                    'sodium' => 320.0,
                    'potassium' => 460.0,
                ],
            ],
            [
                'name' => 'Queso Blanco Frito Dominicano',
                'category' => 'lacteos-y-huevos',
                'aliases' => ['queso frito', 'queso de freir', 'queso geo'],
                'portions' => [
                    ['name' => '1 rebanada mediana (50g)', 'grams' => 50.0, 'is_default' => true],
                    ['name' => '2 rebanadas (100g)', 'grams' => 100.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 310.0,
                    'protein' => 19.5,
                    'carbohydrate' => 2.0,
                    'total_fat' => 25.0,
                    'saturated_fat' => 14.0,
                    'sodium' => 680.0,
                    'calcium' => 520.0,
                ],
            ],
            [
                'name' => 'Salami Dominicano Frito',
                'category' => 'carnes-y-aves',
                'aliases' => ['salami frito', 'salami induveca', 'salchichón criollo'],
                'portions' => [
                    ['name' => '2 rodajas fritas (60g)', 'grams' => 60.0, 'is_default' => true],
                    ['name' => '1 rodaja (30g)', 'grams' => 30.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 340.0,
                    'protein' => 15.0,
                    'carbohydrate' => 4.0,
                    'total_fat' => 29.0,
                    'saturated_fat' => 11.0,
                    'sodium' => 950.0,
                ],
            ],
            [
                'name' => 'Yuca Hervida con Cebollita Encebollada',
                'category' => 'verduras-y-hortalizas',
                'aliases' => ['yuca hervida', 'yuca con mojo', 'mandioca cocida'],
                'portions' => [
                    ['name' => '1 trozo mediano con mojo (150g)', 'grams' => 150.0, 'is_default' => true],
                ],
                'nutrients' => [
                    'calories' => 160.0,
                    'protein' => 1.4,
                    'carbohydrate' => 38.0,
                    'total_fat' => 2.5,
                    'fiber' => 1.8,
                    'sodium' => 220.0,
                    'potassium' => 270.0,
                ],
            ],
            [
                'name' => 'Mofongo Dominicano con Chicharrón',
                'category' => 'platos-preparados',
                'aliases' => ['mofongo', 'mofongo de chicharrón', 'plátano con ajo'],
                'portions' => [
                    ['name' => '1 pilón individual (250g)', 'grams' => 250.0, 'is_default' => true],
                ],
                'nutrients' => [
                    'calories' => 285.0,
                    'protein' => 9.5,
                    'carbohydrate' => 32.0,
                    'total_fat' => 14.0,
                    'saturated_fat' => 4.5,
                    'sodium' => 540.0,
                ],
            ],
            [
                'name' => 'Jugo de Morir Soñando',
                'category' => 'bebidas-e-infusiones',
                'aliases' => ['morir soñando', 'morisoñando', 'naranja con leche'],
                'portions' => [
                    ['name' => '1 vaso regular (250ml / 250g)', 'grams' => 250.0, 'is_default' => true],
                ],
                'nutrients' => [
                    'calories' => 95.0,
                    'protein' => 2.8,
                    'carbohydrate' => 15.0,
                    'total_fat' => 2.5,
                    'sugar' => 13.5,
                    'vitamin_c' => 25.0,
                    'calcium' => 90.0,
                    'sodium' => 45.0,
                ],
            ],
            [
                'name' => 'Aguacate Criollo Dominicano',
                'category' => 'frutas',
                'aliases' => ['aguacate', 'palta', 'aguacate de mantequilla'],
                'portions' => [
                    ['name' => '1 lonja / cuarto mediano (75g)', 'grams' => 75.0, 'is_default' => true],
                    ['name' => 'Medio aguacate (150g)', 'grams' => 150.0, 'is_default' => false],
                ],
                'nutrients' => [
                    'calories' => 160.0,
                    'protein' => 2.0,
                    'carbohydrate' => 8.5,
                    'total_fat' => 14.7,
                    'monounsaturated_fat' => 9.8,
                    'fiber' => 6.7,
                    'potassium' => 485.0,
                    'sodium' => 7.0,
                ],
            ],
            [
                'name' => 'Yaniqueque Dominicano',
                'category' => 'snacks-y-dulces',
                'aliases' => ['yaniqueque', 'johnny cake', 'fritura de playa'],
                'portions' => [
                    ['name' => '1 unidad mediana (80g)', 'grams' => 80.0, 'is_default' => true],
                ],
                'nutrients' => [
                    'calories' => 380.0,
                    'protein' => 5.2,
                    'carbohydrate' => 48.0,
                    'total_fat' => 18.5,
                    'sodium' => 490.0,
                ],
            ],
            [
                'name' => 'Habichuelas con Dulce',
                'category' => 'snacks-y-dulces',
                'aliases' => ['habichuelas con dulce', 'postre dominicano de semana santa'],
                'portions' => [
                    ['name' => '1 taza con galletitas (220g)', 'grams' => 220.0, 'is_default' => true],
                ],
                'nutrients' => [
                    'calories' => 170.0,
                    'protein' => 4.2,
                    'carbohydrate' => 32.0,
                    'total_fat' => 3.2,
                    'fiber' => 3.0,
                    'sugar' => 24.0,
                    'calcium' => 85.0,
                    'iron' => 1.8,
                ],
            ],
        ];

        foreach ($foods as $f) {
            $catId = isset($categories[$f['category']]) ? $categories[$f['category']]->id : null;
            $normalizedName = mb_strtolower(trim($f['name']), 'UTF-8');

            $food = Food::updateOrCreate(
                [
                    'canonical_name' => $f['name'],
                    'country_code' => 'DO',
                ],
                [
                    'normalized_name' => $normalizedName,
                    'category_id' => $catId,
                    'language' => 'es',
                    'verified' => true,
                    'source' => 'generic',
                    'default_basis_amount' => 100.00,
                    'default_basis_unit' => 'g',
                ]
            );

            // Aliases
            foreach ($f['aliases'] as $alias) {
                FoodAlias::updateOrCreate(
                    [
                        'food_id' => $food->id,
                        'alias' => $alias,
                    ],
                    [
                        'normalized_alias' => mb_strtolower(trim($alias), 'UTF-8'),
                        'language' => 'es',
                    ]
                );
            }

            // Portions
            foreach ($f['portions'] as $p) {
                FoodPortion::updateOrCreate(
                    [
                        'food_id' => $food->id,
                        'portion_name' => $p['name'],
                    ],
                    [
                        'gram_weight' => $p['grams'],
                        'amount' => 1.0,
                        'unit' => 'porción',
                        'is_default' => $p['is_default'],
                    ]
                );
            }

            // Nutrients
            foreach ($f['nutrients'] as $code => $amount) {
                if (isset($nutrients[$code])) {
                    FoodNutrient::updateOrCreate(
                        [
                            'food_id' => $food->id,
                            'nutrient_id' => $nutrients[$code]->id,
                        ],
                        [
                            'amount' => (float) $amount,
                            'basis_amount' => 100.00,
                            'basis_unit' => 'g',
                            'source' => 'generic',
                        ]
                    );
                }
            }
        }
    }
}
