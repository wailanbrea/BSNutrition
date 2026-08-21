package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Query("SELECT * FROM water_logs WHERE log_date = :date AND is_deleted = 0 ORDER BY id ASC")
    fun observeWaterLogsForDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE log_date = :date AND is_deleted = 0 ORDER BY id ASC")
    suspend fun getWaterLogsForDate(date: String): List<WaterLogEntity>

    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_logs WHERE log_date = :date AND is_deleted = 0")
    fun observeTotalWaterForDate(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: WaterLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<WaterLogEntity>)

    @Query("UPDATE water_logs SET is_deleted = 1, sync_status = 'pending_delete', updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM water_logs WHERE sync_status != 'synced'")
    suspend fun getPendingSyncLogs(): List<WaterLogEntity>

    @Query("UPDATE water_logs SET sync_status = 'synced' WHERE id = :id")
    suspend fun markSynced(id: Long)
}
