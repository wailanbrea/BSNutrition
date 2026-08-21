<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class NutritionGoal extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'effective_from',
        'calorie_target',
        'protein_target_g',
        'carbohydrate_target_g',
        'fat_target_g',
        'fiber_target_g',
        'water_target_ml',
        'source',
        'calculation_version',
    ];

    protected $casts = [
        'effective_from' => 'date',
        'calorie_target' => 'integer',
        'protein_target_g' => 'float',
        'carbohydrate_target_g' => 'float',
        'fat_target_g' => 'float',
        'fiber_target_g' => 'float',
        'water_target_ml' => 'integer',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
