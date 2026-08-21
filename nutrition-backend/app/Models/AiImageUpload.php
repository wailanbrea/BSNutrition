<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\SoftDeletes;

class AiImageUpload extends Model
{
    use HasFactory, SoftDeletes;

    protected $fillable = [
        'user_id',
        'disk',
        'path',
        'original_name',
        'mime_type',
        'file_size_bytes',
        'status',
        'analysis_metadata',
        'retention_hours',
        'expires_at',
    ];

    protected $casts = [
        'analysis_metadata' => 'array',
        'expires_at' => 'datetime',
        'file_size_bytes' => 'integer',
        'retention_hours' => 'integer',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
