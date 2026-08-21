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

    @Provides
    fun provideFavoriteFoodDao(database: NutritionDatabase): FavoriteFoodDao {
        return database.favoriteFoodDao()
    }

    @Provides
    fun provideRecentFoodDao(database: NutritionDatabase): RecentFoodDao {
        return database.recentFoodDao()
    }

    @Provides
    fun provideDiaryDao(database: NutritionDatabase): DiaryDao {
        return database.diaryDao()
    }

    @Provides
    fun provideMealEntryDao(database: NutritionDatabase): MealEntryDao {
        return database.mealEntryDao()
    }

    @Provides
    fun provideWaterLogDao(database: NutritionDatabase): WaterLogDao {
        return database.waterLogDao()
    }

    @Provides
    fun provideWeightLogDao(database: NutritionDatabase): WeightLogDao {
        return database.weightLogDao()
    }

    @Provides
    fun provideFoodCacheDao(database: NutritionDatabase): FoodCacheDao {
        return database.foodCacheDao()
    }

    @Provides
    fun provideSyncQueueDao(database: NutritionDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
}



