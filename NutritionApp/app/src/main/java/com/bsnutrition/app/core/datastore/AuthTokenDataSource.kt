package com.bsnutrition.app.core.datastore

import kotlinx.coroutines.flow.Flow

interface AuthTokenDataSource {
    val authToken: Flow<String?>
    val userId: Flow<Long?>
    suspend fun saveAuth(token: String, userId: Long)
    suspend fun clearAuth()
}
