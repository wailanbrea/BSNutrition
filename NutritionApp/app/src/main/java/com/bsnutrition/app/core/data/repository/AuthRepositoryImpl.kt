package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.BsnDispatchers
import com.bsnutrition.app.core.common.Dispatcher
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.UserDao
import com.bsnutrition.app.core.database.UserEntity
import com.bsnutrition.app.core.datastore.AuthTokenDataSource
import com.bsnutrition.app.core.model.User
import com.bsnutrition.app.core.network.api.AuthApiService
import com.bsnutrition.app.core.network.model.LoginRequestDto
import com.bsnutrition.app.core.network.model.RegisterRequestDto
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authTokenDataSource: AuthTokenDataSource,
    private val userDao: UserDao,
    @Dispatcher(BsnDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : AuthRepository {

    override val currentUserId: Flow<Long?> = authTokenDataSource.userId

    override val isAuthenticated: Flow<Boolean> = authTokenDataSource.authToken.map {
        !it.isNullOrBlank()
    }

    override val currentUser: Flow<User?> = currentUserId.flatMapLatest { id ->
        if (id != null) {
            userDao.getUser(id).map { entity -> entity?.toDomain() }
        } else {
            flowOf(null)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        deviceName: String?
    ): Result<User> {
        val request = RegisterRequestDto(
            name = name,
            email = email,
            password = password,
            passwordConfirmation = passwordConfirmation,
            deviceName = deviceName ?: "Android Device"
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            authApiService.register(request)
        }

        return when (apiResult) {
            is Result.Success -> {
                val authResponse = apiResult.data
                val domainUser = authResponse.user.toDomain()
                authTokenDataSource.saveAuth(authResponse.token, domainUser.id)
                userDao.insertUser(domainUser.toEntity())
                Result.Success(domainUser)
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun login(
        email: String,
        password: String,
        deviceName: String?
    ): Result<User> {
        val request = LoginRequestDto(
            email = email,
            password = password,
            deviceName = deviceName ?: "Android Device"
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            authApiService.login(request)
        }

        return when (apiResult) {
            is Result.Success -> {
                val authResponse = apiResult.data
                val domainUser = authResponse.user.toDomain()
                authTokenDataSource.saveAuth(authResponse.token, domainUser.id)
                userDao.insertUser(domainUser.toEntity())
                Result.Success(domainUser)
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun logout(): Result<Unit> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            authApiService.logout()
        }

        authTokenDataSource.clearAuth()
        userDao.clearUsers()

        return when (apiResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Success(Unit) // Limpieza local garantizada aunque el backend falle
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun getMe(): Result<User> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            authApiService.getMe()
        }

        return when (apiResult) {
            is Result.Success -> {
                val domainUser = apiResult.data.user.toDomain()
                userDao.insertUser(domainUser.toEntity())
                Result.Success(domainUser)
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun deleteMe(): Result<Unit> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            authApiService.deleteMe()
        }

        authTokenDataSource.clearAuth()
        userDao.clearUsers()

        return when (apiResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }
}

private fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    emailVerifiedAt = emailVerifiedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    emailVerifiedAt = emailVerifiedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
