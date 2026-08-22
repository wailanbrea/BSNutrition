<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;

class Recipe extends Model
{
    use HasFactory, SoftDeletes;

    protected $fillable = [
        'user_id',
        'name',
        'description',
        'servings',
        'prep_time_minutes',
        'cook_time_minutes',
        'total_weight_grams',
        'calories_per_serving',
        'protein_per_serving_g',
        'carbs_per_serving_g',
        'fat_per_serving_g',
        'fiber_per_serving_g',
        'is_public',
        'image_path',
    ];

    protected function casts(): array
    {
        return [
            'servings' => 'integer',
            'prep_time_minutes' => 'integer',
            'cook_time_minutes' => 'integer',
            'total_weight_grams' => 'decimal:2',
            'calories_per_serving' => 'integer',
            'protein_per_serving_g' => 'decimal:2',
            'carbs_per_serving_g' => 'decimal:2',
            'fat_per_serving_g' => 'decimal:2',
            'fiber_per_serving_g' => 'decimal:2',
            'is_public' => 'boolean',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function ingredients(): HasMany
    {
        return $this->hasMany(RecipeIngredient::class)->orderBy('sort_order', 'asc');
    }

    public function steps(): HasMany
    {
        return $this->hasMany(RecipeStep::class)->orderBy('step_number', 'asc');
    }
}
