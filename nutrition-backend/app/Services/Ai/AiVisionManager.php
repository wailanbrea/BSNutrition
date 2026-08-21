<?php

namespace App\Services\Ai;

use App\Contracts\AiVisionProviderInterface;
use InvalidArgumentException;

class AiVisionManager
{
    private array $drivers = [];

    public function driver(?string $name = null): AiVisionProviderInterface
    {
        $driverName = $name ?? (string) config('services.ai.default_vision_driver', env('AI_VISION_DRIVER', 'auto'));

        if ($driverName === 'auto') {
            $openAiKey = config('services.openai.api_key', env('OPENAI_API_KEY'));
            $driverName = ! empty($openAiKey) ? 'openai' : 'mock';
        }

        if (! isset($this->drivers[$driverName])) {
            $this->drivers[$driverName] = $this->createDriver($driverName);
        }

        return $this->drivers[$driverName];
    }

    private function createDriver(string $name): AiVisionProviderInterface
    {
        return match ($name) {
            'openai' => new OpenAiVisionProvider,
            'mock' => new MockVisionProvider,
            default => throw new InvalidArgumentException("Driver de visión IA no soportado: {$name}"),
        };
    }
}
