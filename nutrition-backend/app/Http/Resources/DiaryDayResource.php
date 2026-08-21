<?php

namespace App\Http\Resources;

use App\Services\DiaryService;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class DiaryDayResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        $dateStr = $this->diary_date instanceof \DateTimeInterface
            ? $this->diary_date->format('Y-m-d')
            : (string) $this->diary_date;

        $user = $request->user() ?? $this->user;
        $diaryService = app(DiaryService::class);
        $summary = $diaryService->getDailySummary($user, $dateStr);

        return [
            'id' => $this->id,
            'user_id' => $this->user_id,
            'diary_date' => $dateStr,
            'timezone' => $this->timezone,
            'notes' => $this->notes,
            'summary' => [
                'calories' => $summary['calories'],
                'protein_g' => $summary['protein_g'],
                'carbs_g' => $summary['carbs_g'],
                'fat_g' => $summary['fat_g'],
                'fiber_g' => $summary['fiber_g'],
                'water_ml' => $summary['water_ml'],
            ],
            'meals' => MealResource::collection($this->whenLoaded('meals')),
            'created_at' => $this->created_at?->toISOString(),
            'updated_at' => $this->updated_at?->toISOString(),
        ];
    }
}
