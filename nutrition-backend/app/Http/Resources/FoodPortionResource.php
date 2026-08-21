<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class FoodPortionResource extends JsonResource
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
            'portion_name' => $this->portion_name,
            'gram_weight' => (float) $this->gram_weight,
            'amount' => (float) $this->amount,
            'unit' => $this->unit,
            'is_default' => (bool) $this->is_default,
        ];
    }
}
