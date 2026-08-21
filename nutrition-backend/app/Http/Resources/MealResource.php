<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class MealResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        $calories = 0;
        $protein = 0.0;
        $carbs = 0.0;
        $fat = 0.0;

        if ($this->relationLoaded('entries')) {
            foreach ($this->entries as $entry) {
                $calories += $entry->calories_snapshot;
                $protein += (float) $entry->protein_snapshot;
                $carbs += (float) $entry->carbs_snapshot;
                $fat += (float) $entry->fat_snapshot;
            }
        }

        return [
            'id' => $this->id,
            'diary_id' => $this->diary_id,
            'meal_type' => $this->meal_type,
            'name' => $this->name,
            'sort_order' => (int) $this->sort_order,
            'total_calories' => $calories,
            'total_protein_g' => round($protein, 2),
            'total_carbs_g' => round($carbs, 2),
            'total_fat_g' => round($fat, 2),
            'entries' => MealEntryResource::collection($this->whenLoaded('entries')),
        ];
    }
}
