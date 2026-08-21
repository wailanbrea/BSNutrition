<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\AddMealEntryRequest;
use App\Http\Requests\CopyDayRequest;
use App\Http\Requests\CopyMealRequest;
use App\Http\Requests\LogWaterRequest;
use App\Http\Requests\UpdateMealEntryRequest;
use App\Http\Resources\DailySummaryResource;
use App\Http\Resources\DiaryDayResource;
use App\Http\Resources\MealEntryResource;
use App\Http\Resources\MealResource;
use App\Http\Resources\WaterLogResource;
use App\Models\WaterLog;
use App\Services\DiaryService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;

class DiaryController extends Controller
{
    public function __construct(
        protected DiaryService $diaryService
    ) {}

    /**
     * Get or create daily diary with all meals, entries, and summary totals for a given date.
     */
    public function show(Request $request, string $date): JsonResponse
    {
        $diary = $this->diaryService->getOrCreateDiaryForDate($request->user(), $date);

        return (new DiaryDayResource($diary))->response()->setStatusCode(200);
    }

    /**
     * Add a food entry to a meal for a specific date.
     */
    public function addEntry(AddMealEntryRequest $request, string $date): JsonResponse
    {
        $entry = $this->diaryService->addEntry($request->user(), $date, $request->validated());

        return (new MealEntryResource($entry->load(['food.brand', 'food.category', 'portion'])))
            ->response()
            ->setStatusCode(201);
    }

    /**
     * Update an existing meal entry (quantity, portion, custom name, etc.).
     */
    public function updateEntry(UpdateMealEntryRequest $request, int $id): MealEntryResource
    {
        $entry = $this->diaryService->updateEntry($request->user(), $id, $request->validated());

        return new MealEntryResource($entry->load(['food.brand', 'food.category', 'portion']));
    }

    /**
     * Delete a meal entry.
     */
    public function deleteEntry(Request $request, int $id): JsonResponse
    {
        $this->diaryService->deleteEntry($request->user(), $id);

        return response()->json([
            'message' => 'Entrada eliminada correctamente.',
        ]);
    }

    /**
     * Copy an entire meal to another date or meal section.
     */
    public function copyMeal(CopyMealRequest $request): MealResource
    {
        $meal = $this->diaryService->copyMeal(
            user: $request->user(),
            sourceMealId: $request->validated('source_meal_id'),
            targetDate: $request->validated('target_date'),
            targetMealType: $request->validated('target_meal_type')
        );

        return new MealResource($meal->load('entries'));
    }

    /**
     * Copy an entire day's meals and entries to another date.
     */
    public function copyDay(CopyDayRequest $request): JsonResponse
    {
        $diary = $this->diaryService->copyDay(
            user: $request->user(),
            sourceDate: $request->validated('source_date'),
            targetDate: $request->validated('target_date')
        );

        return (new DiaryDayResource($diary))->response()->setStatusCode(200);
    }

    /**
     * Get water logs for a specific date.
     */
    public function water(Request $request, string $date): AnonymousResourceCollection
    {
        $logs = WaterLog::where('user_id', $request->user()->id)
            ->where('log_date', $date)
            ->orderBy('occurred_at')
            ->get();

        return WaterLogResource::collection($logs);
    }

    /**
     * Log water consumption for a date.
     */
    public function logWater(LogWaterRequest $request, string $date): JsonResponse
    {
        $log = $this->diaryService->logWater(
            user: $request->user(),
            date: $date,
            amountMl: $request->validated('amount_ml'),
            clientId: $request->validated('client_id'),
            source: $request->validated('source', 'manual')
        );

        return (new WaterLogResource($log))
            ->response()
            ->setStatusCode(201);
    }

    /**
     * Delete a water log entry.
     */
    public function deleteWater(Request $request, int $id): JsonResponse
    {
        $this->diaryService->deleteWaterLog($request->user(), $id);

        return response()->json([
            'message' => 'Registro de agua eliminado.',
        ]);
    }

    /**
     * Get aggregate nutritional and macro summary for a date.
     */
    public function summary(Request $request, string $date): DailySummaryResource
    {
        $summary = $this->diaryService->getDailySummary($request->user(), $date);

        return new DailySummaryResource($summary);
    }
}
