package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.BsnDispatchers
import com.bsnutrition.app.core.common.Dispatcher
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.UserProfile
import com.bsnutrition.app.core.network.api.ProfileApiService
import com.bsnutrition.app.core.network.model.UpdateProfileRequestDto
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService,
    @Dispatcher(BsnDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : ProfileRepository {

    override suspend fun getProfile(): Result<UserProfile> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            profileApiService.getProfile()
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.profile.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun updateProfile(profile: UserProfile): Result<UserProfile> {
        val request = UpdateProfileRequestDto(
            birthDate = profile.birthDate,
            sex = profile.sex,
            height = profile.height,
            currentWeight = profile.currentWeight,
            activityLevel = profile.activityLevel,
            goalType = profile.goalType,
            goalWeight = profile.goalWeight,
            weeklyGoalRate = profile.weeklyGoalRate,
            locale = profile.locale,
            countryCode = profile.countryCode,
            timezone = profile.timezone,
            unitSystem = profile.unitSystem
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            profileApiService.updateProfile(request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.profile.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }
}
