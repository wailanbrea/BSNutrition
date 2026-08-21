package com.bsnutrition.app.feature.home

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.data.repository.GoalRepository
import com.bsnutrition.app.core.model.ActivityLevel
import com.bsnutrition.app.core.model.CalorieFormula
import com.bsnutrition.app.core.model.DailyDiary
import com.bsnutrition.app.core.model.DailySummary
import com.bsnutrition.app.core.model.GoalPace
import com.bsnutrition.app.core.model.GoalType
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.NutritionGoal
import com.bsnutrition.app.core.model.WaterLog
import io.mockk.coEvery
import io.mockk.coVerify
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
    private val diaryRepository: DiaryRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val sampleGoal = NutritionGoal(
        id = 1L,
        userId = 10L,
        goalType = GoalType.LOSE_WEIGHT,
        activityLevel = ActivityLevel.MODERATE,
        goalPace = GoalPace.MODERATE,
        targetCalories = 2200,
        targetProteinG = 160.0,
        targetCarbsG = 240.0,
        targetFatG = 70.0,
        targetWaterMl = 2800,
        formulaUsed = CalorieFormula.MIFFLIN_ST_JEOR
    )

    private val sampleSummary = DailySummary(
        date = "2026-08-21",
        calories = 1400,
        proteinG = 110.0,
        carbsG = 160.0,
        fatG = 45.0,
        waterMl = 1500
    )

    private val sampleDiary = DailyDiary(
        id = 1L,
        userId = 10L,
        diaryDate = "2026-08-21",
        timezone = "America/Santo_Domingo",
        summary = sampleSummary,
        meals = listOf(
            MealLog(
                id = 101L,
                diaryId = 1L,
                mealType = "breakfast",
                name = "Desayuno",
                sortOrder = 1,
                totalCalories = 500,
                totalProteinG = 35.0,
                totalCarbsG = 60.0,
                totalFatG = 15.0
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { goalRepository.getCurrentGoal() } returns Result.Success(sampleGoal)
        coEvery { diaryRepository.getDiaryDay(any()) } returns Result.Success(sampleDiary)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsCurrentGoalAndTodayDiarySuccessfully() = runTest {
        val viewModel = HomeViewModel(goalRepository, diaryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.goal)
        assertNotNull(state.diary)
        assertEquals(2200, state.targetCalories)
        assertEquals(1400, state.totalCaloriesConsumed)
        assertEquals(800, state.remainingCalories)
        assertEquals(1500, state.totalWaterMl)
        assertEquals(1, state.meals.size)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun logWater_callsRepositoryAndRefreshesDiary() = runTest {
        val newWaterLog = WaterLog(id = 5L, logDate = "2026-08-21", amountMl = 250)
        coEvery { diaryRepository.logWater(any(), 250) } returns Result.Success(newWaterLog)

        val viewModel = HomeViewModel(goalRepository, diaryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.logWater(250)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("+250ml de agua registrados", viewModel.uiState.value.userMessage)
        coVerify { diaryRepository.logWater(any(), 250) }
    }

    @Test
    fun init_whenGoalError_setsErrorMessage() = runTest {
        coEvery { goalRepository.getCurrentGoal() } returns Result.Error(
            message = "No se pudieron cargar las metas."
        )

        val viewModel = HomeViewModel(goalRepository, diaryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.goal)
        assertEquals("No se pudieron cargar las metas.", state.errorMessage)
    }
}
