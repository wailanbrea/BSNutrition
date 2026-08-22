<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class AiPhotoAnalysis extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'upload_id',
        'status',
        'dish_name',
        'summary',
        'confidence_score',
        'provider',
        'model',
        'prompt_tokens',
        'completion_tokens',
        'estimated_cost_usd',
        'total_calories',
        'total_protein_g',
        'total_carbs_g',
        'total_fat_g',
        'context',
    ];

    protected $casts = [
        'confidence_score' => 'float',
        'estimated_cost_usd' => 'float',
        'total_calories' => 'integer',
        'total_protein_g' => 'float',
        'total_carbs_g' => 'float',
        'total_fat_g' => 'float',
        'prompt_tokens' => 'integer',
        'completion_tokens' => 'integer',
        'context' => 'array',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function upload(): BelongsTo
    {
        return $this->belongsTo(AiImageUpload::class, 'upload_id');
    }

    public function items(): HasMany
    {
        return $this->hasMany(AiPhotoAnalysisItem::class, 'analysis_id');
    }
}
