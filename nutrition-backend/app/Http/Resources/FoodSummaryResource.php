<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class FoodSummaryResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        $nutrients = $this->relationLoaded('nutrients') ? $this->nutrients->keyBy('code') : collect();

        $defaultPortion = $this->relationLoaded('portions')
            ? ($this->portions->firstWhere('is_default', true) ?? $this->portions->first())
            : null;

        return [
            'id' => $this->id,
            'canonical_name' => $this->canonical_name,
            'brand' => $this->brand ? $this->brand->name : null,
            'category' => $this->category ? [
                'id' => $this->category->id,
                'name' => $this->category->name,
                'slug' => $this->category->slug,
                'icon' => $this->category->icon,
            ] : null,
            'country_code' => $this->country_code,
            'verified' => (bool) $this->verified,
            'source' => $this->source,
            'default_basis_amount' => (float) $this->default_basis_amount,
            'default_basis_unit' => $this->default_basis_unit,
            'macros_per_100g' => [
                'calories' => (int) round((float) ($nutrients['calories']->pivot->amount ?? 0)),
                'protein_g' => (float) ($nutrients['protein']->pivot->amount ?? 0),
                'carbs_g' => (float) ($nutrients['carbohydrate']->pivot->amount ?? 0),
                'fat_g' => (float) ($nutrients['total_fat']->pivot->amount ?? 0),
                'fiber_g' => (float) ($nutrients['fiber']->pivot->amount ?? 0),
            ],
            'default_portion' => $defaultPortion ? [
                'id' => $defaultPortion->id,
                'portion_name' => $defaultPortion->portion_name,
                'gram_weight' => (float) $defaultPortion->gram_weight,
            ] : null,
        ];
    }
}
