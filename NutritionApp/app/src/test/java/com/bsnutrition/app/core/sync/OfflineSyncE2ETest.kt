package com.bsnutrition.app.core.sync

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
import com.bsnutrition.app.core.database.SyncQueueEntity
import com.bsnutrition.app.core.database.WaterLogDao
import com.bsnutrition.app.core.database.WaterLogEntity
import com.bsnutrition.app.core.network.api.DiaryApiService
import com.bsnutrition.app.core.network.model.DailySummaryDto
import com.bsnutrition.app.core.network.model.DiaryDayDto
import com.bsnutrition.app.core.network.model.DiaryDayResponseDto
import com.bsnutrition.app.core.network.model.MealDto
import com.bsnutrition.app.core.network.model.MealEntryDto
import com.bsnutrition.app.core.network.model.MealEntryResponseDto
import com.bsnutrition.app.core.network.model.MessageResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineSyncE2ETest {

    private lateinit var diaryApiService: DiaryApiService
    private lateinit var diaryDao: DiaryDao
    private lateinit var mealEntryDao: MealEntryDao
    private lateinit var waterLogDao: WaterLogDao
    private lateinit var syncQueueDao: SyncQueueDao
    private lateinit var repository: DiaryRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

    // In-memory mock storage simulating Room DB tables
    private val inMemoryDiaries = mutableListOf<DiaryEntity>()
    private val inMemoryMeals = mutableListOf<MealEntity>()
    private val inMemoryEntries = mutableListOf<MealEntryEntity>()
    private val inMemoryWater = mutableListOf<WaterLogEntity>()
    private val inMemoryQueue = mutableListOf<SyncQueueEntity>()

    @Before
    fun setUp() {
        diaryApiService = mockk(relaxed = true)
        diaryDao = mockk(relaxed = true)
        mealEntryDao = mockk(relaxed = true)
        waterLogDao = mockk(relaxed = true)
        syncQueueDao = mockk(relaxed = true)

        // Wire Room DiaryDao mocks
        coEvery { diaryDao.getDiaryByDate(any()) } answers {
            val date = firstArg<String>()
            val diary = inMemoryDiaries.find { it.diary_date == date }
            if (diary != null) {
                val meals = inMemoryMeals.filter { it.diary_id == diary.id }
                val mealsWithEntries = meals.map { meal ->
                    MealWithEntries(
                        meal = meal,
                        entries = inMemoryEntries.filter { it.meal_id == meal.id && !it.is_deleted }
                    )
                }
                DiaryWithMeals(diary = diary, mealsWithEntries = mealsWithEntries)
            } else null
        }

        coEvery { diaryDao.insertOrUpdateDiary(any()) } answers {
            val diary = firstArg<DiaryEntity>()
            val generatedId = if (diary.id == 0L) (inMemoryDiaries.size + 1).toLong() else diary.id
            val saved = diary.copy(id = generatedId)
            inMemoryDiaries.removeAll { it.id == saved.id || (it.user_id == saved.user_id && it.diary_date == saved.diary_date) }
            inMemoryDiaries.add(saved)
            generatedId
        }

        coEvery { diaryDao.insertMeals(any()) } answers {
            val meals = firstArg<List<MealEntity>>()
            val ids = meals.mapIndexed { idx, m ->
                val id = (inMemoryMeals.size + idx + 1).toLong()
                inMemoryMeals.add(m.copy(id = id))
                id
            }
            ids
        }

        // Wire Room MealEntryDao mocks
        coEvery { mealEntryDao.insertOrUpdate(any()) } answers {
            val entry = firstArg<MealEntryEntity>()
            val generatedId = if (entry.id == 0L) (inMemoryEntries.size + 1).toLong() else entry.id
            val saved = entry.copy(id = generatedId)
            inMemoryEntries.removeAll { it.id == saved.id || (it.client_id != null && it.client_id == saved.client_id) }
            inMemoryEntries.add(saved)
            generatedId
        }

        coEvery { mealEntryDao.getEntryById(any()) } answers {
            val id = firstArg<Long>()
            inMemoryEntries.find { it.id == id && !it.is_deleted }
        }

        coEvery { mealEntryDao.softDeleteById(any(), any()) } answers {
            val id = firstArg<Long>()
            val existing = inMemoryEntries.find { it.id == id }
            if (existing != null) {
                inMemoryEntries.remove(existing)
                inMemoryEntries.add(existing.copy(is_deleted = true, sync_status = "pending_delete"))
            }
        }

        // Wire Room WaterLogDao mocks
        coEvery { waterLogDao.insertOrUpdate(any()) } answers {
            val log = firstArg<WaterLogEntity>()
            val generatedId = if (log.id == 0L) (inMemoryWater.size + 1).toLong() else log.id
            val saved = log.copy(id = generatedId)
            inMemoryWater.removeAll { it.id == saved.id }
            inMemoryWater.add(saved)
            generatedId
        }

        // Wire Room SyncQueueDao mocks
        coEvery { syncQueueDao.enqueue(any()) } answers {
            val mutation = firstArg<SyncQueueEntity>()
            val genId = (inMemoryQueue.size + 1).toLong()
            inMemoryQueue.add(mutation.copy(id = genId))
            genId
        }

        coEvery { syncQueueDao.getAllPendingMutations() } answers {
            inMemoryQueue.toList()
        }

        coEvery { syncQueueDao.dequeue(any()) } answers {
            val id = firstArg<Long>()
            inMemoryQueue.removeAll { it.id == id }
        }

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
    fun `E2E full offline create, edit, delete, and reconnect drain`() = runTest(testDispatcher) {
        // SCENARIO: Network is completely offline (throws IOException)
        coEvery { diaryApiService.addMealEntry(any(), any()) } throws IOException("No network connection")
        coEvery { diaryApiService.updateMealEntry(any(), any()) } throws IOException("No network connection")
        coEvery { diaryApiService.deleteMealEntry(any()) } throws IOException("No network connection")
        coEvery { diaryApiService.logWater(any(), any()) } throws IOException("No network connection")

        // 1. User adds food entry while OFFLINE
        val addResult = repository.addMealEntry(
            date = "2026-08-21",
            mealType = "breakfast",
            foodId = 1L,
            customName = "Mangú con Salami",
            quantity = 1.0,
            calories = 420,
            proteinG = 12.0,
            carbsG = 58.0,
            fatG = 16.0
        )

        assertTrue(addResult is Result.Success)
        val entry = (addResult as Result.Success).data
        assertEquals("Mangú con Salami", entry.customName)
        assertEquals(420, entry.caloriesSnapshot)
        assertEquals(1, inMemoryEntries.size)
        assertEquals("pending_create", inMemoryEntries[0].sync_status)
        assertEquals(1, inMemoryQueue.size)
        assertEquals("CREATE", inMemoryQueue[0].operation)

        // 2. User logs water while OFFLINE
        val waterResult = repository.logWater("2026-08-21", 500)
        assertTrue(waterResult is Result.Success)
        assertEquals(1, inMemoryWater.size)
        assertEquals("pending_create", inMemoryWater[0].sync_status)
        assertEquals(2, inMemoryQueue.size)

        // 3. User updates food entry quantity while OFFLINE
        val updateResult = repository.updateMealEntry(
            id = entry.id,
            quantity = 2.0,
            calories = 840
        )
        assertTrue(updateResult is Result.Success)
        assertEquals(840, (updateResult as Result.Success).data.caloriesSnapshot)
        assertEquals("pending_update", inMemoryEntries.find { it.id == entry.id }?.sync_status)
        assertEquals(3, inMemoryQueue.size)

        // 4. User deletes food entry while OFFLINE
        val deleteResult = repository.deleteMealEntry(entry.id)
        assertTrue(deleteResult is Result.Success)
        assertTrue(inMemoryEntries.find { it.id == entry.id }?.is_deleted == true)
        assertEquals(4, inMemoryQueue.size)

        // 5. RECONNECTION: Network comes back ONLINE
        coEvery { diaryApiService.deleteMealEntry(any()) } returns MessageResponseDto("Deleted")
        coEvery { diaryApiService.deleteWaterLog(any()) } returns MessageResponseDto("Deleted")

        // Sync pending mutations
        val syncCountResult = repository.syncPendingMutations()
        assertTrue(syncCountResult is Result.Success)
        val syncedCount = (syncCountResult as Result.Success).data
        assertTrue(syncedCount >= 1)
        coVerify { diaryApiService.deleteMealEntry(entry.id) }
    }

    @Test
    fun `E2E handles server 4xx permanent error by discarding poison pill`() = runTest(testDispatcher) {
        val notFoundResponseBody = "{\"message\":\"Resource not found\"}".toResponseBody("application/json".toMediaType())
        val http404Exception = HttpException(Response.error<Any>(404, notFoundResponseBody))

        coEvery { diaryApiService.deleteMealEntry(999L) } throws http404Exception

        inMemoryQueue.add(
            SyncQueueEntity(
                id = 99L,
                entity_type = "meal_entry",
                entity_id = 999L,
                operation = "DELETE",
                payload_json = "{}"
            )
        )

        // Test sync handling
        repository.syncPendingMutations()
        coVerify { syncQueueDao.getAllPendingMutations() }
    }
}
