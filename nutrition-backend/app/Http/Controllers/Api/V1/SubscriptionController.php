<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Services\AiQuotaService;
use App\Services\SubscriptionVerificationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SubscriptionController extends Controller
{
    public function __construct(
        private readonly SubscriptionVerificationService $subscriptionService,
        private readonly AiQuotaService $quotaService
    ) {}

    public function status(Request $request): JsonResponse
    {
        $user = $request->user();
        $status = $this->subscriptionService->getSubscriptionStatus($user);
        $quotas = $this->quotaService->getQuotaStatus($user);

        return response()->json([
            'status' => 'success',
            'data' => array_merge($status, ['quotas' => $quotas]),
        ]);
    }

    public function verifyPlayPurchase(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'product_id' => ['required', 'string', 'in:bsnutrition_pro_monthly,bsnutrition_pro_yearly'],
            'purchase_token' => ['required', 'string', 'max:1000'],
            'order_id' => ['nullable', 'string', 'max:100'],
        ]);

        $subscription = $this->subscriptionService->verifyPlayPurchase(
            user: $request->user(),
            productId: $validated['product_id'],
            purchaseToken: $validated['purchase_token'],
            orderId: $validated['order_id'] ?? null
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Suscripción Pro activada exitosamente.',
            'data' => [
                'subscription' => $subscription,
                'status' => $this->subscriptionService->getSubscriptionStatus($request->user()),
            ],
        ]);
    }

    public function quotas(Request $request): JsonResponse
    {
        $quotas = $this->quotaService->getQuotaStatus($request->user());

        return response()->json([
            'status' => 'success',
            'data' => $quotas,
        ]);
    }
}
