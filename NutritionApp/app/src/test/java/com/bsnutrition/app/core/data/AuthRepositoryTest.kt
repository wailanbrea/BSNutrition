package com.bsnutrition.app.core.data

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.AuthRepositoryImpl
import com.bsnutrition.app.core.database.UserDao
import com.bsnutrition.app.core.datastore.AuthTokenDataSource
import com.bsnutrition.app.core.network.api.AuthApiService
import com.bsnutrition.app.core.network.model.AuthResponseDto
import com.bsnutrition.app.core.network.model.MessageResponseDto
import com.bsnutrition.app.core.network.model.UserDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {

    private val authApiService: AuthApiService = mockk()
    private val authTokenDataSource: AuthTokenDataSource = mockk(relaxed = true)
    private val userDao: UserDao = mockk(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        coEvery { authTokenDataSource.userId } returns flowOf(1L)
        coEvery { authTokenDataSource.authToken } returns flowOf("mock-token")

        authRepository = AuthRepositoryImpl(
            authApiService = authApiService,
            authTokenDataSource = authTokenDataSource,
            userDao = userDao,
            ioDispatcher = Dispatchers.Unconfined,
            json = json
        )
    }

    @Test
    fun login_whenSuccessful_savesTokenAndInsertsUser() = runTest {
        val userDto = UserDto(
            id = 1L,
            name = "Test User",
            email = "test@example.com"
        )
        val responseDto = AuthResponseDto(
            user = userDto,
            token = "jwt-sanctum-token",
            tokenType = "Bearer"
        )

        coEvery { authApiService.login(any()) } returns Response.success(responseDto)

        val result = authRepository.login("test@example.com", "password123")

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).data.id)
        assertEquals("Test User", result.data.name)

        coVerify { authTokenDataSource.saveAuth("jwt-sanctum-token", 1L) }
        coVerify { userDao.insertUser(any()) }
    }

    @Test
    fun logout_clearsDataStoreAndUserDao() = runTest {
        coEvery { authApiService.logout() } returns Response.success(MessageResponseDto("Logged out"))

        val result = authRepository.logout()

        assertTrue(result is Result.Success)
        coVerify { authTokenDataSource.clearAuth() }
        coVerify { userDao.clearUsers() }
    }
}
