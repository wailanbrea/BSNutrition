package com.bsnutrition.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("user_id") val userId: Long,
    @SerialName("birth_date") val birthDate: String? = null,
    val sex: String? = null,
    val height: Float? = null,
    @SerialName("current_weight") val currentWeight: Float? = null,
    @SerialName("activity_level") val activityLevel: String? = null,
    @SerialName("goal_type") val goalType: String? = null,
    @SerialName("goal_weight") val goalWeight: Float? = null,
    @SerialName("weekly_goal_rate") val weeklyGoalRate: Float? = null,
    val locale: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val timezone: String? = null,
    @SerialName("unit_system") val unitSystem: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
