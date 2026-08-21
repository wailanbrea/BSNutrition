package com.bsnutrition.app.core.network.model

import com.bsnutrition.app.core.model.DailyDiary
import com.bsnutrition.app.core.model.DailySummary
import com.bsnutrition.app.core.model.FoodLogEntry
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.MealSummaryInfo
import com.bsnutrition.app.core.model.WaterLog
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaryDayResponseDto(
    val data: DiaryDayDto
)

@Serializable
data class DiaryDayDto(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("diary_date") val diaryDate: String,
    val timezone: String = "America/Santo_Domingo",
    val notes: String? = null,
    val summary: DailySummaryDto,
    val meals: List<MealDto> = emptyList()
) {
    fun toDomain() = DailyDiary(
        id = id,
        userId = userId,
        diaryDate = diaryDate,
        timezone = timezone,
        notes = notes,
        summary = summary.toDomain(),
        meals = meals.map { it.toDomain() }
    )
}

@Serializable
data class DailySummaryResponseDto(
    val data: DailySummaryDto
)

@Serializable
data class DailySummaryDto(
    val date: String,
    val calories: Int,
    @SerialName("protein_g") val proteinG: Double,
    @SerialName("carbs_g") val carbsG: Double,
    @SerialName("fat_g") val fatG: Double,
    @SerialName("fiber_g") val fiberG: Double = 0.0,
    @SerialName("water_ml") val waterMl: Int = 0,
    val meals: List<MealSummaryInfoDto> = emptyList()
) {
    fun toDomain() = DailySummary(
        date = date,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG,
        waterMl = waterMl,
        meals = meals.map { it.toDomain() }
    )
}

@Serializable
data class MealSummaryInfoDto(
    val id: Long,
    @SerialName("meal_type") val mealType: String,
    val name: String,
    val calories: Int,
    @SerialName("protein_g") val proteinG: Double,
    @SerialName("carbs_g") val carbsG: Double,
    @SerialName("fat_g") val fatG: Double,
    @SerialName("entries_count") val entriesCount: Int = 0
) {
    fun toDomain() = MealSummaryInfo(
        id = id,
        mealType = mealType,
        name = name,
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        entriesCount = entriesCount
    )
}

@Serializable
data class MealResponseDto(
    val data: MealDto
)

@Serializable
data class MealDto(
    val id: Long,
    @SerialName("diary_id") val diaryId: Long,
    @SerialName("meal_type") val mealType: String,
    val name: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("total_calories") val totalCalories: Int = 0,
    @SerialName("total_protein_g") val totalProteinG: Double = 0.0,
    @SerialName("total_carbs_g") val totalCarbsG: Double = 0.0,
    @SerialName("total_fat_g") val totalFatG: Double = 0.0,
    val entries: List<MealEntryDto> = emptyList()
) {
    fun toDomain() = MealLog(
        id = id,
        diaryId = diaryId,
        mealType = mealType,
        name = name,
        sortOrder = sortOrder,
        totalCalories = totalCalories,
        totalProteinG = totalProteinG,
        totalCarbsG = totalCarbsG,
        totalFatG = totalFatG,
        entries = entries.map { it.toDomain() }
    )
}

@Serializable
data class MealEntryResponseDto(
    val data: MealEntryDto
)

@Serializable
data class MealEntryDto(
    val id: Long,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("meal_id") val mealId: Long,
    @SerialName("food_id") val foodId: Long? = null,
    @SerialName("portion_id") val portionId: Long? = null,
    @SerialName("custom_name") val customName: String,
    val quantity: Double,
    val unit: String = "porción",
    val grams: Double = 100.0,
    @SerialName("calories_snapshot") val caloriesSnapshot: Int,
    @SerialName("protein_snapshot") val proteinSnapshot: Double,
    @SerialName("carbs_snapshot") val carbsSnapshot: Double,
    @SerialName("fat_snapshot") val fatSnapshot: Double,
    @SerialName("fiber_snapshot") val fiberSnapshot: Double? = null,
    @SerialName("sodium_snapshot") val sodiumSnapshot: Double? = null,
    @SerialName("sugar_snapshot") val sugarSnapshot: Double? = null,
    val source: String = "catalog",
    val version: Int = 1,
    val food: FoodSummaryDto? = null,
    val portion: FoodPortionDto? = null
) {
    fun toDomain() = FoodLogEntry(
        id = id,
        clientId = clientId,
        mealId = mealId,
        foodId = foodId,
        portionId = portionId,
        customName = customName,
        quantity = quantity,
        unit = unit,
        grams = grams,
        caloriesSnapshot = caloriesSnapshot,
        proteinSnapshot = proteinSnapshot,
        carbsSnapshot = carbsSnapshot,
        fatSnapshot = fatSnapshot,
        fiberSnapshot = fiberSnapshot,
        sodiumSnapshot = sodiumSnapshot,
        sugarSnapshot = sugarSnapshot,
        source = source,
        version = version,
        food = food?.toDomain(),
        portion = portion?.toDomain()
    )
}

@Serializable
data class WaterLogResponseDto(
    val data: WaterLogDto
)

@Serializable
data class WaterLogListResponseDto(
    val data: List<WaterLogDto> = emptyList()
)

@Serializable
data class WaterLogDto(
    val id: Long,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("log_date") val logDate: String,
    @SerialName("amount_ml") val amountMl: Int,
    @SerialName("occurred_at") val occurredAt: String? = null,
    val source: String = "manual",
    val version: Int = 1
) {
    fun toDomain() = WaterLog(
        id = id,
        clientId = clientId,
        logDate = logDate,
        amountMl = amountMl,
        occurredAt = occurredAt,
        source = source,
        version = version
    )
}

@Serializable
data class AddMealEntryRequestDto(
    @SerialName("meal_type") val mealType: String,
    @SerialName("food_id") val foodId: Long? = null,
    @SerialName("portion_id") val portionId: Long? = null,
    @SerialName("custom_name") val customName: String? = null,
    val quantity: Double,
    val unit: String? = null,
    val grams: Double? = null,
    val calories: Int? = null,
    @SerialName("protein_g") val proteinG: Double? = null,
    @SerialName("carbs_g") val carbsG: Double? = null,
    @SerialName("fat_g") val fatG: Double? = null,
    @SerialName("client_id") val clientId: String? = null,
    val source: String? = null
)

@Serializable
data class UpdateMealEntryRequestDto(
    val quantity: Double? = null,
    @SerialName("portion_id") val portionId: Long? = null,
    val unit: String? = null,
    @SerialName("custom_name") val customName: String? = null,
    val calories: Int? = null,
    @SerialName("protein_g") val proteinG: Double? = null,
    @SerialName("carbs_g") val carbsG: Double? = null,
    @SerialName("fat_g") val fatG: Double? = null
)

@Serializable
data class CopyMealRequestDto(
    @SerialName("source_meal_id") val sourceMealId: Long,
    @SerialName("target_date") val targetDate: String,
    @SerialName("target_meal_type") val targetMealType: String
)

@Serializable
data class CopyDayRequestDto(
    @SerialName("source_date") val sourceDate: String,
    @SerialName("target_date") val targetDate: String
)

@Serializable
data class LogWaterRequestDto(
    @SerialName("amount_ml") val amountMl: Int,
    @SerialName("client_id") val clientId: String? = null,
    val source: String? = "manual"
)
