<?php

namespace App\Services\Ai;

use App\Contracts\AiVisionProviderInterface;
use App\DTOs\AiFoodAnalysisResult;
use App\DTOs\AiRecognizedFoodItem;
use Illuminate\Support\Facades\Http;
use RuntimeException;

class OpenAiVisionProvider implements AiVisionProviderInterface
{
    private string $apiKey;

    private string $model;

    private int $timeoutSeconds;

    public function __construct(?string $apiKey = null, ?string $model = null, int $timeoutSeconds = 30)
    {
        $this->apiKey = $apiKey ?? (string) config('services.openai.api_key', env('OPENAI_API_KEY', ''));
        $this->model = $model ?? (string) config('services.openai.vision_model', env('OPENAI_VISION_MODEL', 'gpt-4o-mini'));
        $this->timeoutSeconds = $timeoutSeconds;
    }

    /**
     * Analyze food photo using OpenAI Vision models.
     */
    public function analyzeFoodImage(string $base64Image, string $mimeType = 'image/jpeg', array $context = []): AiFoodAnalysisResult
    {
        if (empty($this->apiKey)) {
            throw new RuntimeException('OpenAI API key is missing. Please set OPENAI_API_KEY.');
        }

        $systemPrompt = <<<PROMPT
Eres un nutricionista experto y vision AI especializado en análisis gastronómico y dietético, con profundo conocimiento de la cocina dominicana, caribeña e internacional.
Tu tarea es analizar la imagen del plato de comida proporcionada y desglosarla en sus componentes individuales con estimación de peso en gramos, porción y macronutrientes.

Debes responder ÚNICAMENTE con un objeto JSON válido con la siguiente estructura exacta:
{
  "dish_name": "Nombre general del plato o comida (ej. Mangú con Los Tres Golpes, Bandera Dominicana, Pechuga a la Plancha con Ensalada)",
  "summary": "Breve descripción nutricional en español de los componentes y método de cocción observado",
  "confidence_score": 0.95,
  "items": [
    {
      "name": "Nombre del ingrediente o componente (ej. Mangú de plátano verde, Salami frito, Huevo frito, Queso frito)",
      "estimated_weight_grams": 200.0,
      "portion_description": "1 taza o porción mediana",
      "confidence": 0.92,
      "estimated_calories": 310,
      "estimated_protein_g": 3.2,
      "estimated_carbs_g": 62.0,
      "estimated_fat_g": 6.4,
      "preparation_method": "hervido y majado con mantequilla"
    }
  ]
}
PROMPT;

        $userPrompt = 'Analiza esta imagen y desglosa todos los alimentos visibles con sus pesos aproximados y nutrientes.';
        if (! empty($context['locale'])) {
            $userPrompt .= " Contexto geográfico/cultural: {$context['locale']}.";
        }
        if (! empty($context['meal_type'])) {
            $userPrompt .= " Tipo de comida previsto: {$context['meal_type']}.";
        }

        $imageUrl = "data:{$mimeType};base64,{$base64Image}";

        $response = Http::timeout($this->timeoutSeconds)
            ->withToken($this->apiKey)
            ->post('https://api.openai.com/v1/chat/completions', [
                'model' => $this->model,
                'response_format' => ['type' => 'json_object'],
                'messages' => [
                    [
                        'role' => 'system',
                        'content' => $systemPrompt,
                    ],
                    [
                        'role' => 'user',
                        'content' => [
                            ['type' => 'text', 'text' => $userPrompt],
                            [
                                'type' => 'image_url',
                                'image_url' => [
                                    'url' => $imageUrl,
                                    'detail' => 'high',
                                ],
                            ],
                        ],
                    ],
                ],
                'temperature' => 0.2,
                'max_tokens' => 1500,
            ]);

        if (! $response->successful()) {
            $errorMsg = $response->json('error.message') ?? $response->body();
            throw new RuntimeException("Error en OpenAI Vision API ({$response->status()}): {$errorMsg}");
        }

        $body = $response->json();
        $rawContent = $body['choices'][0]['message']['content'] ?? '{}';
        $decoded = json_decode($rawContent, true);

        if (! is_array($decoded) || empty($decoded['items'])) {
            throw new RuntimeException('Respuesta estructurada inválida recibida del proveedor de IA.');
        }

        $usage = $body['usage'] ?? [];
        $promptTokens = (int) ($usage['prompt_tokens'] ?? 0);
        $completionTokens = (int) ($usage['completion_tokens'] ?? 0);

        // gpt-4o-mini pricing: ~$0.15 / 1M prompt, $0.60 / 1M completion
        $estimatedCost = ($promptTokens * 0.00000015) + ($completionTokens * 0.0000006);

        $decoded['prompt_tokens'] = $promptTokens;
        $decoded['completion_tokens'] = $completionTokens;
        $decoded['estimated_cost_usd'] = $estimatedCost;

        return AiFoodAnalysisResult::fromArray($decoded, 'openai', $this->model);
    }
}
