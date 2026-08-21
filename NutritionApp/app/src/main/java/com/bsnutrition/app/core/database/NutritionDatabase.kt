package com.bsnutrition.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FavoriteFoodEntity::class,
        RecentFoodEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun favoriteFoodDao(): FavoriteFoodDao
    abstract fun recentFoodDao(): RecentFoodDao
}


