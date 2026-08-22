package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.WeightLogDao
import com.bsnutrition.app.core.database.WeightLogEntity
import com.bsnutrition.app.core.network.api.ProgressApiService
import com.bsnutrition.app.core.network.dto.LogWeightRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val weightLogDao: WeightLogDao,
    private val progressApiService: ProgressApiService
) : WeightRepository {

    override fun observeAllWeightLogs(): Flow<List<WeightLogEntity>> {
        return weightLogDao.observeAllWeightLogs()
    }

    override suspend fun getLatestWeight(): WeightLogEntity? {
        return weightLogDao.getLatestWeight()
    }

    override suspend fun logWeight(
        date: String,
        weightKg: Double,
        source: String,
        notes: String?
    ): Result<WeightLogEntity> = withContext(Dispatchers.IO) {
        val clientId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = WeightLogEntity(
            clientId = clientId,
            logDate = date,
            weightKg = weightKg,
            occurredAt = now,
            source = source,
            notes = notes,
            syncStatus = "pending_insert",
            version = 1
        )

        val localId = weightLogDao.insertOrUpdate(entity)
        val saved = entity.copy(id = localId)

        // Try immediate network sync
        try {
            val response = progressApiService.logWeight(
                LogWeightRequest(
                    clientId = clientId,
                    logDate = date,
                    weightKg = weightKg,
                    source = source,
                    notes = notes
                )
            )
            if (response.isSuccessful) {
                weightLogDao.insertOrUpdate(saved.copy(syncStatus = "synced"))
            }
        } catch (_: Exception) {
            // Retained in Room
        }

        Result.Success(saved)
    }

    override suspend fun deleteWeightLog(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        weightLogDao.softDeleteById(id)
        try {
            progressApiService.deleteWeightLog(id)
        } catch (_: Exception) {
            // Retained for sync
        }
        Result.Success(Unit)
    }

    override suspend fun syncWeightLogs(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = progressApiService.getWeightLogs(limit = 100)
            if (response.isSuccessful && response.body() != null) {
                val remoteLogs = response.body()!!.data.logs
                remoteLogs.forEach { log ->
                    weightLogDao.insertOrUpdate(
                        WeightLogEntity(
                            id = log.id,
                            clientId = log.clientId ?: UUID.randomUUID().toString(),
                            logDate = log.logDate,
                            weightKg = log.weightKg,
                            occurredAt = System.currentTimeMillis(),
                            source = log.source,
                            notes = log.notes,
                            syncStatus = "synced",
                            version = 1
                        )
                    )
                }
                Result.Success(remoteLogs.size)
            } else {
                Result.Error(Exception("Error al sincronizar peso (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de red al sincronizar peso.")
        }
    }
}
