<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class FoodAlias extends Model
{
    use HasFactory;

    protected $fillable = [
        'food_id',
        'alias',
        'normalized_alias',
        'language',
    ];

    public function food(): BelongsTo
    {
        return $this->belongsTo(Food::class);
    }
}
