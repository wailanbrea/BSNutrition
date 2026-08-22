<?php

use App\Services\NutritionLabelParserService;

beforeEach(function () {
    $this->parser = new NutritionLabelParserService;
});

test('parseRawText extracts complete US Nutrition Facts label', function () {
    $usLabel = <<<TEXT
Nutrition Facts
8 servings per container
Serving size 2/3 cup (55g)
Amount per serving
Calories 230
Total Fat 8g
Saturated Fat 1g
Trans Fat 0g
Sodium 160mg
Total Carbohydrate 37g
Dietary Fiber 4g
Total Sugars 12g
Protein 3g
TEXT;

    $result = $this->parser->parseRawText($usLabel);

    expect($result['serving']['weight_grams'])->toBe(55.0)
        ->and($result['per_serving']['calories'])->toBe(230)
        ->and($result['per_serving']['fat_g'])->toBe(8.0)
        ->and($result['per_serving']['saturated_fat_g'])->toBe(1.0)
        ->and($result['per_serving']['sodium_mg'])->toBe(160.0)
        ->and($result['per_serving']['carbs_g'])->toBe(37.0)
        ->and($result['per_serving']['fiber_g'])->toBe(4.0)
        ->and($result['per_serving']['sugars_g'])->toBe(12.0)
        ->and($result['per_serving']['protein_g'])->toBe(3.0)
        // Per 100g calculated: 230 * (100 / 55) = ~418 kcal
        ->and($result['per_100g']['calories'])->toBe(418)
        ->and($result['confidence'])->toBe(1.0);
});

test('parseRawText extracts Spanish Tabla Nutricional label', function () {
    $spanishLabel = <<<TEXT
INFORMACIÓN NUTRICIONAL
Porción: 30g
Calorías 140
Grasas totales 6.5g
Grasas saturadas 2.0g
Sodio 95mg
Carbohidratos totales 18g
Fibra dietética 2g
Azúcares 5g
Proteínas 4g
TEXT;

    $result = $this->parser->parseRawText($spanishLabel);

    expect($result['serving']['weight_grams'])->toBe(30.0)
        ->and($result['per_serving']['calories'])->toBe(140)
        ->and($result['per_serving']['fat_g'])->toBe(6.5)
        ->and($result['per_serving']['carbs_g'])->toBe(18.0)
        ->and($result['per_serving']['protein_g'])->toBe(4.0)
        ->and($result['confidence'])->toBe(1.0);
});

test('parseRawText does not invent missing nutrients', function () {
    $partialLabel = <<<TEXT
Valores por 100g:
Energia 90 kcal
Proteina 12g
TEXT;

    $result = $this->parser->parseRawText($partialLabel);

    expect($result['per_serving']['calories'])->toBe(90)
        ->and($result['per_serving']['protein_g'])->toBe(12.0)
        ->and($result['per_serving']['fat_g'])->toBeNull()
        ->and($result['per_serving']['carbs_g'])->toBeNull();
});
