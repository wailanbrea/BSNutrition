<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class FoodNutrient extends Model
{
    use HasFactory;

    protected $fillable = [
        'food_id',
        'nutrient_id',
        'amount',
        'basis_amount',
        'basis_unit',
        'source',
    ];

    protected $casts = [
        'amount' => 'float',
        'basis_amount' => 'float',
    ];

    public function food(): BelongsTo
    {
        return $this->belongsTo(Food::class);
    }

    public function nutrient(): BelongsTo
    {
        return $this->belongsTo(Nutrient::class);
    }
}
