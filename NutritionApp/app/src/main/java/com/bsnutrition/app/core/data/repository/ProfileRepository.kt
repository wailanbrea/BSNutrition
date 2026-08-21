package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Result<UserProfile>
}
