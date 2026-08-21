<?php

namespace App\DTOs;

use App\Models\Food;

class FoodMatchCandidate
{
    public function __construct(
        public readonly Food $food,
        public readonly float $score,
        public readonly string $matchType, // exact, alias, fuzzy, token
        public readonly ?string $matchedAlias = null,
    ) {}

    /**
     * @return array<string, mixed>
     */
    public function toArray(): array
    {
        return [
            'food_id' => $this->food->id,
            'canonical_name' => $this->food->canonical_name,
            'brand_name' => $this->food->brand?->name,
            'score' => round($this->score, 2),
            'match_type' => $this->matchType,
            'matched_alias' => $this->matchedAlias,
            'calories_100g' => $this->food->getNutrientAmount('ENERGY_KCAL'),
            'protein_100g' => $this->food->getNutrientAmount('PROTEIN_G'),
            'carbs_100g' => $this->food->getNutrientAmount('CARBS_G'),
            'fat_100g' => $this->food->getNutrientAmount('FAT_G'),
        ];
    }
}
