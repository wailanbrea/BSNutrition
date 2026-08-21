package com.bsnutrition.app.feature.home

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.GoalRepository
import com.bsnutrition.app.core.model.NutritionGoal
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val goalRepository: GoalRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsCurrentGoalSuccessfully() = runTest {
        val testGoal = NutritionGoal(
            calorieTarget = 2200,
            proteinTargetG = 160f,
            carbohydrateTargetG = 240f,
            fatTargetG = 70f,
            waterTargetMl = 2800
        )

        coEvery { goalRepository.getCurrentGoal() } returns Result.Success(testGoal)

        val viewModel = HomeViewModel(goalRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.goal)
        assertEquals(2200, state.goal?.calorieTarget)
        assertEquals(160f, state.goal?.proteinTargetG)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun init_whenError_setsErrorMessage() = runTest {
        coEvery { goalRepository.getCurrentGoal() } returns Result.Error(
            message = "No se pudieron cargar las metas."
        )

        val viewModel = HomeViewModel(goalRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.goal)
        assertEquals("No se pudieron cargar las metas.", state.errorMessage)
        assertFalse(state.isLoading)
    }
}
