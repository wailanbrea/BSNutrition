package com.bsnutrition.app.core.data

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.DiaryRepositoryImpl
import com.bsnutrition.app.core.database.DiaryDao
import com.bsnutrition.app.core.database.DiaryEntity
import com.bsnutrition.app.core.database.DiaryWithMeals
import com.bsnutrition.app.core.database.MealEntity
import com.bsnutrition.app.core.database.MealEntryDao
import com.bsnutrition.app.core.database.MealEntryEntity
import com.bsnutrition.app.core.database.MealWithEntries
import com.bsnutrition.app.core.database.SyncQueueDao
import com.bsnutrition.app.core.database.WaterLogDao
import com.bsnutrition.app.core.database.WaterLogEntity
import com.bsnutrition.app.core.network.api.DiaryApiService
import com.bsnutrition.app.core.network.model.DailySummaryDto
import com.bsnutrition.app.core.network.model.DiaryDayDto
import com.bsnutrition.app.core.network.model.DiaryDayResponseDto
import com.bsnutrition.app.core.network.model.MealDto
import com.bsnutrition.app.core.network.model.MessageResponseDto
import com.bsnutrition.app.core.network.model.WaterLogDto
import com.bsnutrition.app.core.network.model.WaterLogResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryRepositoryTest {

    private lateinit var diaryApiService: DiaryApiService
    private lateinit var diaryDao: DiaryDao
    private lateinit var mealEntryDao: MealEntryDao
    private lateinit var waterLogDao: WaterLogDao
    private lateinit var syncQueueDao: SyncQueueDao
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
        diaryApiService = mockk(relaxed = true)
        diaryDao = mockk(relaxed = true)
        mealEntryDao = mockk(relaxed = true)
        waterLogDao = mockk(relaxed = true)
        syncQueueDao = mockk(relaxed = true)

        repository = DiaryRepositoryImpl(
            diaryApiService = diaryApiService,
            diaryDao = diaryDao,
            mealEntryDao = mealEntryDao,
            waterLogDao = waterLogDao,
            syncQueueDao = syncQueueDao,
            ioDispatcher = testDispatcher,
            json = json
        )
    }

    @Test
    fun `getDiaryDay returns local diary immediately when cached in Room`() = runTest(testDispatcher) {
        val sampleDiaryEntity = DiaryEntity(
            id = 1L,
            user_id = 10L,
            diary_date = "2026-08-21"
        )
        val sampleMealEntity = MealEntity(
            id = 101L,
            diary_id = 1L,
            meal_type = "breakfast",
            name = "Desayuno",
            sort_order = 1
        )
        val sampleEntryEntity = MealEntryEntity(
            id = 501L,
            client_id = "client-1",
            meal_id = 101L,
            custom_name = "Mangú",
            quantity = 1.0,
            calories_snapshot = 310,
            protein_snapshot = 3.0,
            carbs_snapshot = 62.0,
            fat_snapshot = 6.4
        )

        val localDiaryWithMeals = DiaryWithMeals(
            diary = sampleDiaryEntity,
            mealsWithEntries = listOf(
                MealWithEntries(
                    meal = sampleMealEntity,
                    entries = listOf(sampleEntryEntity)
                )
            )
        )

        coEvery { diaryDao.getDiaryByDate("2026-08-21") } returns localDiaryWithMeals

        val result = repository.getDiaryDay("2026-08-21")

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("2026-08-21", data.diaryDate)
        assertEquals(310, data.summary.calories)
        assertEquals(1, data.meals.size)
        assertEquals("Desayuno", data.meals[0].name)
    }

    @Test
    fun `getDiaryDay fetches from network and caches in Room when local is null`() = runTest(testDispatcher) {
        coEvery { diaryDao.getDiaryByDate("2026-08-21") } returns null
        coEvery { diaryApiService.getDiaryDay("2026-08-21") } returns DiaryDayResponseDto(data = sampleDiaryDto)

        val result = repository.getDiaryDay("2026-08-21")

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("2026-08-21", data.diaryDate)
        assertEquals(1850, data.summary.calories)
        coVerify { diaryDao.insertOrUpdateDiary(any()) }
    }

    @Test
    fun `addMealEntry creates local entry, enqueues sync mutation, and returns domain model`() = runTest(testDispatcher) {
        coEvery { diaryDao.getDiaryByDate("2026-08-21") } returns null
        coEvery { diaryDao.insertOrUpdateDiary(any()) } returns 1L
        coEvery { mealEntryDao.insertOrUpdate(any()) } returns 501L
        coEvery { syncQueueDao.enqueue(any()) } returns 1001L

        val result = repository.addMealEntry(
            date = "2026-08-21",
            mealType = "breakfast",
            foodId = 1L,
            customName = "Mangú de Plátano Verde",
            quantity = 1.0,
            calories = 310,
            proteinG = 3.0,
            carbsG = 62.0,
            fatG = 6.4
        )

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals("Mangú de Plátano Verde", data.customName)
        assertEquals(310, data.caloriesSnapshot)
        coVerify { mealEntryDao.insertOrUpdate(any()) }
        coVerify { syncQueueDao.enqueue(any()) }
    }

    @Test
    fun `logWater registers local entry and enqueues sync mutation`() = runTest(testDispatcher) {
        coEvery { waterLogDao.insertOrUpdate(any()) } returns 201L
        coEvery { syncQueueDao.enqueue(any()) } returns 1002L

        val result = repository.logWater("2026-08-21", 250)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(250, data.amountMl)
        coVerify { waterLogDao.insertOrUpdate(any()) }
        coVerify { syncQueueDao.enqueue(any()) }
    }

    @Test
    fun `deleteMealEntry soft-deletes locally and enqueues delete mutation`() = runTest(testDispatcher) {
        coEvery { mealEntryDao.softDeleteById(501L) } returns Unit
        coEvery { syncQueueDao.enqueue(any()) } returns 1003L

        val result = repository.deleteMealEntry(501L)

        assertTrue(result is Result.Success)
        coVerify { mealEntryDao.softDeleteById(501L) }
        coVerify { syncQueueDao.enqueue(any()) }
    }

    @Test
    fun `observeDiaryDay emits flow from Room`() = runTest(testDispatcher) {
        val sampleDiaryEntity = DiaryEntity(id = 1L, user_id = 10L, diary_date = "2026-08-21")
        val diaryWithMeals = DiaryWithMeals(diary = sampleDiaryEntity, mealsWithEntries = emptyList())

        coEvery { diaryDao.observeDiaryByDate("2026-08-21") } returns flowOf(diaryWithMeals)

        val flowResult = repository.observeDiaryDay("2026-08-21").first()

        assertNotNull(flowResult)
        assertEquals("2026-08-21", flowResult?.diaryDate)
    }
}
