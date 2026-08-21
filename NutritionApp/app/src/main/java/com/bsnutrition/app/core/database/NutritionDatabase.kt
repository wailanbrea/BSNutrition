package com.bsnutrition.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FavoriteFoodEntity::class,
        RecentFoodEntity::class,
        DiaryEntity::class,
        MealEntity::class,
        MealEntryEntity::class,
        WaterLogEntity::class,
        WeightLogEntity::class,
        FoodCacheEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteFoodDao(): FavoriteFoodDao
    abstract fun recentFoodDao(): RecentFoodDao
    abstract fun diaryDao(): DiaryDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun foodCacheDao(): FoodCacheDao
    abstract fun syncQueueDao(): SyncQueueDao
}



