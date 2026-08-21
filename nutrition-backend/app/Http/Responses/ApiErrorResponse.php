<?php

namespace App\Http\Responses;

use Illuminate\Http\JsonResponse;

class ApiErrorResponse
{
    public static function make(
        string $code,
        string $message,
        mixed $fields = null,
        int $status = 400
    ): JsonResponse {
        $fieldsPayload = $fields;

        if ($fieldsPayload === null || (is_array($fieldsPayload) && empty($fieldsPayload))) {
            $fieldsPayload = (object) [];
        }

        return response()->json([
            'error' => [
                'code' => $code,
                'message' => $message,
                'fields' => $fieldsPayload,
            ],
        ], $status);
    }
}
