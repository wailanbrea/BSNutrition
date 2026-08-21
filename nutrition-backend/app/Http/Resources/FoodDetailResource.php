<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class FoodDetailResource extends JsonResource
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
            'canonical_name' => $this->canonical_name,
            'brand' => $this->brand ? [
                'id' => $this->brand->id,
                'name' => $this->brand->name,
            ] : null,
            'category' => $this->category ? [
                'id' => $this->category->id,
                'name' => $this->category->name,
                'slug' => $this->category->slug,
                'icon' => $this->category->icon,
            ] : null,
            'country_code' => $this->country_code,
            'language' => $this->language,
            'verified' => (bool) $this->verified,
            'source' => $this->source,
            'external_source_id' => $this->external_source_id,
            'default_basis_amount' => (float) $this->default_basis_amount,
            'default_basis_unit' => $this->default_basis_unit,
            'portions' => FoodPortionResource::collection($this->whenLoaded('portions')),
            'nutrients' => FoodNutrientResource::collection($this->whenLoaded('nutrients')),
            'barcodes' => $this->whenLoaded('barcodes', function () {
                return $this->barcodes->pluck('barcode');
            }),
            'aliases' => $this->whenLoaded('aliases', function () {
                return $this->aliases->pluck('alias');
            }),
            'created_at' => $this->created_at?->toISOString(),
            'updated_at' => $this->updated_at?->toISOString(),
        ];
    }
}
