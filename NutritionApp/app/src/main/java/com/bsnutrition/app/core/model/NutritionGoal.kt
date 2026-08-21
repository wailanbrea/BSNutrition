package com.bsnutrition.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NutritionGoal(
    val id: Long = 0,
    @SerialName("user_id") val userId: Long = 0,
    @SerialName("effective_from") val effectiveFrom: String? = null,
    @SerialName("calorie_target") val calorieTarget: Int,
    @SerialName("protein_target_g") val proteinTargetG: Float,
    @SerialName("carbohydrate_target_g") val carbohydrateTargetG: Float,
    @SerialName("fat_target_g") val fatTargetG: Float,
    @SerialName("fiber_target_g") val fiberTargetG: Float? = null,
    @SerialName("water_target_ml") val waterTargetMl: Int? = null,
    val source: String = "calculated",
    @SerialName("calculation_version") val calculationVersion: String = "mifflin_v1.0"
)
