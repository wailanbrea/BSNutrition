package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {

    @Query("SELECT * FROM weight_logs WHERE is_deleted = 0 ORDER BY log_date DESC")
    fun observeAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE log_date = :date AND is_deleted = 0 LIMIT 1")
    suspend fun getWeightForDate(date: String): WeightLogEntity?

    @Query("SELECT * FROM weight_logs WHERE is_deleted = 0 ORDER BY log_date DESC LIMIT 1")
    suspend fun getLatestWeight(): WeightLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: WeightLogEntity): Long

    @Query("UPDATE weight_logs SET is_deleted = 1, sync_status = 'pending_delete', updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM weight_logs WHERE sync_status != 'synced'")
    suspend fun getPendingSyncLogs(): List<WeightLogEntity>
}
