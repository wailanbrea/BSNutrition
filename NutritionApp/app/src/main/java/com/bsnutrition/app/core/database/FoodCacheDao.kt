package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodCacheDao {

    @Query("SELECT * FROM foods_cache WHERE name LIKE '%' || :query || '%' OR brand_name LIKE '%' || :query || '%' LIMIT :limit")
    fun searchFoodsCache(query: String, limit: Int = 30): Flow<List<FoodCacheEntity>>

    @Query("SELECT * FROM foods_cache WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: Long): FoodCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(food: FoodCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodCacheEntity>)

    @Query("DELETE FROM foods_cache WHERE cached_at < :expirationTimestamp")
    suspend fun cleanExpiredCache(expirationTimestamp: Long)
}
