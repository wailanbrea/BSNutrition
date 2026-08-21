package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    suspend fun getAllPendingMutations(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    fun observePendingMutations(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(mutation: SyncQueueEntity): Long

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun dequeue(id: Long)

    @Query("UPDATE sync_queue SET attempts = attempts + 1, last_error = :error WHERE id = :id")
    suspend fun recordAttempt(id: Long, error: String?)

    @Query("DELETE FROM sync_queue WHERE client_id = :clientId")
    suspend fun removeByClientId(clientId: String)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
