package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteFoodDao {

    @Query("SELECT * FROM favorite_foods ORDER BY created_at DESC")
    fun getFavoriteFoodsFlow(): Flow<List<FavoriteFoodEntity>>

    @Query("SELECT * FROM favorite_foods ORDER BY created_at DESC")
    suspend fun getFavoriteFoods(): List<FavoriteFoodEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_foods WHERE id = :foodId)")
    suspend fun isFavorite(foodId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(food: FavoriteFoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FavoriteFoodEntity>)

    @Query("DELETE FROM favorite_foods WHERE id = :foodId")
    suspend fun deleteFavorite(foodId: Long)

    @Query("DELETE FROM favorite_foods")
    suspend fun clearFavorites()
}
