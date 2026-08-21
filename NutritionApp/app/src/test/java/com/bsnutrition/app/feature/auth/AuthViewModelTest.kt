package com.bsnutrition.app.feature.auth

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.AuthRepository
import com.bsnutrition.app.core.model.User
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { authRepository.isAuthenticated } returns flowOf(false)
        coEvery { authRepository.currentUser } returns flowOf(null)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_withBlankFields_setsFieldErrors() {
        viewModel.login("", "")

        val state = viewModel.uiState.value
        assertEquals("El correo electrónico es requerido", state.fieldErrors["email"])
        assertEquals("La contraseña es requerida", state.fieldErrors["password"])
        assertFalse(state.isLoading)
    }

    @Test
    fun register_withMismatchedPasswords_setsPasswordConfirmationError() {
        viewModel.register("John", "john@example.com", "password123", "password456")

        val state = viewModel.uiState.value
        assertEquals("Las contraseñas no coinciden", state.fieldErrors["password_confirmation"])
        assertFalse(state.isLoading)
    }

    @Test
    fun login_whenSuccessful_updatesUiStateToAuthenticated() = runTest {
        val user = User(id = 1L, name = "John Doe", email = "john@example.com")
        coEvery { authRepository.login("john@example.com", "password123") } returns Result.Success(user)

        viewModel.login("john@example.com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertTrue(state.isSuccess)
        assertEquals(user, state.user)
        assertFalse(state.isLoading)
    }
}
