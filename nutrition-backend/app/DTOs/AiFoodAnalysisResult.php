<?php

namespace App\DTOs;

class AiFoodAnalysisResult
{
    /**
     * @param  array<int, AiRecognizedFoodItem>  $items
     */
    public function __construct(
        public readonly string $dishName,
        public readonly string $summary,
        public readonly array $items,
        public readonly float $confidenceScore,
        public readonly int $totalEstimatedCalories,
        public readonly float $totalEstimatedProteinG,
        public readonly float $totalEstimatedCarbsG,
        public readonly float $totalEstimatedFatG,
        public readonly string $provider,
        public readonly string $model,
        public readonly int $promptTokens = 0,
        public readonly int $completionTokens = 0,
        public readonly float $estimatedCostUsd = 0.0,
    ) {}

    /**
     * @param  array<string, mixed>  $data
     */
    public static function fromArray(array $data, string $provider = 'mock', string $model = 'default'): self
    {
        $rawItems = $data['items'] ?? [];
        $items = [];
        foreach ($rawItems as $item) {
            $items[] = $item instanceof AiRecognizedFoodItem ? $item : AiRecognizedFoodItem::fromArray($item);
        }

        $totalCals = (int) ($data['total_estimated_calories'] ?? array_sum(array_map(fn ($i) => $i->estimatedCalories, $items)));
        $totalProt = (float) ($data['total_estimated_protein_g'] ?? array_sum(array_map(fn ($i) => $i->estimatedProteinG, $items)));
        $totalCarbs = (float) ($data['total_estimated_carbs_g'] ?? array_sum(array_map(fn ($i) => $i->estimatedCarbsG, $items)));
        $totalFat = (float) ($data['total_estimated_fat_g'] ?? array_sum(array_map(fn ($i) => $i->estimatedFatG, $items)));

        return new self(
            dishName: (string) ($data['dish_name'] ?? 'Platillo identificado'),
            summary: (string) ($data['summary'] ?? 'Comida detectada mediante visión artificial'),
            items: $items,
            confidenceScore: (float) ($data['confidence_score'] ?? 0.88),
            totalEstimatedCalories: $totalCals,
            totalEstimatedProteinG: $totalProt,
            totalEstimatedCarbsG: $totalCarbs,
            totalEstimatedFatG: $totalFat,
            provider: $provider,
            model: $model,
            promptTokens: (int) ($data['prompt_tokens'] ?? 0),
            completionTokens: (int) ($data['completion_tokens'] ?? 0),
            estimatedCostUsd: (float) ($data['estimated_cost_usd'] ?? 0.0),
        );
    }

    /**
     * @return array<string, mixed>
     */
    public function toArray(): array
    {
        return [
            'dish_name' => $this->dishName,
            'summary' => $this->summary,
            'items' => array_map(fn ($i) => $i->toArray(), $this->items),
            'confidence_score' => $this->confidenceScore,
            'total_estimated_calories' => $this->totalEstimatedCalories,
            'total_estimated_protein_g' => $this->totalEstimatedProteinG,
            'total_estimated_carbs_g' => $this->totalEstimatedCarbsG,
            'total_estimated_fat_g' => $this->totalEstimatedFatG,
            'provider' => $this->provider,
            'model' => $this->model,
            'usage' => [
                'prompt_tokens' => $this->promptTokens,
                'completion_tokens' => $this->completionTokens,
                'estimated_cost_usd' => $this->estimatedCostUsd,
            ],
        ];
    }
}
