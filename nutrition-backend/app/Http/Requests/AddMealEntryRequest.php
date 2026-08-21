<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class AddMealEntryRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array<string, ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'meal_type' => 'required|string|in:breakfast,lunch,dinner,snack,custom',
            'food_id' => 'nullable|integer|exists:foods,id',
            'portion_id' => 'nullable|integer|exists:food_portions,id',
            'custom_name' => 'nullable|string|max:150',
            'quantity' => 'required|numeric|min:0.01|max:10000',
            'unit' => 'nullable|string|max:50',
            'grams' => 'nullable|numeric|min:0.01|max:50000',
            'calories' => 'nullable|integer|min:0|max:50000',
            'protein_g' => 'nullable|numeric|min:0|max:5000',
            'carbs_g' => 'nullable|numeric|min:0|max:5000',
            'fat_g' => 'nullable|numeric|min:0|max:5000',
            'client_id' => 'nullable|string|max:64',
            'source' => 'nullable|string|max:30',
        ];
    }
}
