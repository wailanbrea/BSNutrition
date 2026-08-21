<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class FoodBarcode extends Model
{
    use HasFactory;

    protected $fillable = [
        'food_id',
        'barcode',
        'barcode_type',
        'is_primary',
    ];

    protected $casts = [
        'is_primary' => 'boolean',
    ];

    public function food(): BelongsTo
    {
        return $this->belongsTo(Food::class);
    }
}
