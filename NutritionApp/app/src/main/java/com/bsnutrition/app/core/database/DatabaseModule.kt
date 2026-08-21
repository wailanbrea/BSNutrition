package com.bsnutrition.app.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DB_NAME = "bsnutrition.db"

    @Provides
    @Singleton
    fun provideNutritionDatabase(
        @ApplicationContext context: Context
    ): NutritionDatabase {
        return Room.databaseBuilder(
            context,
            NutritionDatabase::class.java,
            DB_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: NutritionDatabase): UserDao {
        return database.userDao()
    }
}
