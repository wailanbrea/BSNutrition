package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFoodDao {

    @Query("SELECT * FROM recent_foods ORDER BY last_used_at DESC LIMIT :limit")
    fun getRecentFoodsFlow(limit: Int = 50): Flow<List<RecentFoodEntity>>

    @Query("SELECT * FROM recent_foods ORDER BY last_used_at DESC LIMIT :limit")
    suspend fun getRecentFoods(limit: Int = 50): List<RecentFoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(food: RecentFoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<RecentFoodEntity>)

    @Query("DELETE FROM recent_foods WHERE id = :foodId")
    suspend fun deleteRecent(foodId: Long)

    @Query("DELETE FROM recent_foods")
    suspend fun clearRecents()
}
