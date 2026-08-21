package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.BsnDispatchers
import com.bsnutrition.app.core.common.Dispatcher
import com.bsnutrition.app.core.common.Result
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
import com.bsnutrition.app.core.model.DailyDiary
import com.bsnutrition.app.core.model.DailySummary
import com.bsnutrition.app.core.model.FoodLogEntry
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.MealSummaryInfo
import com.bsnutrition.app.core.model.WaterLog
import com.bsnutrition.app.core.network.api.DiaryApiService
import com.bsnutrition.app.core.network.model.AddMealEntryRequestDto
import com.bsnutrition.app.core.network.model.CopyDayRequestDto
import com.bsnutrition.app.core.network.model.CopyMealRequestDto
import com.bsnutrition.app.core.network.model.LogWaterRequestDto
import com.bsnutrition.app.core.network.model.UpdateMealEntryRequestDto
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryApiService: DiaryApiService,
    private val diaryDao: DiaryDao,
    private val mealEntryDao: MealEntryDao,
    private val waterLogDao: WaterLogDao,
    private val syncQueueDao: SyncQueueDao,
    @Dispatcher(BsnDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : DiaryRepository {

    override fun observeDiaryDay(date: String): Flow<DailyDiary?> {
        return diaryDao.observeDiaryByDate(date).map { diaryWithMeals ->
            diaryWithMeals?.toDomain()
        }
    }

    override suspend fun getDiaryDay(date: String): Result<DailyDiary> = withContext(ioDispatcher) {
        // 1. Check local Room first
        val localDiaryWithMeals = diaryDao.getDiaryByDate(date)
        if (localDiaryWithMeals != null) {
            // Trigger background refresh silently
            refreshDiaryFromNetwork(date)
            return@withContext Result.Success(localDiaryWithMeals.toDomain())
        }

        // 2. Fetch from network and cache in Room
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.getDiaryDay(date)
        }

        when (apiResult) {
            is Result.Success -> {
                val diaryDomain = apiResult.data.data.toDomain()
                cacheDiaryInRoom(diaryDomain)
                Result.Success(diaryDomain)
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun addMealEntry(
        date: String,
        mealType: String,
        foodId: Long?,
        portionId: Long?,
        quantity: Double,
        unit: String?,
        customName: String?,
        calories: Int?,
        proteinG: Double?,
        carbsG: Double?,
        fatG: Double?,
        clientId: String?,
        source: String?
    ): Result<FoodLogEntry> = withContext(ioDispatcher) {
        val resolvedClientId = clientId ?: UUID.randomUUID().toString()
        val entryName = customName ?: "Alimento"
        val entryCalories = calories ?: 0
        val entryProtein = proteinG ?: 0.0
        val entryCarbs = carbsG ?: 0.0
        val entryFat = fatG ?: 0.0

        // 1. Get or create local diary & meal
        var diaryWithMeals = diaryDao.getDiaryByDate(date)
        if (diaryWithMeals == null) {
            val diaryId = diaryDao.insertOrUpdateDiary(
                DiaryEntity(user_id = 0, diary_date = date)
            )
            val defaultMeals = listOf(
                MealEntity(diary_id = diaryId, meal_type = "breakfast", name = "Desayuno", sort_order = 1),
                MealEntity(diary_id = diaryId, meal_type = "lunch", name = "Almuerzo", sort_order = 2),
                MealEntity(diary_id = diaryId, meal_type = "dinner", name = "Cena", sort_order = 3),
                MealEntity(diary_id = diaryId, meal_type = "snack", name = "Meriendas", sort_order = 4)
            )
            diaryDao.insertMeals(defaultMeals)
            diaryWithMeals = diaryDao.getDiaryByDate(date)
        }

        val targetMeal = diaryWithMeals?.mealsWithEntries?.find { it.meal.meal_type == mealType }?.meal
            ?: MealEntity(diary_id = diaryWithMeals?.diary?.id ?: 1L, meal_type = mealType, name = mealType)

        // 2. Insert locally with pending_create
        val localEntry = MealEntryEntity(
            client_id = resolvedClientId,
            meal_id = targetMeal.id,
            food_id = foodId,
            portion_id = portionId,
            custom_name = entryName,
            quantity = quantity,
            unit = unit ?: "porción",
            calories_snapshot = entryCalories,
            protein_snapshot = entryProtein,
            carbs_snapshot = entryCarbs,
            fat_snapshot = entryFat,
            source = source ?: "catalog",
            sync_status = "pending_create"
        )
        val localId = mealEntryDao.insertOrUpdate(localEntry)

        // 3. Enqueue mutation
        val request = AddMealEntryRequestDto(
            mealType = mealType,
            foodId = foodId,
            portionId = portionId,
            customName = entryName,
            quantity = quantity,
            unit = unit,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            clientId = resolvedClientId,
            source = source
        )
        val queueId = syncQueueDao.enqueue(
            SyncQueueEntity(
                entity_type = "meal_entry",
                entity_id = localId,
                client_id = resolvedClientId,
                operation = "CREATE",
                payload_json = json.encodeToString(request)
            )
        )

        // 4. Fire async network reconciliation
        tryReconcileEntryCreate(date, request, localId, queueId)

        Result.Success(
            FoodLogEntry(
                id = localId,
                clientId = resolvedClientId,
                mealId = targetMeal.id,
                foodId = foodId,
                portionId = portionId,
                customName = entryName,
                quantity = quantity,
                unit = unit ?: "porción",
                grams = 100.0,
                caloriesSnapshot = entryCalories,
                proteinSnapshot = entryProtein,
                carbsSnapshot = entryCarbs,
                fatSnapshot = entryFat,
                source = source ?: "catalog"
            )
        )
    }

    override suspend fun updateMealEntry(
        id: Long,
        quantity: Double?,
        portionId: Long?,
        unit: String?,
        customName: String?,
        calories: Int?,
        proteinG: Double?,
        carbsG: Double?,
        fatG: Double?
    ): Result<FoodLogEntry> = withContext(ioDispatcher) {
        val existing = mealEntryDao.getEntryById(id)
        if (existing != null) {
            val updated = existing.copy(
                quantity = quantity ?: existing.quantity,
                portion_id = portionId ?: existing.portion_id,
                unit = unit ?: existing.unit,
                custom_name = customName ?: existing.custom_name,
                calories_snapshot = calories ?: existing.calories_snapshot,
                protein_snapshot = proteinG ?: existing.protein_snapshot,
                carbs_snapshot = carbsG ?: existing.carbs_snapshot,
                fat_snapshot = fatG ?: existing.fat_snapshot,
                sync_status = "pending_update",
                updated_at = System.currentTimeMillis()
            )
            mealEntryDao.insertOrUpdate(updated)

            val request = UpdateMealEntryRequestDto(
                quantity = quantity,
                portionId = portionId,
                unit = unit,
                customName = customName,
                calories = calories,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG
            )

            val queueId = syncQueueDao.enqueue(
                SyncQueueEntity(
                    entity_type = "meal_entry",
                    entity_id = id,
                    client_id = existing.client_id,
                    operation = "UPDATE",
                    payload_json = json.encodeToString(request)
                )
            )

            // Async network call
            tryReconcileEntryUpdate(id, request, queueId)

            return@withContext Result.Success(updated.toDomain())
        }

        // Fallback directly to API if not present locally
        val request = UpdateMealEntryRequestDto(
            quantity = quantity,
            portionId = portionId,
            unit = unit,
            customName = customName,
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG
        )
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.updateMealEntry(id, request)
        }
        when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun deleteMealEntry(id: Long): Result<Unit> = withContext(ioDispatcher) {
        mealEntryDao.softDeleteById(id)

        val queueId = syncQueueDao.enqueue(
            SyncQueueEntity(
                entity_type = "meal_entry",
                entity_id = id,
                operation = "DELETE",
                payload_json = "{}"
            )
        )

        tryReconcileEntryDelete(id, queueId)
        Result.Success(Unit)
    }

    override suspend fun copyMeal(
        sourceMealId: Long,
        targetDate: String,
        targetMealType: String
    ): Result<MealLog> = withContext(ioDispatcher) {
        val request = CopyMealRequestDto(
            sourceMealId = sourceMealId,
            targetDate = targetDate,
            targetMealType = targetMealType
        )
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.copyMeal(request)
        }
        when (apiResult) {
            is Result.Success -> {
                refreshDiaryFromNetwork(targetDate)
                Result.Success(apiResult.data.data.toDomain())
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun copyDay(
        sourceDate: String,
        targetDate: String
    ): Result<DailyDiary> = withContext(ioDispatcher) {
        val request = CopyDayRequestDto(
            sourceDate = sourceDate,
            targetDate = targetDate
        )
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.copyDay(request)
        }
        when (apiResult) {
            is Result.Success -> {
                val domain = apiResult.data.data.toDomain()
                cacheDiaryInRoom(domain)
                Result.Success(domain)
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override fun observeWaterLogs(date: String): Flow<List<WaterLog>> {
        return waterLogDao.observeWaterLogsForDate(date).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeTotalWater(date: String): Flow<Int> {
        return waterLogDao.observeTotalWaterForDate(date)
    }

    override suspend fun getWaterLogs(date: String): Result<List<WaterLog>> = withContext(ioDispatcher) {
        val localLogs = waterLogDao.getWaterLogsForDate(date)
        if (localLogs.isNotEmpty()) {
            refreshWaterFromNetwork(date)
            return@withContext Result.Success(localLogs.map { it.toDomain() })
        }

        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.getWaterLogs(date)
        }
        when (apiResult) {
            is Result.Success -> {
                val domainList = apiResult.data.data.map { it.toDomain() }
                waterLogDao.insertAll(domainList.map { it.toEntity() })
                Result.Success(domainList)
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun logWater(
        date: String,
        amountMl: Int,
        clientId: String?,
        source: String?
    ): Result<WaterLog> = withContext(ioDispatcher) {
        val resolvedClientId = clientId ?: UUID.randomUUID().toString()

        val entity = WaterLogEntity(
            client_id = resolvedClientId,
            log_date = date,
            amount_ml = amountMl,
            source = source ?: "manual",
            sync_status = "pending_create"
        )
        val localId = waterLogDao.insertOrUpdate(entity)

        val request = LogWaterRequestDto(
            amountMl = amountMl,
            clientId = resolvedClientId,
            source = source ?: "manual"
        )
        val queueId = syncQueueDao.enqueue(
            SyncQueueEntity(
                entity_type = "water_log",
                entity_id = localId,
                client_id = resolvedClientId,
                operation = "CREATE",
                payload_json = json.encodeToString(request)
            )
        )

        tryReconcileWaterCreate(date, request, localId, queueId)

        Result.Success(
            WaterLog(
                id = localId,
                clientId = resolvedClientId,
                logDate = date,
                amountMl = amountMl,
                source = source ?: "manual"
            )
        )
    }

    override suspend fun deleteWaterLog(id: Long): Result<Unit> = withContext(ioDispatcher) {
        waterLogDao.softDeleteById(id)

        val queueId = syncQueueDao.enqueue(
            SyncQueueEntity(
                entity_type = "water_log",
                entity_id = id,
                operation = "DELETE",
                payload_json = "{}"
            )
        )

        tryReconcileWaterDelete(id, queueId)
        Result.Success(Unit)
    }

    override suspend fun getDailySummary(date: String): Result<DailySummary> = withContext(ioDispatcher) {
        val apiResult = safeApiCall(ioDispatcher, json) {
            diaryApiService.getDailySummary(date)
        }
        when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun syncPendingMutations(): Result<Int> = withContext(ioDispatcher) {
        val pending = syncQueueDao.getAllPendingMutations()
        var syncedCount = 0

        for (mutation in pending) {
            try {
                when (mutation.entity_type) {
                    "meal_entry" -> {
                        if (mutation.operation == "DELETE" && mutation.entity_id != null) {
                            diaryApiService.deleteMealEntry(mutation.entity_id)
                            syncQueueDao.dequeue(mutation.id)
                            syncedCount++
                        }
                    }
                    "water_log" -> {
                        if (mutation.operation == "DELETE" && mutation.entity_id != null) {
                            diaryApiService.deleteWaterLog(mutation.entity_id)
                            syncQueueDao.dequeue(mutation.id)
                            syncedCount++
                        }
                    }
                }
            } catch (e: Exception) {
                syncQueueDao.recordAttempt(mutation.id, e.message)
            }
        }

        Result.Success(syncedCount)
    }

    private fun tryReconcileEntryCreate(date: String, request: AddMealEntryRequestDto, localId: Long, queueId: Long) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val res = diaryApiService.addMealEntry(date, request)
                mealEntryDao.insertOrUpdate(res.data.toEntity().copy(id = localId, sync_status = "synced"))
                syncQueueDao.dequeue(queueId)
            } catch (_: Exception) {
                // Keep pending in Room and SyncQueue for background sync worker
            }
        }
    }

    private fun tryReconcileEntryUpdate(id: Long, request: UpdateMealEntryRequestDto, queueId: Long) {
        CoroutineScope(ioDispatcher).launch {
            try {
                diaryApiService.updateMealEntry(id, request)
                mealEntryDao.markSynced(id)
                syncQueueDao.dequeue(queueId)
            } catch (_: Exception) {}
        }
    }

    private fun tryReconcileEntryDelete(id: Long, queueId: Long) {
        CoroutineScope(ioDispatcher).launch {
            try {
                diaryApiService.deleteMealEntry(id)
                syncQueueDao.dequeue(queueId)
            } catch (_: Exception) {}
        }
    }

    private fun tryReconcileWaterCreate(date: String, request: LogWaterRequestDto, localId: Long, queueId: Long) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val res = diaryApiService.logWater(date, request)
                waterLogDao.insertOrUpdate(res.data.toEntity().copy(id = localId, sync_status = "synced"))
                syncQueueDao.dequeue(queueId)
            } catch (_: Exception) {}
        }
    }

    private fun tryReconcileWaterDelete(id: Long, queueId: Long) {
        CoroutineScope(ioDispatcher).launch {
            try {
                diaryApiService.deleteWaterLog(id)
                syncQueueDao.dequeue(queueId)
            } catch (_: Exception) {}
        }
    }

    private fun refreshDiaryFromNetwork(date: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val res = diaryApiService.getDiaryDay(date)
                cacheDiaryInRoom(res.data.toDomain())
            } catch (_: Exception) {}
        }
    }

    private fun refreshWaterFromNetwork(date: String) {
        CoroutineScope(ioDispatcher).launch {
            try {
                val res = diaryApiService.getWaterLogs(date)
                waterLogDao.insertAll(res.data.map { it.toDomain().toEntity() })
            } catch (_: Exception) {}
        }
    }

    private suspend fun cacheDiaryInRoom(diary: DailyDiary) {
        val diaryId = diaryDao.insertOrUpdateDiary(
            DiaryEntity(
                id = diary.id,
                user_id = diary.userId,
                diary_date = diary.diaryDate,
                timezone = diary.timezone,
                notes = diary.notes,
                sync_status = "synced"
            )
        )

        val mealEntities = diary.meals.map { meal ->
            MealEntity(
                id = meal.id,
                diary_id = diaryId,
                meal_type = meal.mealType,
                name = meal.name,
                sort_order = meal.sortOrder,
                sync_status = "synced"
            )
        }
        diaryDao.insertMeals(mealEntities)

        val allEntries = diary.meals.flatMap { meal ->
            meal.entries.map { entry ->
                MealEntryEntity(
                    id = entry.id,
                    client_id = entry.clientId,
                    meal_id = meal.id,
                    food_id = entry.foodId,
                    portion_id = entry.portionId,
                    custom_name = entry.customName,
                    quantity = entry.quantity,
                    unit = entry.unit,
                    grams = entry.grams,
                    calories_snapshot = entry.caloriesSnapshot,
                    protein_snapshot = entry.proteinSnapshot,
                    carbs_snapshot = entry.carbsSnapshot,
                    fat_snapshot = entry.fatSnapshot,
                    fiber_snapshot = entry.fiberSnapshot,
                    sodium_snapshot = entry.sodiumSnapshot,
                    sugar_snapshot = entry.sugarSnapshot,
                    source = entry.source,
                    version = entry.version,
                    sync_status = "synced"
                )
            }
        }
        if (allEntries.isNotEmpty()) {
            mealEntryDao.insertAll(allEntries)
        }
    }
}

private fun DiaryWithMeals.toDomain(): DailyDiary {
    val mealsDomain = mealsWithEntries.map { mwe ->
        MealLog(
            id = mwe.meal.id,
            diaryId = mwe.meal.diary_id,
            mealType = mwe.meal.meal_type,
            name = mwe.meal.name,
            sortOrder = mwe.meal.sort_order,
            totalCalories = mwe.entries.sumOf { it.calories_snapshot },
            totalProteinG = mwe.entries.sumOf { it.protein_snapshot },
            totalCarbsG = mwe.entries.sumOf { it.carbs_snapshot },
            totalFatG = mwe.entries.sumOf { it.fat_snapshot },
            entries = mwe.entries.map { it.toDomain() }
        )
    }

    val totalCals = mealsDomain.sumOf { it.totalCalories }
    val totalProtein = mealsDomain.sumOf { it.totalProteinG }
    val totalCarbs = mealsDomain.sumOf { it.totalCarbsG }
    val totalFat = mealsDomain.sumOf { it.totalFatG }

    return DailyDiary(
        id = diary.id,
        userId = diary.user_id,
        diaryDate = diary.diary_date,
        timezone = diary.timezone,
        notes = diary.notes,
        summary = DailySummary(
            date = diary.diary_date,
            calories = totalCals,
            proteinG = totalProtein,
            carbsG = totalCarbs,
            fatG = totalFat,
            meals = mealsDomain.map {
                MealSummaryInfo(
                    id = it.id,
                    mealType = it.mealType,
                    name = it.name,
                    calories = it.totalCalories,
                    proteinG = it.totalProteinG,
                    carbsG = it.totalCarbsG,
                    fatG = it.totalFatG,
                    entriesCount = it.entries.size
                )
            }
        ),
        meals = mealsDomain
    )
}

private fun MealEntryEntity.toDomain() = FoodLogEntry(
    id = id,
    clientId = client_id,
    mealId = meal_id,
    foodId = food_id,
    portionId = portion_id,
    customName = custom_name,
    quantity = quantity,
    unit = unit,
    grams = grams,
    caloriesSnapshot = calories_snapshot,
    proteinSnapshot = protein_snapshot,
    carbsSnapshot = carbs_snapshot,
    fatSnapshot = fat_snapshot,
    fiberSnapshot = fiber_snapshot,
    sodiumSnapshot = sodium_snapshot,
    sugarSnapshot = sugar_snapshot,
    source = source,
    version = version
)

private fun WaterLogEntity.toDomain() = WaterLog(
    id = id,
    clientId = client_id,
    logDate = log_date,
    amountMl = amount_ml,
    occurredAt = occurred_at,
    source = source
)

private fun WaterLog.toEntity() = WaterLogEntity(
    id = id,
    client_id = clientId,
    log_date = logDate,
    amount_ml = amountMl,
    occurred_at = occurredAt,
    source = source
)

private fun com.bsnutrition.app.core.network.model.MealEntryDto.toEntity() = MealEntryEntity(
    id = id,
    client_id = clientId,
    meal_id = mealId,
    food_id = foodId,
    portion_id = portionId,
    custom_name = customName,
    quantity = quantity,
    unit = unit,
    grams = grams,
    calories_snapshot = caloriesSnapshot,
    protein_snapshot = proteinSnapshot,
    carbs_snapshot = carbsSnapshot,
    fat_snapshot = fatSnapshot,
    fiber_snapshot = fiberSnapshot,
    sodium_snapshot = sodiumSnapshot,
    sugar_snapshot = sugarSnapshot,
    source = source,
    version = version
)

private fun com.bsnutrition.app.core.network.model.WaterLogDto.toEntity() = WaterLogEntity(
    id = id,
    client_id = clientId,
    log_date = logDate,
    amount_ml = amountMl,
    occurred_at = occurredAt,
    source = source
)
