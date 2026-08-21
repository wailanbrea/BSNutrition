<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class UserProfile extends Model
{
    use HasFactory;

    protected $table = 'user_profiles';

    protected $fillable = [
        'user_id',
        'birth_date',
        'sex',
        'height',
        'current_weight',
        'activity_level',
        'goal_type',
        'goal_weight',
        'weekly_goal_rate',
        'locale',
        'country_code',
        'timezone',
        'unit_system',
    ];

    /**
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'birth_date' => 'date',
            'height' => 'float',
            'current_weight' => 'float',
            'goal_weight' => 'float',
            'weekly_goal_rate' => 'float',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
