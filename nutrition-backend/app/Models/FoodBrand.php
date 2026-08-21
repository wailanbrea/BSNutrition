<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class FoodBrand extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'normalized_name',
        'country_code',
    ];

    public function foods(): HasMany
    {
        return $this->hasMany(Food::class, 'brand_id');
    }
}
