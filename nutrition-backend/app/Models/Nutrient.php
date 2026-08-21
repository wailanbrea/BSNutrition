<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Nutrient extends Model
{
    use HasFactory;

    protected $fillable = [
        'code',
        'name',
        'unit',
        'description',
        'is_macro',
        'sort_order',
    ];

    protected $casts = [
        'is_macro' => 'boolean',
        'sort_order' => 'integer',
    ];

    public function foodNutrients(): HasMany
    {
        return $this->hasMany(FoodNutrient::class);
    }
}
