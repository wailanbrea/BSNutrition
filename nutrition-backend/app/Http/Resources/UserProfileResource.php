<?php

namespace App\Http\Resources;

use App\Models\UserProfile;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

/**
 * @mixin UserProfile
 */
class UserProfileResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'user_id' => $this->user_id,
            'birth_date' => $this->birth_date?->format('Y-m-d'),
            'sex' => $this->sex,
            'height' => $this->height,
            'current_weight' => $this->current_weight,
            'activity_level' => $this->activity_level,
            'goal_type' => $this->goal_type,
            'goal_weight' => $this->goal_weight,
            'weekly_goal_rate' => $this->weekly_goal_rate,
            'locale' => $this->locale,
            'country_code' => $this->country_code,
            'timezone' => $this->timezone,
            'unit_system' => $this->unit_system,
            'created_at' => $this->created_at?->toIso8601String(),
            'updated_at' => $this->updated_at?->toIso8601String(),
        ];
    }
}
