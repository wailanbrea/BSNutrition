package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.NutritionGoal

interface GoalRepository {
    suspend fun calculateGoal(
        birthDate: String?,
        sex: String?,
        height: Float?,
        currentWeight: Float?,
        activityLevel: String?,
        goalType: String?,
        weeklyGoalRate: Float?
    ): Result<NutritionGoal>

    suspend fun getCurrentGoal(): Result<NutritionGoal>

    suspend fun saveGoal(goal: NutritionGoal): Result<NutritionGoal>
}
