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
}


