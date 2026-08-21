package com.bsnutrition.app.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.database.MealEntryDao
import com.bsnutrition.app.core.database.SyncQueueDao
import com.bsnutrition.app.core.database.WaterLogDao
import com.bsnutrition.app.core.datastore.TokenManager
import com.bsnutrition.app.core.network.api.DiaryApiService
import com.bsnutrition.app.core.network.model.AddMealEntryRequestDto
import com.bsnutrition.app.core.network.model.LogWaterRequestDto
import com.bsnutrition.app.core.network.model.UpdateMealEntryRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class DiarySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val diaryApiService: DiaryApiService,
    private val mealEntryDao: MealEntryDao,
    private val waterLogDao: WaterLogDao,
    private val diaryRepository: DiaryRepository,
    private val tokenManager: TokenManager,
    private val json: Json
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Auth check
        val token = tokenManager.getAccessToken().firstOrNull()
        if (token.isNullOrBlank()) {
            return Result.success()
        }

        try {
            // 2. Drain pending mutations in sync queue (PUSH)
            val pendingMutations = syncQueueDao.getAllPendingMutations()
            for (mutation in pendingMutations) {
                try {
                    when (mutation.entity_type) {
                        "meal_entry" -> {
                            when (mutation.operation) {
                                "CREATE" -> {
                                    val req = json.decodeFromString<AddMealEntryRequestDto>(mutation.payload_json)
                                    val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    val res = diaryApiService.addMealEntry(todayStr, req)
                                    mutation.entity_id?.let { localId ->
                                        mealEntryDao.markSynced(localId)
                                    }
                                    syncQueueDao.dequeue(mutation.id)
                                }
                                "UPDATE" -> {
                                    if (mutation.entity_id != null) {
                                        val req = json.decodeFromString<UpdateMealEntryRequestDto>(mutation.payload_json)
                                        diaryApiService.updateMealEntry(mutation.entity_id, req)
                                        mealEntryDao.markSynced(mutation.entity_id)
                                        syncQueueDao.dequeue(mutation.id)
                                    }
                                }
                                "DELETE" -> {
                                    if (mutation.entity_id != null) {
                                        diaryApiService.deleteMealEntry(mutation.entity_id)
                                        syncQueueDao.dequeue(mutation.id)
                                    }
                                }
                            }
                        }
                        "water_log" -> {
                            when (mutation.operation) {
                                "CREATE" -> {
                                    val req = json.decodeFromString<LogWaterRequestDto>(mutation.payload_json)
                                    val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    val res = diaryApiService.logWater(todayStr, req)
                                    mutation.entity_id?.let { localId ->
                                        waterLogDao.markSynced(localId)
                                    }
                                    syncQueueDao.dequeue(mutation.id)
                                }
                                "DELETE" -> {
                                    if (mutation.entity_id != null) {
                                        diaryApiService.deleteWaterLog(mutation.entity_id)
                                        syncQueueDao.dequeue(mutation.id)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: HttpException) {
                    if (e.code() in 400..499 && e.code() != 429) {
                        // Permanent client error: discard to prevent poison pill loop
                        syncQueueDao.dequeue(mutation.id)
                    } else {
                        syncQueueDao.recordAttempt(mutation.id, e.message())
                        return Result.retry()
                    }
                } catch (e: Exception) {
                    syncQueueDao.recordAttempt(mutation.id, e.message)
                    return Result.retry()
                }
            }

            // 3. Pull recent diary (PULL)
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            diaryRepository.getDiaryDay(todayStr)

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
