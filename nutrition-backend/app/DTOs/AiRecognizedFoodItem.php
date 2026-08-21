<?php

namespace App\DTOs;

class AiRecognizedFoodItem
{
    public function __construct(
        public readonly string $name,
        public readonly float $estimatedWeightGrams,
        public readonly string $portionDescription,
        public readonly float $confidence,
        public readonly int $estimatedCalories,
        public readonly float $estimatedProteinG,
        public readonly float $estimatedCarbsG,
        public readonly float $estimatedFatG,
        public readonly ?string $preparationMethod = null,
        public readonly ?string $matchedCanonicalName = null,
        public readonly ?int $matchedFoodId = null,
    ) {}

    /**
     * @param  array<string, mixed>  $data
     */
    public static function fromArray(array $data): self
    {
        return new self(
            name: (string) ($data['name'] ?? 'Alimento detectado'),
            estimatedWeightGrams: (float) ($data['estimated_weight_grams'] ?? $data['weight_g'] ?? 100.0),
            portionDescription: (string) ($data['portion_description'] ?? '1 porción'),
            confidence: (float) ($data['confidence'] ?? 0.85),
            estimatedCalories: (int) ($data['estimated_calories'] ?? $data['calories'] ?? 0),
            estimatedProteinG: (float) ($data['estimated_protein_g'] ?? $data['protein_g'] ?? 0.0),
            estimatedCarbsG: (float) ($data['estimated_carbs_g'] ?? $data['carbs_g'] ?? 0.0),
            estimatedFatG: (float) ($data['estimated_fat_g'] ?? $data['fat_g'] ?? 0.0),
            preparationMethod: isset($data['preparation_method']) ? (string) $data['preparation_method'] : null,
            matchedCanonicalName: isset($data['matched_canonical_name']) ? (string) $data['matched_canonical_name'] : null,
            matchedFoodId: isset($data['matched_food_id']) ? (int) $data['matched_food_id'] : null,
        );
    }

    /**
     * @return array<string, mixed>
     */
    public function toArray(): array
    {
        return [
            'name' => $this->name,
            'estimated_weight_grams' => $this->estimatedWeightGrams,
            'portion_description' => $this->portionDescription,
            'confidence' => $this->confidence,
            'estimated_calories' => $this->estimatedCalories,
            'estimated_protein_g' => $this->estimatedProteinG,
            'estimated_carbs_g' => $this->estimatedCarbsG,
            'estimated_fat_g' => $this->estimatedFatG,
            'preparation_method' => $this->preparationMethod,
            'matched_canonical_name' => $this->matchedCanonicalName,
            'matched_food_id' => $this->matchedFoodId,
        ];
    }
}
