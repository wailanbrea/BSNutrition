<?php

namespace App\Models;

// use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\HasOne;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'name',
        'email',
        'password',
        'role',
    ];


    /**
     * The attributes that should be hidden for serialization.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }

    public function profile(): HasOne
    {
        return $this->hasOne(UserProfile::class);
    }

    public function nutritionGoals(): HasMany
    {
        return $this->hasMany(NutritionGoal::class);
    }

    public function currentNutritionGoal(): HasOne
    {
        return $this->hasOne(NutritionGoal::class)->latestOfMany('effective_from');
    }

    public function favoriteFoods(): BelongsToMany
    {
        return $this->belongsToMany(Food::class, 'user_food_favorites')->withTimestamps();
    }

    public function recentFoods(): BelongsToMany
    {
        return $this->belongsToMany(Food::class, 'user_food_recents')
            ->withPivot(['use_count', 'last_used_at'])
            ->withTimestamps();
    }

    public function diaries(): HasMany
    {
        return $this->hasMany(Diary::class);
    }

    public function waterLogs(): HasMany
    {
        return $this->hasMany(WaterLog::class);
    }

    public function subscriptions(): HasMany
    {
        return $this->hasMany(UserSubscription::class);
    }

    public function activeSubscription(): HasOne
    {
        return $this->hasOne(UserSubscription::class)
            ->where('status', 'active')
            ->where(function ($q) {
                $q->whereNull('expires_at')->orWhere('expires_at', '>', now());
            })
            ->latestOfMany('starts_at');
    }

    public function isPro(): bool
    {
        if ($this->isAdmin()) {
            return true;
        }

        return $this->activeSubscription()->exists();
    }

    public function isAdmin(): bool
    {
        return $this->role === 'admin';
    }


    public function isCurator(): bool
    {
        return in_array($this->role, ['admin', 'curator']);
    }

    public function hasAnyRole(array $roles): bool
    {
        return in_array($this->role, $roles);
    }
}



