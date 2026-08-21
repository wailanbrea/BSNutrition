<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class DailySummaryResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'date' => $this->resource['date'],
            'calories' => (int) $this->resource['calories'],
            'protein_g' => (float) $this->resource['protein_g'],
            'carbs_g' => (float) $this->resource['carbs_g'],
            'fat_g' => (float) $this->resource['fat_g'],
            'fiber_g' => (float) $this->resource['fiber_g'],
            'water_ml' => (int) $this->resource['water_ml'],
            'meals' => $this->resource['meals'],
        ];
    }
}
