<?php

namespace App\Exceptions;

use Exception;
use Illuminate\Http\JsonResponse;

class QuotaExceededException extends Exception
{
    public function __construct(
        string $message = 'Has alcanzado el límite diario de análisis con IA para la cuenta gratuita.',
        public readonly string $feature = 'ai_photo',
        public readonly int $limit = 3,
        public readonly int $used = 3
    ) {
        parent::__construct($message, 429);
    }

    public function render(): JsonResponse
    {
        return response()->json([
            'status' => 'error',
            'code' => 'AI_QUOTA_EXCEEDED',
            'message' => $this->getMessage(),
            'data' => [
                'feature' => $this->feature,
                'limit' => $this->limit,
                'used' => $this->used,
                'is_pro' => false,
                'upgrade_required' => true,
            ],
        ], 429);
    }
}
