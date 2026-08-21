package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.BsnDispatchers
import com.bsnutrition.app.core.common.Dispatcher
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.DailyDiary
import com.bsnutrition.app.core.model.DailySummary
import com.bsnutrition.app.core.model.FoodLogEntry
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.WaterLog
import com.bsnutrition.app.core.network.api.DiaryApiService
import com.bsnutrition.app.core.network.model.AddMealEntryRequestDto
import com.bsnutrition.app.core.network.model.CopyDayRequestDto
import com.bsnutrition.app.core.network.model.CopyMealRequestDto
import com.bsnutrition.app.core.network.model.LogWaterRequestDto
import com.bsnutrition.app.core.network.model.UpdateMealEntryRequestDto
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryApiService: DiaryApiService,
    @Dispatcher(BsnDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : DiaryRepository {

    override suspend fun getDiaryDay(date: String): Result<DailyDiary> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.getDiaryDay(date)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun addMealEntry(
        date: String,
        mealType: String,
        foodId: Long?,
        portionId: Long?,
        quantity: Double,
        unit: String?,
        customName: String?,
        calories: Int?,
        proteinG: Double?,
        carbsG: Double?,
        fatG: Double?,
        clientId: String?,
        source: String?
    ): Result<FoodLogEntry> {
        val request = AddMealEntryRequestDto(
            mealType = mealType,
            foodId = foodId,
            portionId = portionId,
            customName = customName,
            quantity = quantity,
            unit = unit,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            clientId = clientId,
            source = source
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.addMealEntry(date, request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun updateMealEntry(
        id: Long,
        quantity: Double?,
        portionId: Long?,
        unit: String?,
        customName: String?,
        calories: Int?,
        proteinG: Double?,
        carbsG: Double?,
        fatG: Double?
    ): Result<FoodLogEntry> {
        val request = UpdateMealEntryRequestDto(
            quantity = quantity,
            portionId = portionId,
            unit = unit,
            customName = customName,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.updateMealEntry(id, request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun deleteMealEntry(id: Long): Result<Unit> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.deleteMealEntry(id)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun copyMeal(
        sourceMealId: Long,
        targetDate: String,
        targetMealType: String
    ): Result<MealLog> {
        val request = CopyMealRequestDto(
            sourceMealId = sourceMealId,
            targetDate = targetDate,
            targetMealType = targetMealType
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.copyMeal(request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun copyDay(
        sourceDate: String,
        targetDate: String
    ): Result<DailyDiary> {
        val request = CopyDayRequestDto(
            sourceDate = sourceDate,
            targetDate = targetDate
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.copyDay(request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun getWaterLogs(date: String): Result<List<WaterLog>> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.getWaterLogs(date)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.map { it.toDomain() })
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun logWater(
        date: String,
        amountMl: Int,
        clientId: String?,
        source: String?
    ): Result<WaterLog> {
        val request = LogWaterRequestDto(
            amountMl = amountMl,
            clientId = clientId,
            source = source ?: "manual"
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.logWater(date, request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun deleteWaterLog(id: Long): Result<Unit> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.deleteWaterLog(id)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun getDailySummary(date: String): Result<DailySummary> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.getDailySummary(date)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }
}
