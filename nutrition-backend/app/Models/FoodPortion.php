<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class FoodPortion extends Model
{
    use HasFactory;

    protected $fillable = [
        'food_id',
        'portion_name',
        'gram_weight',
        'amount',
        'unit',
        'is_default',
    ];

    protected $casts = [
        'gram_weight' => 'float',
        'amount' => 'float',
        'is_default' => 'boolean',
    ];

    public function food(): BelongsTo
    {
        return $this->belongsTo(Food::class);
    }
}
