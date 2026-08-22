<?php

namespace App\Services;

class NutritionLabelParserService
{
    /**
     * Parse raw OCR text into structured nutrition label data.
     *
     * @return array<string, mixed>
     */
    public function parseRawText(string $rawText): array
    {
        $text = mb_strtolower($rawText, 'UTF-8');

        $servingSize = $this->extractServingSize($text);
        $servingWeightGrams = $servingSize['grams'] ?? 100.0;
        $servingUnit = $servingSize['unit'] ?? 'g';
        $servingName = $servingSize['name'] ?? '1 porción';

        $calories = $this->extractCalories($text);
        $totalFat = $this->extractNutrient($text, ['total fat', 'grasa total', 'grasas totales', 'grasa', 'grasas', 'lipidos']);
        $saturatedFat = $this->extractNutrient($text, ['saturated fat', 'grasa saturada', 'grasas saturadas']);
        $transFat = $this->extractNutrient($text, ['trans fat', 'grasa trans', 'grasas trans']);
        $sodiumMg = $this->extractSodium($text);
        $totalCarbs = $this->extractNutrient($text, ['total carbohydrate', 'carbohidratos totales', 'carbohidrato total', 'carbohidratos', 'hidratos de carbono']);
        $fiber = $this->extractNutrient($text, ['dietary fiber', 'fibra dietetica', 'fibra dietética', 'fibra']);
        $sugars = $this->extractNutrient($text, ['total sugars', 'azucares totales', 'azúcares totales', 'azucares', 'azúcares', 'azucar']);
        $protein = $this->extractNutrient($text, ['protein', 'proteina', 'proteína', 'proteinas', 'proteínas']);

        // Factor to calculate per 100g if serving size is given
        $ratio100g = $servingWeightGrams > 0 ? (100.0 / $servingWeightGrams) : 1.0;

        return [
            'raw_text_length' => strlen($rawText),
            'serving' => [
                'name' => $servingName,
                'weight_grams' => round($servingWeightGrams, 2),
                'unit' => $servingUnit,
            ],
            'per_serving' => [
                'calories' => $calories,
                'fat_g' => $totalFat,
                'saturated_fat_g' => $saturatedFat,
                'trans_fat_g' => $transFat,
                'sodium_mg' => $sodiumMg,
                'carbs_g' => $totalCarbs,
                'fiber_g' => $fiber,
                'sugars_g' => $sugars,
                'protein_g' => $protein,
            ],
            'per_100g' => [
                'calories' => $calories !== null ? (int) round($calories * $ratio100g) : 0,
                'fat_g' => $totalFat !== null ? round($totalFat * $ratio100g, 2) : 0.0,
                'saturated_fat_g' => $saturatedFat !== null ? round($saturatedFat * $ratio100g, 2) : 0.0,
                'trans_fat_g' => $transFat !== null ? round($transFat * $ratio100g, 2) : 0.0,
                'sodium_mg' => $sodiumMg !== null ? round($sodiumMg * $ratio100g, 2) : 0.0,
                'carbs_g' => $totalCarbs !== null ? round($totalCarbs * $ratio100g, 2) : 0.0,
                'fiber_g' => $fiber !== null ? round($fiber * $ratio100g, 2) : 0.0,
                'sugars_g' => $sugars !== null ? round($sugars * $ratio100g, 2) : 0.0,
                'protein_g' => $protein !== null ? round($protein * $ratio100g, 2) : 0.0,
            ],
            'confidence' => $this->calculateParsingConfidence($calories, $protein, $totalCarbs, $totalFat),
        ];
    }

    private function extractServingSize(string $text): array
    {
        // Pattern 1: "serving size 30g" or "tamaño por porcion 28.3 g" or "porcion 100g"
        if (preg_match('/(?:serving size|tamano por porcion|tamaño por porción|tamaño de la porción|porcion|porción)[:\s]+(?:[0-9\/]+\s*[a-zA-Z]+\s*\(?)?([0-9]+(?:[\.,][0-9]+)?)\s*(g|gr|gramos|ml|mililitros)/i', $text, $matches)) {
            $val = (float) str_replace(',', '.', $matches[1]);
            $unit = in_array(strtolower($matches[2]), ['ml', 'mililitros'], true) ? 'ml' : 'g';

            return [
                'name' => '1 porción',
                'grams' => $val,
                'unit' => $unit,
            ];
        }

        // Pattern 2: simple "(240 g)" or "(30g)"
        if (preg_match('/\(([0-9]+(?:[\.,][0-9]+)?)\s*(g|gr|ml)\)/i', $text, $matches)) {
            $val = (float) str_replace(',', '.', $matches[1]);

            return [
                'name' => '1 porción',
                'grams' => $val,
                'unit' => $matches[2] === 'ml' ? 'ml' : 'g',
            ];
        }

        return [
            'name' => '100g',
            'grams' => 100.0,
            'unit' => 'g',
        ];
    }

    private function extractCalories(string $text): ?int
    {
        // "calories 150", "calorias 220", "valor energetico 140 kcal", "energia 200 kcal"
        if (preg_match('/(?:calories|calorias|calorías|valor energetico|valor energético|energia|energía|energy)[:\s]+([0-9]{1,4})(?:\s*kcal)?/i', $text, $matches)) {
            return (int) $matches[1];
        }

        return null;
    }

    private function extractSodium(string $text): ?float
    {
        // "sodium 200mg", "sodio 150 mg"
        if (preg_match('/(?:sodium|sodio)[:\s]+([0-9]+(?:[\.,][0-9]+)?)\s*(mg|g)?/i', $text, $matches)) {
            $val = (float) str_replace(',', '.', $matches[1]);
            $unit = isset($matches[2]) ? strtolower($matches[2]) : 'mg';
            if ($unit === 'g') {
                $val *= 1000.0; // convert g to mg
            }

            return $val;
        }

        return null;
    }

    /**
     * @param  array<string>  $keywords
     */
    private function extractNutrient(string $text, array $keywords): ?float
    {
        $joined = implode('|', array_map('preg_quote', $keywords));
        // Match keyword followed by optional separator and numeric grams
        $pattern = '/(?:'.$joined.')[:\s]+<?\s*([0-9]+(?:[\.,][0-9]+)?)\s*g/i';

        if (preg_match($pattern, $text, $matches)) {
            return (float) str_replace(',', '.', $matches[1]);
        }

        return null;
    }

    private function calculateParsingConfidence(?int $cals, ?float $prot, ?float $carbs, ?float $fat): float
    {
        $found = 0;
        if ($cals !== null) {
            $found++;
        }
        if ($prot !== null) {
            $found++;
        }
        if ($carbs !== null) {
            $found++;
        }
        if ($fat !== null) {
            $found++;
        }

        return round($found / 4.0, 2);
    }
}
