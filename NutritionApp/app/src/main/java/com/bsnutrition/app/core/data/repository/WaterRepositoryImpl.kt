package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.WaterLogDao
import com.bsnutrition.app.core.database.WaterLogEntity
import com.bsnutrition.app.core.network.api.ProgressApiService
import com.bsnutrition.app.core.network.dto.LogWaterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepositoryImpl @Inject constructor(
    private val waterLogDao: WaterLogDao,
    private val progressApiService: ProgressApiService
) : WaterRepository {

    override fun observeWaterLogs(date: String): Flow<List<WaterLogEntity>> {
        return waterLogDao.observeWaterLogsForDate(date)
    }

    override fun observeTotalWater(date: String): Flow<Int> {
        return waterLogDao.observeTotalWaterForDate(date)
    }

    override suspend fun logWater(
        date: String,
        amountMl: Int,
        source: String
    ): Result<WaterLogEntity> = withContext(Dispatchers.IO) {
        val clientId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = WaterLogEntity(
            clientId = clientId,
            logDate = date,
            amountMl = amountMl,
            occurredAt = now,
            source = source,
            syncStatus = "pending_insert",
            version = 1
        )

        val localId = waterLogDao.insertOrUpdate(entity)
        val saved = entity.copy(id = localId)

        // Try immediate network sync
        try {
            val response = progressApiService.logWater(
                LogWaterRequest(
                    clientId = clientId,
                    logDate = date,
                    amountMl = amountMl,
                    source = source
                )
            )
            if (response.isSuccessful) {
                waterLogDao.markSynced(localId)
            }
        } catch (_: Exception) {
            // Retained in Room for background sync worker
        }

        Result.Success(saved)
    }

    override suspend fun deleteWaterLog(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        waterLogDao.softDeleteById(id)
        try {
            progressApiService.deleteWaterLog(id)
        } catch (_: Exception) {
            // Keep pending_delete for sync worker
        }
        Result.Success(Unit)
    }

    override suspend fun syncWaterLogs(date: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = progressApiService.getWaterLogs(date = date)
            if (response.isSuccessful && response.body() != null) {
                val remoteLogs = response.body()!!.data.logs
                val entities = remoteLogs.map { log ->
                    WaterLogEntity(
                        id = log.id,
                        clientId = log.clientId ?: UUID.randomUUID().toString(),
                        logDate = log.logDate,
                        amountMl = log.amountMl,
                        occurredAt = System.currentTimeMillis(),
                        source = log.source,
                        syncStatus = "synced",
                        version = 1
                    )
                }
                waterLogDao.insertAll(entities)
                Result.Success(entities.size)
            } else {
                Result.Error(Exception("Error al sincronizar agua (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de red al sincronizar agua.")
        }
    }
}
