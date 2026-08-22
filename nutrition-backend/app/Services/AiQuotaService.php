<?php

namespace App\Services;

use App\Exceptions\QuotaExceededException;
use App\Models\User;
use App\Models\UserDailyAiQuota;
use Illuminate\Support\Facades\DB;

class AiQuotaService
{
    public const PHOTO_DAILY_LIMIT = 3;

    public const TEXT_DAILY_LIMIT = 5;

    /**
     * Atomically check and consume 1 photo analysis quota for the user.
     *
     * @throws QuotaExceededException
     */
    public function checkAndConsumePhotoQuota(User $user): array
    {
        if ($user->isPro()) {
            return [
                'is_pro' => true,
                'unlimited' => true,
                'remaining' => -1,
                'limit' => -1,
            ];
        }

        return DB::transaction(function () use ($user) {
            $today = now()->toDateString();

            $quota = UserDailyAiQuota::lockForUpdate()->firstOrCreate(
                ['user_id' => $user->id, 'quota_date' => $today],
                ['photo_analyses_count' => 0, 'text_parses_count' => 0]
            );

            if ($quota->photo_analyses_count >= self::PHOTO_DAILY_LIMIT) {
                throw new QuotaExceededException(
                    message: 'Has alcanzado el límite diario de fotos ('.self::PHOTO_DAILY_LIMIT.'/día). Actualiza a Pro para análisis ilimitados.',
                    feature: 'ai_photo',
                    limit: self::PHOTO_DAILY_LIMIT,
                    used: $quota->photo_analyses_count
                );
            }

            $quota->increment('photo_analyses_count');
            $used = $quota->photo_analyses_count;

            return [
                'is_pro' => false,
                'unlimited' => false,
                'used' => $used,
                'remaining' => max(0, self::PHOTO_DAILY_LIMIT - $used),
                'limit' => self::PHOTO_DAILY_LIMIT,
            ];
        });
    }

    /**
     * Atomically check and consume 1 text/voice analysis quota for the user.
     *
     * @throws QuotaExceededException
     */
    public function checkAndConsumeTextQuota(User $user): array
    {
        if ($user->isPro()) {
            return [
                'is_pro' => true,
                'unlimited' => true,
                'remaining' => -1,
                'limit' => -1,
            ];
        }

        return DB::transaction(function () use ($user) {
            $today = now()->toDateString();

            $quota = UserDailyAiQuota::lockForUpdate()->firstOrCreate(
                ['user_id' => $user->id, 'quota_date' => $today],
                ['photo_analyses_count' => 0, 'text_parses_count' => 0]
            );

            if ($quota->text_parses_count >= self::TEXT_DAILY_LIMIT) {
                throw new QuotaExceededException(
                    message: 'Has alcanzado el límite diario de dictado/texto ('.self::TEXT_DAILY_LIMIT.'/día). Actualiza a Pro para análisis ilimitados.',
                    feature: 'ai_text',
                    limit: self::TEXT_DAILY_LIMIT,
                    used: $quota->text_parses_count
                );
            }

            $quota->increment('text_parses_count');
            $used = $quota->text_parses_count;

            return [
                'is_pro' => false,
                'unlimited' => false,
                'used' => $used,
                'remaining' => max(0, self::TEXT_DAILY_LIMIT - $used),
                'limit' => self::TEXT_DAILY_LIMIT,
            ];
        });
    }

    /**
     * Get current status of all AI daily quotas for user.
     */
    public function getQuotaStatus(User $user): array
    {
        $isPro = $user->isPro();

        if ($isPro) {
            return [
                'is_pro' => true,
                'plan' => 'pro',
                'photo_analyses' => ['unlimited' => true, 'used' => 0, 'remaining' => -1, 'limit' => -1],
                'text_parses' => ['unlimited' => true, 'used' => 0, 'remaining' => -1, 'limit' => -1],
            ];
        }

        $today = now()->toDateString();
        $quota = UserDailyAiQuota::firstOrCreate(
            ['user_id' => $user->id, 'quota_date' => $today],
            ['photo_analyses_count' => 0, 'text_parses_count' => 0]
        );

        return [
            'is_pro' => false,
            'plan' => 'free',
            'photo_analyses' => [
                'unlimited' => false,
                'used' => $quota->photo_analyses_count,
                'remaining' => max(0, self::PHOTO_DAILY_LIMIT - $quota->photo_analyses_count),
                'limit' => self::PHOTO_DAILY_LIMIT,
            ],
            'text_parses' => [
                'unlimited' => false,
                'used' => $quota->text_parses_count,
                'remaining' => max(0, self::TEXT_DAILY_LIMIT - $quota->text_parses_count),
                'limit' => self::TEXT_DAILY_LIMIT,
            ],
        ];
    }
}
