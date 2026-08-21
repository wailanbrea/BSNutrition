<?php

namespace App\Http\Requests\Profile;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class UpdateProfileRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    /**
     * @return array<string, ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'birth_date' => ['nullable', 'date', 'before:today'],
            'sex' => ['nullable', 'string', 'in:male,female,other'],
            'height' => ['nullable', 'numeric', 'min:30', 'max:300'],
            'current_weight' => ['nullable', 'numeric', 'min:10', 'max:500'],
            'activity_level' => ['nullable', 'string', 'in:sedentary,lightly_active,moderately_active,very_active,extra_active'],
            'goal_type' => ['nullable', 'string', 'in:lose_weight,maintain_weight,gain_weight,build_muscle'],
            'goal_weight' => ['nullable', 'numeric', 'min:10', 'max:500'],
            'weekly_goal_rate' => ['nullable', 'numeric', 'min:0', 'max:2'],
            'locale' => ['nullable', 'string', 'max:10'],
            'country_code' => ['nullable', 'string', 'size:2'],
            'timezone' => ['nullable', 'string', 'timezone'],
            'unit_system' => ['nullable', 'string', 'in:metric,imperial'],
        ];
    }
}
