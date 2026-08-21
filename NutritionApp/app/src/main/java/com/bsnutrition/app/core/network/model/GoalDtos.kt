package com.bsnutrition.app.core.network.model

import com.bsnutrition.app.core.model.NutritionGoal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalculateGoalRequestDto(
    @SerialName("birth_date") val birthDate: String? = null,
    val sex: String? = null,
    val height: Float? = null,
    @SerialName("current_weight") val currentWeight: Float? = null,
    @SerialName("activity_level") val activityLevel: String? = null,
    @SerialName("goal_type") val goalType: String? = null,
    @SerialName("weekly_goal_rate") val weeklyGoalRate: Float? = null
)

@Serializable
data class CalculatedGoalDto(
    val bmr: Float,
    val tdee: Float,
    @SerialName("calorie_target") val calorieTarget: Int,
    @SerialName("protein_target_g") val proteinTargetG: Float,
    @SerialName("carbohydrate_target_g") val carbohydrateTargetG: Float,
    @SerialName("fat_target_g") val fatTargetG: Float,
    @SerialName("fiber_target_g") val fiberTargetG: Float? = null,
    @SerialName("water_target_ml") val waterTargetMl: Int? = null,
    @SerialName("calculation_version") val calculationVersion: String = "mifflin_v1.0"
) {
    fun toDomain(): NutritionGoal = NutritionGoal(
        calorieTarget = calorieTarget,
        proteinTargetG = proteinTargetG,
        carbohydrateTargetG = carbohydrateTargetG,
        fatTargetG = fatTargetG,
        fiberTargetG = fiberTargetG,
        waterTargetMl = waterTargetMl,
        source = "calculated",
        calculationVersion = calculationVersion
    )
}

@Serializable
data class CalculatedGoalResponseDto(
    @SerialName("calculated_goal") val calculatedGoal: CalculatedGoalDto
)

@Serializable
data class SaveGoalRequestDto(
    @SerialName("effective_from") val effectiveFrom: String? = null,
    @SerialName("calorie_target") val calorieTarget: Int,
    @SerialName("protein_target_g") val proteinTargetG: Float,
    @SerialName("carbohydrate_target_g") val carbohydrateTargetG: Float,
    @SerialName("fat_target_g") val fatTargetG: Float,
    @SerialName("fiber_target_g") val fiberTargetG: Float? = null,
    @SerialName("water_target_ml") val waterTargetMl: Int? = null,
    val source: String = "calculated"
)

@Serializable
data class NutritionGoalDto(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("effective_from") val effectiveFrom: String? = null,
    @SerialName("calorie_target") val calorieTarget: Int,
    @SerialName("protein_target_g") val proteinTargetG: Float,
    @SerialName("carbohydrate_target_g") val carbohydrateTargetG: Float,
    @SerialName("fat_target_g") val fatTargetG: Float,
    @SerialName("fiber_target_g") val fiberTargetG: Float? = null,
    @SerialName("water_target_ml") val waterTargetMl: Int? = null,
    val source: String = "calculated",
    @SerialName("calculation_version") val calculationVersion: String = "mifflin_v1.0"
) {
    fun toDomain(): NutritionGoal = NutritionGoal(
        id = id,
        userId = userId,
        effectiveFrom = effectiveFrom,
        calorieTarget = calorieTarget,
        proteinTargetG = proteinTargetG,
        carbohydrateTargetG = carbohydrateTargetG,
        fatTargetG = fatTargetG,
        fiberTargetG = fiberTargetG,
        waterTargetMl = waterTargetMl,
        source = source,
        calculationVersion = calculationVersion
    )
}

@Serializable
data class GoalContainerDto(
    val goal: NutritionGoalDto,
    val message: String? = null
)
