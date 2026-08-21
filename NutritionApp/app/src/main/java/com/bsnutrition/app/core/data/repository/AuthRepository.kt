package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<Long?>
    val currentUser: Flow<User?>
    val isAuthenticated: Flow<Boolean>

    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        deviceName: String? = null
    ): Result<User>

    suspend fun login(
        email: String,
        password: String,
        deviceName: String? = null
    ): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun getMe(): Result<User>

    suspend fun deleteMe(): Result<Unit>
}
