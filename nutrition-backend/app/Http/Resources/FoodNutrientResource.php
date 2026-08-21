<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class FoodNutrientResource extends JsonResource
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
            'code' => $this->code,
            'name' => $this->name,
            'unit' => $this->unit,
            'amount' => (float) ($this->pivot->amount ?? $this->amount ?? 0),
            'basis_amount' => (float) ($this->pivot->basis_amount ?? 100),
            'basis_unit' => $this->pivot->basis_unit ?? 'g',
            'is_macro' => (bool) $this->is_macro,
        ];
    }
}
