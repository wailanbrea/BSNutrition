<?php

namespace Database\Seeders;

use App\Models\FoodCategory;
use Illuminate\Database\Seeder;

class FoodCategorySeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $categories = [
            ['name' => 'Carnes y Aves', 'slug' => 'carnes-y-aves', 'icon' => 'meat'],
            ['name' => 'Pescados y Mariscos', 'slug' => 'pescados-y-mariscos', 'icon' => 'fish'],
            ['name' => 'Lácteos y Huevos', 'slug' => 'lacteos-y-huevos', 'icon' => 'egg'],
            ['name' => 'Frutas', 'slug' => 'frutas', 'icon' => 'fruit'],
            ['name' => 'Verduras y Hortalizas', 'slug' => 'verduras-y-hortalizas', 'icon' => 'vegetable'],
            ['name' => 'Cereales y Granos', 'slug' => 'cereales-y-granos', 'icon' => 'grain'],
            ['name' => 'Legumbres y Frutos Secos', 'slug' => 'legumbres-y-frutos-secos', 'icon' => 'nut'],
            ['name' => 'Aceites y Grasas', 'slug' => 'aceites-y-grasas', 'icon' => 'oil'],
            ['name' => 'Bebidas e Infusiones', 'slug' => 'bebidas-e-infusiones', 'icon' => 'drink'],
            ['name' => 'Snacks y Dulces', 'slug' => 'snacks-y-dulces', 'icon' => 'snack'],
            ['name' => 'Suplementos y Proteínas', 'slug' => 'suplementos-y-proteinas', 'icon' => 'supplement'],
            ['name' => 'Platos Preparados', 'slug' => 'platos-preparados', 'icon' => 'meal'],
        ];

        foreach ($categories as $cat) {
            FoodCategory::updateOrCreate(['slug' => $cat['slug']], $cat);
        }
    }
}
