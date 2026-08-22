<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class UserDailyAiQuota extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'quota_date',
        'photo_analyses_count',
        'text_parses_count',
    ];

    protected function casts(): array
    {
        return [
            'photo_analyses_count' => 'integer',
            'text_parses_count' => 'integer',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
