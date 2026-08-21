package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.DailyDiary
import com.bsnutrition.app.core.model.DailySummary
import com.bsnutrition.app.core.model.FoodLogEntry
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.WaterLog

interface DiaryRepository {

    suspend fun getDiaryDay(date: String): Result<DailyDiary>

    suspend fun addMealEntry(
        date: String,
        mealType: String,
        foodId: Long? = null,
        portionId: Long? = null,
        quantity: Double,
        unit: String? = null,
        customName: String? = null,
        calories: Int? = null,
        proteinG: Double? = null,
        carbsG: Double? = null,
        fatG: Double? = null,
        clientId: String? = null,
        source: String? = null
    ): Result<FoodLogEntry>

    suspend fun updateMealEntry(
        id: Long,
        quantity: Double? = null,
        portionId: Long? = null,
        unit: String? = null,
        customName: String? = null,
        calories: Int? = null,
        proteinG: Double? = null,
        carbsG: Double? = null,
        fatG: Double? = null
    ): Result<FoodLogEntry>

    suspend fun deleteMealEntry(id: Long): Result<Unit>

    suspend fun copyMeal(
        sourceMealId: Long,
        targetDate: String,
        targetMealType: String
    ): Result<MealLog>

    suspend fun copyDay(
        sourceDate: String,
        targetDate: String
    ): Result<DailyDiary>

    suspend fun getWaterLogs(date: String): Result<List<WaterLog>>

    suspend fun logWater(
        date: String,
        amountMl: Int,
        clientId: String? = null,
        source: String? = "manual"
    ): Result<WaterLog>

    suspend fun deleteWaterLog(id: Long): Result<Unit>

    suspend fun getDailySummary(date: String): Result<DailySummary>
}
