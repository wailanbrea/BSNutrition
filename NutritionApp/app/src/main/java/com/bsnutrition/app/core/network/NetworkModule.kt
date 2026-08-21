package com.bsnutrition.app.core.network

import com.bsnutrition.app.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): com.bsnutrition.app.core.network.api.AuthApiService {
        return retrofit.create(com.bsnutrition.app.core.network.api.AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): com.bsnutrition.app.core.network.api.ProfileApiService {
        return retrofit.create(com.bsnutrition.app.core.network.api.ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHealthApiService(retrofit: Retrofit): com.bsnutrition.app.core.network.api.HealthApiService {
        return retrofit.create(com.bsnutrition.app.core.network.api.HealthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGoalApiService(retrofit: Retrofit): com.bsnutrition.app.core.network.api.GoalApiService {
        return retrofit.create(com.bsnutrition.app.core.network.api.GoalApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodApiService(retrofit: Retrofit): com.bsnutrition.app.core.network.api.FoodApiService {
        return retrofit.create(com.bsnutrition.app.core.network.api.FoodApiService::class.java)
    }
}



