<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\SoftDeletes;

class MealEntry extends Model
{
    use HasFactory, SoftDeletes;

    protected $fillable = [
        'client_id',
        'meal_id',
        'food_id',
        'portion_id',
        'custom_name',
        'quantity',
        'unit',
        'grams',
        'calories_snapshot',
        'protein_snapshot',
        'carbs_snapshot',
        'fat_snapshot',
        'fiber_snapshot',
        'sodium_snapshot',
        'sugar_snapshot',
        'nutrient_snapshot_json',
        'source',
        'version',
    ];

    protected function casts(): array
    {
        return [
            'quantity' => 'decimal:2',
            'grams' => 'decimal:2',
            'calories_snapshot' => 'integer',
            'protein_snapshot' => 'decimal:2',
            'carbs_snapshot' => 'decimal:2',
            'fat_snapshot' => 'decimal:2',
            'fiber_snapshot' => 'decimal:2',
            'sodium_snapshot' => 'decimal:2',
            'sugar_snapshot' => 'decimal:2',
            'nutrient_snapshot_json' => 'array',
            'version' => 'integer',
        ];
    }

    public function meal(): BelongsTo
    {
        return $this->belongsTo(Meal::class);
    }

    public function food(): BelongsTo
    {
        return $this->belongsTo(Food::class);
    }

    public function portion(): BelongsTo
    {
        return $this->belongsTo(FoodPortion::class, 'portion_id');
    }
}
