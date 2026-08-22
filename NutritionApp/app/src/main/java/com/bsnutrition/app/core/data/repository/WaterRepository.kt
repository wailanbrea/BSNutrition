package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.WaterLogEntity
import kotlinx.coroutines.flow.Flow

interface WaterRepository {
    fun observeWaterLogs(date: String): Flow<List<WaterLogEntity>>
    fun observeTotalWater(date: String): Flow<Int>
    suspend fun logWater(date: String, amountMl: Int, source: String = "quick_add"): Result<WaterLogEntity>
    suspend fun deleteWaterLog(id: Long): Result<Unit>
    suspend fun syncWaterLogs(date: String): Result<Int>
}
