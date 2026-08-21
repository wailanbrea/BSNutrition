package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.BsnDispatchers
import com.bsnutrition.app.core.common.Dispatcher
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.NutritionGoal
import com.bsnutrition.app.core.network.api.GoalApiService
import com.bsnutrition.app.core.network.model.CalculateGoalRequestDto
import com.bsnutrition.app.core.network.model.SaveGoalRequestDto
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalApiService: GoalApiService,
    @Dispatcher(BsnDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : GoalRepository {

    override suspend fun calculateGoal(
        birthDate: String?,
        sex: String?,
        height: Float?,
        currentWeight: Float?,
        activityLevel: String?,
        goalType: String?,
        weeklyGoalRate: Float?
    ): Result<NutritionGoal> {
        val request = CalculateGoalRequestDto(
            birthDate = birthDate,
            sex = sex,
            height = height,
            currentWeight = currentWeight,
            activityLevel = activityLevel,
            goalType = goalType,
            weeklyGoalRate = weeklyGoalRate
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            goalApiService.calculateGoal(request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.calculatedGoal.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun getCurrentGoal(): Result<NutritionGoal> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            goalApiService.getCurrentGoal()
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.goal.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun saveGoal(goal: NutritionGoal): Result<NutritionGoal> {
        val request = SaveGoalRequestDto(
            effectiveFrom = goal.effectiveFrom,
            calorieTarget = goal.calorieTarget,
            proteinTargetG = goal.proteinTargetG,
            carbohydrateTargetG = goal.carbohydrateTargetG,
            fatTargetG = goal.fatTargetG,
            fiberTargetG = goal.fiberTargetG,
            waterTargetMl = goal.waterTargetMl,
            source = goal.source
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            goalApiService.updateGoal(request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.goal.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }
}
