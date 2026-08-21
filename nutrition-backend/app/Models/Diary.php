<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\HasManyThrough;

class Diary extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'diary_date',
        'timezone',
        'notes',
    ];

    protected function casts(): array
    {
        return [
            'diary_date' => 'date:Y-m-d',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function meals(): HasMany
    {
        return $this->hasMany(Meal::class)->orderBy('sort_order');
    }

    public function entries(): HasManyThrough
    {
        return $this->hasManyThrough(MealEntry::class, Meal::class);
    }
}
