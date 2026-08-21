package com.bsnutrition.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Transaction
    @Query("SELECT * FROM diaries WHERE diary_date = :date LIMIT 1")
    fun observeDiaryByDate(date: String): Flow<DiaryWithMeals?>

    @Transaction
    @Query("SELECT * FROM diaries WHERE diary_date = :date LIMIT 1")
    suspend fun getDiaryByDate(date: String): DiaryWithMeals?

    @Query("SELECT * FROM diaries WHERE id = :id LIMIT 1")
    suspend fun getDiaryById(id: Long): DiaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDiary(diary: DiaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>): List<Long>

    @Query("SELECT * FROM meals WHERE diary_id = :diaryId")
    suspend fun getMealsForDiary(diaryId: Long): List<MealEntity>

    @Query("SELECT * FROM meals WHERE diary_id = :diaryId AND meal_type = :mealType LIMIT 1")
    suspend fun getMealByType(diaryId: Long, mealType: String): MealEntity?

    @Query("DELETE FROM diaries WHERE diary_date = :date")
    suspend fun deleteDiaryByDate(date: String)
}
