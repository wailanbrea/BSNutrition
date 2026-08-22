<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class AiPhotoAnalysisItem extends Model
{
    use HasFactory;

    protected $fillable = [
        'analysis_id',
        'food_id',
        'name',
        'matched_name',
        'estimated_weight_grams',
        'portion_description',
        'preparation_method',
        'confidence',
        'calories',
        'protein_g',
        'carbs_g',
        'fat_g',
        'candidates',
    ];

    protected $casts = [
        'estimated_weight_grams' => 'float',
        'confidence' => 'float',
        'calories' => 'integer',
        'protein_g' => 'float',
        'carbs_g' => 'float',
        'fat_g' => 'float',
        'candidates' => 'array',
    ];

    public function analysis(): BelongsTo
    {
        return $this->belongsTo(AiPhotoAnalysis::class, 'analysis_id');
    }

    public function food(): BelongsTo
    {
        return $this->belongsTo(Food::class, 'food_id');
    }
}
