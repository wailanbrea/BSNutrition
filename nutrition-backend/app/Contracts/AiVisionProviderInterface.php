<?php

namespace App\Contracts;

use App\DTOs\AiFoodAnalysisResult;

interface AiVisionProviderInterface
{
    /**
     * Analyze a food image and return structured recognized items and nutrition estimates.
     *
     * @param  string  $base64Image  Base64-encoded image data
     * @param  string  $mimeType  MIME type (e.g. image/jpeg, image/png)
     * @param  array<string, mixed>  $context  Optional user context (locale, previous meals, etc.)
     */
    public function analyzeFoodImage(string $base64Image, string $mimeType = 'image/jpeg', array $context = []): AiFoodAnalysisResult;
}
