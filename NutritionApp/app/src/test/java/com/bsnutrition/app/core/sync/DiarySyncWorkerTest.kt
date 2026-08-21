package com.bsnutrition.app.core.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.database.MealEntryDao
import com.bsnutrition.app.core.database.SyncQueueDao
import com.bsnutrition.app.core.database.SyncQueueEntity
import com.bsnutrition.app.core.database.WaterLogDao
import com.bsnutrition.app.core.datastore.TokenManager
import com.bsnutrition.app.core.network.api.DiaryApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiarySyncWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var syncQueueDao: SyncQueueDao
    private lateinit var diaryApiService: DiaryApiService
    private lateinit var mealEntryDao: MealEntryDao
    private lateinit var waterLogDao: WaterLogDao
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var tokenManager: TokenManager
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        syncQueueDao = mockk(relaxed = true)
        diaryApiService = mockk(relaxed = true)
        mealEntryDao = mockk(relaxed = true)
        waterLogDao = mockk(relaxed = true)
        diaryRepository = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)

        every { tokenManager.getAccessToken() } returns flowOf("mock-sanctum-token")
    }

    @Test
    fun `doWork drains sync queue and completes successfully`() = runTest {
        val pendingMutation = SyncQueueEntity(
            id = 1L,
            entity_type = "meal_entry",
            entity_id = 501L,
            operation = "DELETE",
            payload_json = "{}"
        )

        coEvery { syncQueueDao.getAllPendingMutations() } returns listOf(pendingMutation)
        coEvery { diaryApiService.deleteMealEntry(501L) } returns mockk(relaxed = true)

        val worker = DiarySyncWorker(
            appContext = context,
            workerParams = workerParams,
            syncQueueDao = syncQueueDao,
            diaryApiService = diaryApiService,
            mealEntryDao = mealEntryDao,
            waterLogDao = waterLogDao,
            diaryRepository = diaryRepository,
            tokenManager = tokenManager,
            json = json
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { diaryApiService.deleteMealEntry(501L) }
        coVerify { syncQueueDao.dequeue(1L) }
    }

    @Test
    fun `doWork skips execution when user is not authenticated`() = runTest {
        every { tokenManager.getAccessToken() } returns flowOf(null)

        val worker = DiarySyncWorker(
            appContext = context,
            workerParams = workerParams,
            syncQueueDao = syncQueueDao,
            diaryApiService = diaryApiService,
            mealEntryDao = mealEntryDao,
            waterLogDao = waterLogDao,
            diaryRepository = diaryRepository,
            tokenManager = tokenManager,
            json = json
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 0) { syncQueueDao.getAllPendingMutations() }
    }
}
