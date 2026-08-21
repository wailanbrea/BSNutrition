<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class WaterLogResource extends JsonResource
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
            'log_date' => $this->log_date instanceof \DateTimeInterface ? $this->log_date->format('Y-m-d') : (string) $this->log_date,
            'amount_ml' => (int) $this->amount_ml,
            'occurred_at' => $this->occurred_at?->toISOString(),
            'source' => $this->source,
            'version' => (int) $this->version,
            'created_at' => $this->created_at?->toISOString(),
        ];
    }
}
