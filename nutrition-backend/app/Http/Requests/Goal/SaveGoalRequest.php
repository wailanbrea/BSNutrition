<?php

namespace App\Http\Requests\Goal;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class SaveGoalRequest extends FormRequest
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
            'effective_from' => ['nullable', 'date'],
            'calorie_target' => ['required', 'integer', 'min:500', 'max:10000'],
            'protein_target_g' => ['required', 'numeric', 'min:0', 'max:1000'],
            'carbohydrate_target_g' => ['required', 'numeric', 'min:0', 'max:1000'],
            'fat_target_g' => ['required', 'numeric', 'min:0', 'max:1000'],
            'fiber_target_g' => ['nullable', 'numeric', 'min:0', 'max:200'],
            'water_target_ml' => ['nullable', 'integer', 'min:500', 'max:10000'],
            'source' => ['nullable', 'string', 'in:calculated,custom'],
        ];
    }
}
