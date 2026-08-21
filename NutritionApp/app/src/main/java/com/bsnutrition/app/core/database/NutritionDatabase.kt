package com.bsnutrition.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
