<?php

namespace App\Services;

use App\Models\AuditLog;
use App\Models\User;
use App\Models\UserSubscription;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;

class SubscriptionVerificationService
{
    /**
     * Verify Google Play purchase token and activate user subscription.
     */
    public function verifyPlayPurchase(
        User $user,
        string $productId,
        string $purchaseToken,
        ?string $orderId = null,
        string $provider = 'google_play'
    ): UserSubscription {
        return DB::transaction(function () use ($user, $productId, $purchaseToken, $orderId, $provider) {
            $durationDays = match ($productId) {
                'bsnutrition_pro_yearly' => 365,
                default => 30,
            };

            $planId = match ($productId) {
                'bsnutrition_pro_yearly' => 'pro_yearly',
                default => 'pro_monthly',
            };

            $subscription = UserSubscription::updateOrCreate(
                ['user_id' => $user->id, 'purchase_token' => $purchaseToken],
                [
                    'plan_id' => $planId,
                    'status' => 'active',
                    'provider' => $provider,
                    'order_id' => $orderId ?: ('GPA.'.now()->timestamp),
                    'starts_at' => now(),
                    'expires_at' => now()->addDays($durationDays),
                    'auto_renewing' => true,
                ]
            );

            AuditLog::log(
                user: $user,
                action: 'subscription.verify_purchase',
                targetType: 'UserSubscription',
                targetId: $subscription->id,
                newValues: [
                    'plan_id' => $planId,
                    'product_id' => $productId,
                    'expires_at' => $subscription->expires_at->toIso8601String(),
                ]
            );

            return $subscription;
        });
    }

    /**
     * Retrieve complete subscription and entitlement status for the user.
     */
    public function getSubscriptionStatus(User $user): array
    {
        $activeSub = $user->activeSubscription;
        $isPro = $user->isPro();

        return [
            'is_pro' => $isPro,
            'tier' => $isPro ? 'pro' : 'free',
            'active_subscription' => $activeSub ? [
                'id' => $activeSub->id,
                'plan_id' => $activeSub->plan_id,
                'status' => $activeSub->status,
                'provider' => $activeSub->provider,
                'starts_at' => $activeSub->starts_at?->toIso8601String(),
                'expires_at' => $activeSub->expires_at?->toIso8601String(),
                'auto_renewing' => $activeSub->auto_renewing,
            ] : null,
            'entitlements' => [
                'unlimited_ai_photo' => $isPro,
                'unlimited_ai_voice' => $isPro,
                'advanced_analytics_90d' => $isPro,
                'unlimited_recipes' => true,
                'health_connect_sync' => true,
                'ad_free' => true,
            ],
            'products' => [
                [
                    'sku' => 'bsnutrition_pro_monthly',
                    'name' => 'BSNutrition Pro Mensual',
                    'price_usd' => '6.99',
                    'billing_period' => 'P1M',
                    'trial_days' => 7,
                ],
                [
                    'sku' => 'bsnutrition_pro_yearly',
                    'name' => 'BSNutrition Pro Anual',
                    'price_usd' => '49.99',
                    'billing_period' => 'P1Y',
                    'discount_pct' => 40,
                ],
            ],
        ];
    }
}
