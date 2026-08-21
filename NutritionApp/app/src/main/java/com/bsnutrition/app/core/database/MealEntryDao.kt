package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {

    @Query("SELECT * FROM meal_entries WHERE meal_id = :mealId AND is_deleted = 0")
    fun observeEntriesForMeal(mealId: Long): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getEntryById(id: Long): MealEntryEntity?

    @Query("SELECT * FROM meal_entries WHERE client_id = :clientId LIMIT 1")
    suspend fun getEntryByClientId(clientId: String): MealEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: MealEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MealEntryEntity>)

    @Query("UPDATE meal_entries SET is_deleted = 1, sync_status = 'pending_delete', updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE meal_entries SET is_deleted = 1, sync_status = 'pending_delete', updated_at = :updatedAt WHERE client_id = :clientId")
    suspend fun softDeleteByClientId(clientId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM meal_entries WHERE sync_status != 'synced'")
    suspend fun getPendingSyncEntries(): List<MealEntryEntity>

    @Query("UPDATE meal_entries SET sync_status = 'synced' WHERE id = :id")
    suspend fun markSynced(id: Long)
}
