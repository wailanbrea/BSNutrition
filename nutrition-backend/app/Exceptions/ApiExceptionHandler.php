<?php

namespace App\Exceptions;

use App\Http\Responses\ApiErrorResponse;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Auth\AuthenticationException;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;
use Symfony\Component\HttpKernel\Exception\AccessDeniedHttpException;
use Symfony\Component\HttpKernel\Exception\HttpExceptionInterface;
use Symfony\Component\HttpKernel\Exception\MethodNotAllowedHttpException;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\HttpKernel\Exception\TooManyRequestsHttpException;
use Throwable;

class ApiExceptionHandler
{
    public static function render(Throwable $e, Request $request): ?JsonResponse
    {
        if (! $request->is('api/*') && ! $request->expectsJson()) {
            return null;
        }

        if ($e instanceof ValidationException) {
            return ApiErrorResponse::make(
                code: 'VALIDATION_ERROR',
                message: $e->getMessage(),
                fields: $e->errors(),
                status: 422
            );
        }

        if ($e instanceof AuthenticationException) {
            return ApiErrorResponse::make(
                code: 'UNAUTHENTICATED',
                message: 'Unauthenticated.',
                fields: (object) [],
                status: 401
            );
        }

        if ($e instanceof AuthorizationException || $e instanceof AccessDeniedHttpException) {
            return ApiErrorResponse::make(
                code: 'FORBIDDEN',
                message: $e->getMessage() ?: 'This action is unauthorized.',
                fields: (object) [],
                status: 403
            );
        }

        if ($e instanceof ModelNotFoundException || $e instanceof NotFoundHttpException) {
            return ApiErrorResponse::make(
                code: 'NOT_FOUND',
                message: 'Resource not found.',
                fields: (object) [],
                status: 404
            );
        }

        if ($e instanceof MethodNotAllowedHttpException) {
            return ApiErrorResponse::make(
                code: 'METHOD_NOT_ALLOWED',
                message: 'Method not allowed.',
                fields: (object) [],
                status: 405
            );
        }

        if ($e instanceof TooManyRequestsHttpException) {
            return ApiErrorResponse::make(
                code: 'RATE_LIMITED',
                message: $e->getMessage() ?: 'Too many requests. Please try again later.',
                fields: (object) [],
                status: 429
            );
        }

        if ($e instanceof HttpExceptionInterface) {
            return ApiErrorResponse::make(
                code: 'HTTP_ERROR',
                message: $e->getMessage() ?: 'HTTP error occurred.',
                fields: (object) [],
                status: $e->getStatusCode()
            );
        }

        return ApiErrorResponse::make(
            code: 'SERVER_ERROR',
            message: config('app.debug') ? $e->getMessage() : 'An unexpected server error occurred.',
            fields: (object) [],
            status: 500
        );
    }
}
