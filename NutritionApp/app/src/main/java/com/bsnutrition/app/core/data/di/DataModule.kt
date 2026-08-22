package com.bsnutrition.app.core.data.di

import com.bsnutrition.app.core.data.repository.AuthRepository
import com.bsnutrition.app.core.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        impl: com.bsnutrition.app.core.data.repository.GoalRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.GoalRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: com.bsnutrition.app.core.data.repository.ProfileRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.ProfileRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        impl: com.bsnutrition.app.core.data.repository.FoodRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.FoodRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(
        impl: com.bsnutrition.app.core.data.repository.DiaryRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.DiaryRepository

    @Binds
    @Singleton
    abstract fun bindAiPhotoRepository(
        impl: com.bsnutrition.app.core.data.repository.AiPhotoRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.AiPhotoRepository

    @Binds
    @Singleton
    abstract fun bindWaterRepository(
        impl: com.bsnutrition.app.core.data.repository.WaterRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.WaterRepository

    @Binds
    @Singleton
    abstract fun bindWeightRepository(
        impl: com.bsnutrition.app.core.data.repository.WeightRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.WeightRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        impl: com.bsnutrition.app.core.data.repository.StatisticsRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.StatisticsRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        impl: com.bsnutrition.app.core.data.repository.RecipeRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.RecipeRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: com.bsnutrition.app.core.data.repository.SubscriptionRepositoryImpl
    ): com.bsnutrition.app.core.data.repository.SubscriptionRepository
}








