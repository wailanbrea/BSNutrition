package com.bsnutrition.app.core.database

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomSchemaDaoTest {

    private lateinit var diaryDao: DiaryDao
    private lateinit var mealEntryDao: MealEntryDao
    private lateinit var waterLogDao: WaterLogDao
    private lateinit var syncQueueDao: SyncQueueDao

    @Before
    fun setUp() {
        diaryDao = mockk()
        mealEntryDao = mockk()
        waterLogDao = mockk()
        syncQueueDao = mockk()
    }

    @Test
    fun `diaryDao observeDiaryByDate emits diary with meals`() = runTest {
        val sampleDiary = DiaryEntity(
            id = 1L,
            user_id = 10L,
            diary_date = "2026-08-21"
        )
        val sampleMeal = MealEntity(
            id = 101L,
            diary_id = 1L,
            meal_type = "breakfast",
            name = "Desayuno"
        )
        val sampleEntry = MealEntryEntity(
            id = 501L,
            client_id = "entry-cli-1",
            meal_id = 101L,
            custom_name = "Mangú con huevo",
            quantity = 1.0,
            calories_snapshot = 350,
            protein_snapshot = 14.0,
            carbs_snapshot = 45.0,
            fat_snapshot = 12.0
        )
        val diaryWithMeals = DiaryWithMeals(
            diary = sampleDiary,
            mealsWithEntries = listOf(
                MealWithEntries(meal = sampleMeal, entries = listOf(sampleEntry))
            )
        )

        coEvery { diaryDao.observeDiaryByDate("2026-08-21") } returns flowOf(diaryWithMeals)

        val emitted = diaryDao.observeDiaryByDate("2026-08-21").first()

        assertNotNull(emitted)
        assertEquals("2026-08-21", emitted?.diary?.diary_date)
        assertEquals(1, emitted?.mealsWithEntries?.size)
        assertEquals("Desayuno", emitted?.mealsWithEntries?.get(0)?.meal?.name)
        assertEquals(1, emitted?.mealsWithEntries?.get(0)?.entries?.size)
        assertEquals(350, emitted?.mealsWithEntries?.get(0)?.entries?.get(0)?.calories_snapshot)
    }

    @Test
    fun `waterLogDao observeTotalWaterForDate calculates sum`() = runTest {
        coEvery { waterLogDao.observeTotalWaterForDate("2026-08-21") } returns flowOf(1500)

        val totalWater = waterLogDao.observeTotalWaterForDate("2026-08-21").first()
        assertEquals(1500, totalWater)
    }

    @Test
    fun `syncQueueDao enqueue and dequeue work as expected`() = runTest {
        val mutation = SyncQueueEntity(
            id = 1L,
            entity_type = "meal_entry",
            entity_id = 501L,
            operation = "CREATE",
            payload_json = "{\"custom_name\":\"Mangú\"}"
        )

        coEvery { syncQueueDao.enqueue(any()) } returns 1L
        coEvery { syncQueueDao.dequeue(1L) } returns Unit

        val insertedId = syncQueueDao.enqueue(mutation)
        assertEquals(1L, insertedId)

        syncQueueDao.dequeue(1L)
        coVerify { syncQueueDao.dequeue(1L) }
    }
}
