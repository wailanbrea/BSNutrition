package com.bsnutrition.app.core.data

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.DiaryRepositoryImpl
import com.bsnutrition.app.core.network.api.DiaryApiService
import com.bsnutrition.app.core.network.model.AddMealEntryRequestDto
import com.bsnutrition.app.core.network.model.DailySummaryDto
import com.bsnutrition.app.core.network.model.DailySummaryResponseDto
import com.bsnutrition.app.core.network.model.DiaryDayDto
import com.bsnutrition.app.core.network.model.DiaryDayResponseDto
import com.bsnutrition.app.core.network.model.LogWaterRequestDto
import com.bsnutrition.app.core.network.model.MealDto
import com.bsnutrition.app.core.network.model.MealEntryDto
import com.bsnutrition.app.core.network.model.MealEntryResponseDto
import com.bsnutrition.app.core.network.model.MealResponseDto
import com.bsnutrition.app.core.network.model.MessageResponseDto
import com.bsnutrition.app.core.network.model.WaterLogDto
import com.bsnutrition.app.core.network.model.WaterLogListResponseDto
import com.bsnutrition.app.core.network.model.WaterLogResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryRepositoryTest {

    private lateinit var diaryApiService: DiaryApiService
    private lateinit var repository: DiaryRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

    private val sampleSummaryDto = DailySummaryDto(
        date = "2026-08-21",
        calories = 1850,
        proteinG = 135.0,
        carbsG = 190.0,
        fatG = 55.0,
        fiberG = 25.0,
        waterMl = 2000
    )

    private val sampleDiaryDto = DiaryDayDto(
        id = 1L,
        userId = 10L,
        diaryDate = "2026-08-21",
        summary = sampleSummaryDto,
        meals = listOf(
            MealDto(
                id = 101L,
                diaryId = 1L,
                mealType = "breakfast",
                name = "Desayuno",
                sortOrder = 1,
                totalCalories = 450,
                totalProteinG = 30.0,
                totalCarbsG = 50.0,
                totalFatG = 12.0
            )
        )
    )

    @Before
    fun setUp() {
        diaryApiService = mockk()
        repository = DiaryRepositoryImpl(
            diaryApiService = diaryApiService,
            ioDispatcher = testDispatcher,
            json = json
        )
    }

    @Test
    fun `getDiaryDay returns daily diary data`() = runTest(testDispatcher) {
        coEvery { diaryApiService.getDiaryDay("2026-08-21") } returns DiaryDayResponseDto(data = sampleDiaryDto)

        val result = repository.getDiaryDay("2026-08-21")

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("2026-08-21", data.diaryDate)
        assertEquals(1850, data.summary.calories)
        assertEquals(1, data.meals.size)
        assertEquals("Desayuno", data.meals[0].name)
    }

    @Test
    fun `addMealEntry creates entry and returns domain model`() = runTest(testDispatcher) {
        val entryDto = MealEntryDto(
            id = 501L,
            mealId = 101L,
            foodId = 1L,
            customName = "Mangú de Plátano Verde",
            quantity = 1.0,
            unit = "porción",
            grams = 200.0,
            caloriesSnapshot = 310,
            proteinSnapshot = 3.0,
            carbsSnapshot = 62.0,
            fatSnapshot = 6.4
        )

        coEvery { diaryApiService.addMealEntry("2026-08-21", any()) } returns MealEntryResponseDto(data = entryDto)

        val result = repository.addMealEntry(
            date = "2026-08-21",
            mealType = "breakfast",
            foodId = 1L,
            quantity = 1.0
        )

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("Mangú de Plátano Verde", data.customName)
        assertEquals(310, data.caloriesSnapshot)
    }

    @Test
    fun `logWater registers water intake`() = runTest(testDispatcher) {
        val waterDto = WaterLogDto(
            id = 201L,
            logDate = "2026-08-21",
            amountMl = 250,
            source = "manual"
        )

        coEvery { diaryApiService.logWater("2026-08-21", any()) } returns WaterLogResponseDto(data = waterDto)

        val result = repository.logWater("2026-08-21", 250)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(250, data.amountMl)
    }

    @Test
    fun `deleteMealEntry calls API and returns success`() = runTest(testDispatcher) {
        coEvery { diaryApiService.deleteMealEntry(501L) } returns MessageResponseDto("Deleted")

        val result = repository.deleteMealEntry(501L)

        assertTrue(result is Result.Success)
        coVerify { diaryApiService.deleteMealEntry(501L) }
    }
}
