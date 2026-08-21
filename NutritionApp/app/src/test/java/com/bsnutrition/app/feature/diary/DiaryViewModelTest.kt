package com.bsnutrition.app.feature.diary

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
import com.bsnutrition.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var diaryRepository: DiaryRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var viewModel: DiaryViewModel

    private val sampleGoal = NutritionGoal(
        id = 1L,
        userId = 10L,
        goalType = GoalType.LOSE_WEIGHT,
        activityLevel = ActivityLevel.MODERATE,
        goalPace = GoalPace.MODERATE,
        targetCalories = 2000,
        targetProteinG = 150.0,
        targetCarbsG = 200.0,
        targetFatG = 65.0,
        formulaUsed = CalorieFormula.MIFFLIN_ST_JEOR
    )

    private val sampleSummary = DailySummary(
        date = "2026-08-21",
        calories = 1200,
        proteinG = 90.0,
        carbsG = 140.0,
        fatG = 35.0,
        waterMl = 1000
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
                totalCalories = 400,
                totalProteinG = 30.0,
                totalCarbsG = 45.0,
                totalFatG = 10.0
            )
        )
    )

    @Before
    fun setUp() {
        diaryRepository = mockk(relaxed = true)
        goalRepository = mockk(relaxed = true)

        coEvery { goalRepository.getCurrentGoal() } returns Result.Success(sampleGoal)
        coEvery { diaryRepository.getDiaryDay(any()) } returns Result.Success(sampleDiary)
        coEvery { diaryRepository.getWaterLogs(any()) } returns Result.Success(
            listOf(WaterLog(id = 1L, logDate = "2026-08-21", amountMl = 1000))
        )
    }

    @Test
    fun `init loads current goal and diary for today`() = runTest {
        viewModel = DiaryViewModel(diaryRepository, goalRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.diary)
        assertEquals(1200, state.totalCaloriesConsumed)
        assertEquals(2000, state.targetCalories)
        assertEquals(800, state.remainingCalories)
        assertEquals(1000, state.totalWaterMl)
    }

    @Test
    fun `onNextDay advances selected date and reloads diary`() = runTest {
        viewModel = DiaryViewModel(diaryRepository, goalRepository)
        advanceUntilIdle()

        val initialDate = viewModel.uiState.value.selectedDate
        viewModel.onNextDay()
        advanceUntilIdle()

        assertEquals(initialDate.plusDays(1), viewModel.uiState.value.selectedDate)
        coVerify(atLeast = 2) { diaryRepository.getDiaryDay(any()) }
    }

    @Test
    fun `logWater adds water intake and updates message`() = runTest {
        val newWater = WaterLog(id = 2L, logDate = "2026-08-21", amountMl = 250)
        coEvery { diaryRepository.logWater(any(), 250) } returns Result.Success(newWater)
        coEvery { diaryRepository.getDailySummary(any()) } returns Result.Success(sampleSummary.copy(waterMl = 1250))

        viewModel = DiaryViewModel(diaryRepository, goalRepository)
        advanceUntilIdle()

        viewModel.logWater(250)
        advanceUntilIdle()

        assertEquals("+250ml de agua registrados", viewModel.uiState.value.userMessage)
        coVerify { diaryRepository.logWater(any(), 250) }
    }

    @Test
    fun `deleteMealEntry calls repository and reloads diary`() = runTest {
        coEvery { diaryRepository.deleteMealEntry(501L) } returns Result.Success(Unit)

        viewModel = DiaryViewModel(diaryRepository, goalRepository)
        advanceUntilIdle()

        viewModel.deleteMealEntry(501L)
        advanceUntilIdle()

        assertEquals("Alimento eliminado de la comida", viewModel.uiState.value.userMessage)
        coVerify { diaryRepository.deleteMealEntry(501L) }
    }

    @Test
    fun `openDatePickerDialog and dismissDatePickerDialog toggle state correctly`() = runTest {
        viewModel = DiaryViewModel(diaryRepository, goalRepository)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.showDatePickerDialog)

        viewModel.openDatePickerDialog()
        assertEquals(true, viewModel.uiState.value.showDatePickerDialog)

        viewModel.dismissDatePickerDialog()
        assertEquals(false, viewModel.uiState.value.showDatePickerDialog)
    }

    @Test
    fun `onDateSelected updates selected date and fetches historical diary`() = runTest {
        val targetHistoricalDate = LocalDate.of(2026, 8, 15)
        viewModel = DiaryViewModel(diaryRepository, goalRepository)
        advanceUntilIdle()

        viewModel.onDateSelected(targetHistoricalDate)
        advanceUntilIdle()

        assertEquals(targetHistoricalDate, viewModel.uiState.value.selectedDate)
        coVerify { diaryRepository.getDiaryDay("2026-08-15") }
        coVerify { diaryRepository.getWaterLogs("2026-08-15") }
    }
}

