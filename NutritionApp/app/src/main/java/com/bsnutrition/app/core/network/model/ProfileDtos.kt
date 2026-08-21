package com.bsnutrition.app.core.network.model

import com.bsnutrition.app.core.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
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
    @SerialName("unit_system") val unitSystem: String? = null
)

@Serializable
data class UserProfileDto(
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
) {
    fun toDomain(): UserProfile = UserProfile(
        userId = userId,
        birthDate = birthDate,
        sex = sex,
        height = height,
        currentWeight = currentWeight,
        activityLevel = activityLevel,
        goalType = goalType,
        goalWeight = goalWeight,
        weeklyGoalRate = weeklyGoalRate,
        locale = locale,
        countryCode = countryCode,
        timezone = timezone,
        unitSystem = unitSystem,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class ProfileContainerDto(
    val profile: UserProfileDto
)
