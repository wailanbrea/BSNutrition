<?php

namespace App\Http\Requests\Goal;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class CalculateGoalRequest extends FormRequest
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
            'sex' => ['nullable', 'string', 'in:male,female'],
            'height' => ['nullable', 'numeric', 'min:50', 'max:250'],
            'current_weight' => ['nullable', 'numeric', 'min:20', 'max:500'],
            'activity_level' => ['nullable', 'string', 'in:sedentary,light,moderate,active,very_active'],
            'goal_type' => ['nullable', 'string', 'in:lose_weight,maintain_weight,gain_muscle,gain_weight'],
            'weekly_goal_rate' => ['nullable', 'numeric', 'min:0', 'max:2.0'],
        ];
    }
}
