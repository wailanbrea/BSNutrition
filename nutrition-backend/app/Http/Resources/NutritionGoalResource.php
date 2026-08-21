<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class NutritionGoalResource extends JsonResource
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
            'user_id' => $this->user_id,
            'effective_from' => $this->effective_from?->format('Y-m-d'),
            'calorie_target' => $this->calorie_target,
            'protein_target_g' => $this->protein_target_g,
            'carbohydrate_target_g' => $this->carbohydrate_target_g,
            'fat_target_g' => $this->fat_target_g,
            'fiber_target_g' => $this->fiber_target_g,
            'water_target_ml' => $this->water_target_ml,
            'source' => $this->source,
            'calculation_version' => $this->calculation_version,
            'created_at' => $this->created_at?->toISOString(),
            'updated_at' => $this->updated_at?->toISOString(),
        ];
    }
}
