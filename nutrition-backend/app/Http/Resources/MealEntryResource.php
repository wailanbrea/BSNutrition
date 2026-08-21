<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class MealEntryResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'client_id' => $this->client_id,
            'meal_id' => $this->meal_id,
            'food_id' => $this->food_id,
            'portion_id' => $this->portion_id,
            'custom_name' => $this->custom_name,
            'quantity' => (float) $this->quantity,
            'unit' => $this->unit,
            'grams' => (float) $this->grams,
            'calories_snapshot' => (int) $this->calories_snapshot,
            'protein_snapshot' => (float) $this->protein_snapshot,
            'carbs_snapshot' => (float) $this->carbs_snapshot,
            'fat_snapshot' => (float) $this->fat_snapshot,
            'fiber_snapshot' => $this->fiber_snapshot !== null ? (float) $this->fiber_snapshot : null,
            'sodium_snapshot' => $this->sodium_snapshot !== null ? (float) $this->sodium_snapshot : null,
            'sugar_snapshot' => $this->sugar_snapshot !== null ? (float) $this->sugar_snapshot : null,
            'nutrient_snapshot_json' => $this->nutrient_snapshot_json,
            'source' => $this->source,
            'version' => (int) $this->version,
            'food' => $this->whenLoaded('food', fn () => new FoodSummaryResource($this->food)),
            'portion' => $this->whenLoaded('portion', fn () => new FoodPortionResource($this->portion)),
            'created_at' => $this->created_at?->toISOString(),
            'updated_at' => $this->updated_at?->toISOString(),
        ];
    }
}
