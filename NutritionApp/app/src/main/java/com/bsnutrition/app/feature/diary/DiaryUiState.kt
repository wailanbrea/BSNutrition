package com.bsnutrition.app.feature.diary

import com.bsnutrition.app.core.model.DailyDiary
import com.bsnutrition.app.core.model.FoodLogEntry
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.NutritionGoal
import com.bsnutrition.app.core.model.WaterLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val diary: DailyDiary? = null,
    val goal: NutritionGoal? = null,
    val waterLogs: List<WaterLog> = emptyList(),
    val isLoading: Boolean = false,
    val isLoggingWater: Boolean = false,
    val error: String? = null,
    val userMessage: String? = null,
    val showDatePickerDialog: Boolean = false,
    val showCopyDayDialog: Boolean = false,
    val showCopyMealDialog: Boolean = false,
    val mealToCopy: MealLog? = null,
    val editingEntry: FoodLogEntry? = null
) {
    val formattedDate: String
        get() = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    val displayDate: String
        get() = when (selectedDate) {
            LocalDate.now() -> "Hoy"
            LocalDate.now().minusDays(1) -> "Ayer"
            LocalDate.now().plusDays(1) -> "Mañana"
            else -> selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
        }

    val totalCaloriesConsumed: Int
        get() = diary?.summary?.calories ?: 0

    val targetCalories: Int
        get() = goal?.targetCalories ?: 2000

    val remainingCalories: Int
        get() = targetCalories - totalCaloriesConsumed

    val calorieProgress: Float
        get() = if (targetCalories > 0) (totalCaloriesConsumed.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1.5f) else 0f

    val totalProteinG: Double
        get() = diary?.summary?.proteinG ?: 0.0

    val targetProteinG: Double
        get() = goal?.targetProteinG ?: 150.0

    val totalCarbsG: Double
        get() = diary?.summary?.carbsG ?: 0.0

    val targetCarbsG: Double
        get() = goal?.targetCarbsG ?: 200.0

    val totalFatG: Double
        get() = diary?.summary?.fatG ?: 0.0

    val targetFatG: Double
        get() = goal?.targetFatG ?: 65.0

    val totalWaterMl: Int
        get() = diary?.summary?.waterMl ?: waterLogs.sumOf { it.amountMl }
}
