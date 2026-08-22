<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\SoftDeletes;

class WeightLog extends Model
{
    use HasFactory, SoftDeletes;

    protected $fillable = [
        'client_id',
        'user_id',
        'log_date',
        'weight_kg',
        'occurred_at',
        'source',
        'notes',
        'version',
    ];

    protected function casts(): array
    {
        return [
            'log_date' => 'date:Y-m-d',
            'weight_kg' => 'decimal:2',
            'occurred_at' => 'datetime',
            'version' => 'integer',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
