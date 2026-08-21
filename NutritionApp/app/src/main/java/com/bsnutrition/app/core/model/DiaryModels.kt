package com.bsnutrition.app.core.model

data class DailyDiary(
    val id: Long,
    val userId: Long,
    val diaryDate: String,
    val timezone: String,
    val notes: String? = null,
    val summary: DailySummary,
    val meals: List<MealLog> = emptyList()
)

data class MealLog(
    val id: Long,
    val diaryId: Long,
    val mealType: String,
    val name: String,
    val sortOrder: Int,
    val totalCalories: Int,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double,
    val entries: List<FoodLogEntry> = emptyList()
)

data class FoodLogEntry(
    val id: Long,
    val clientId: String? = null,
    val mealId: Long,
    val foodId: Long? = null,
    val portionId: Long? = null,
    val customName: String,
    val quantity: Double,
    val unit: String,
    val grams: Double,
    val caloriesSnapshot: Int,
    val proteinSnapshot: Double,
    val carbsSnapshot: Double,
    val fatSnapshot: Double,
    val fiberSnapshot: Double? = null,
    val sodiumSnapshot: Double? = null,
    val sugarSnapshot: Double? = null,
    val source: String = "catalog",
    val version: Int = 1,
    val food: FoodSummary? = null,
    val portion: FoodPortion? = null
)

data class WaterLog(
    val id: Long,
    val clientId: String? = null,
    val logDate: String,
    val amountMl: Int,
    val occurredAt: String? = null,
    val source: String = "manual",
    val version: Int = 1
)

data class DailySummary(
    val date: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0,
    val waterMl: Int = 0,
    val meals: List<MealSummaryInfo> = emptyList()
)

data class MealSummaryInfo(
    val id: Long,
    val mealType: String,
    val name: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val entriesCount: Int = 0
)
