<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class CopyMealRequest extends FormRequest
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
            'source_meal_id' => 'required|integer|exists:meals,id',
            'target_date' => 'required|date_format:Y-m-d',
            'target_meal_type' => 'required|string|in:breakfast,lunch,dinner,snack,custom',
        ];
    }
}
