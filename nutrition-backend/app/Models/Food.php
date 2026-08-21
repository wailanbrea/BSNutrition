<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;

class Food extends Model
{
    use HasFactory, SoftDeletes;

    protected $table = 'foods';

    protected $fillable = [
        'canonical_name',
        'normalized_name',
        'brand_id',
        'category_id',
        'user_id',
        'country_code',
        'language',
        'verified',
        'source',
        'external_source_id',
        'default_basis_amount',
        'default_basis_unit',
    ];

    protected $casts = [
        'verified' => 'boolean',
        'default_basis_amount' => 'float',
    ];

    public function brand(): BelongsTo
    {
        return $this->belongsTo(FoodBrand::class, 'brand_id');
    }

    public function category(): BelongsTo
    {
        return $this->belongsTo(FoodCategory::class, 'category_id');
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function aliases(): HasMany
    {
        return $this->hasMany(FoodAlias::class);
    }

    public function barcodes(): HasMany
    {
        return $this->hasMany(FoodBarcode::class);
    }

    public function portions(): HasMany
    {
        return $this->hasMany(FoodPortion::class);
    }

    public function foodNutrients(): HasMany
    {
        return $this->hasMany(FoodNutrient::class);
    }

    public function nutrients(): BelongsToMany
    {
        return $this->belongsToMany(Nutrient::class, 'food_nutrients')
            ->withPivot(['amount', 'basis_amount', 'basis_unit', 'source'])
            ->withTimestamps();
    }

    public function favoritedByUsers(): BelongsToMany
    {
        return $this->belongsToMany(User::class, 'user_food_favorites')
            ->withTimestamps();
    }

    public function scopeVerified(Builder $query): Builder
    {
        return $query->where('verified', true);
    }

    public function scopeByBarcode(Builder $query, string $barcode): Builder
    {
        return $query->whereHas('barcodes', function (Builder $bQuery) use ($barcode) {
            $bQuery->where('barcode', $barcode);
        });
    }

    public function scopeSearch(Builder $query, string $term): Builder
    {
        $normalized = mb_strtolower(trim($term), 'UTF-8');

        return $query->where(function (Builder $sub) use ($normalized) {
            $sub->where('normalized_name', 'LIKE', "%{$normalized}%")
                ->orWhereHas('aliases', function (Builder $aQuery) use ($normalized) {
                    $aQuery->where('normalized_alias', 'LIKE', "%{$normalized}%");
                })
                ->orWhereHas('brand', function (Builder $bQuery) use ($normalized) {
                    $bQuery->where('normalized_name', 'LIKE', "%{$normalized}%");
                });
        });
    }
}
