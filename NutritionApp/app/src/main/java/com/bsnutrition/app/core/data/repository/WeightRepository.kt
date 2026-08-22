package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.WeightLogEntity
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun observeAllWeightLogs(): Flow<List<WeightLogEntity>>
    suspend fun getLatestWeight(): WeightLogEntity?
    suspend fun logWeight(date: String, weightKg: Double, source: String = "manual", notes: String? = null): Result<WeightLogEntity>
    suspend fun deleteWeightLog(id: Long): Result<Unit>
    suspend fun syncWeightLogs(): Result<Int>
}
