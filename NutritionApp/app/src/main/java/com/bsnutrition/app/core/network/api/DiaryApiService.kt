package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.model.AddMealEntryRequestDto
import com.bsnutrition.app.core.network.model.CopyDayRequestDto
import com.bsnutrition.app.core.network.model.CopyMealRequestDto
import com.bsnutrition.app.core.network.model.DailySummaryResponseDto
import com.bsnutrition.app.core.network.model.DiaryDayResponseDto
import com.bsnutrition.app.core.network.model.LogWaterRequestDto
import com.bsnutrition.app.core.network.model.MealEntryResponseDto
import com.bsnutrition.app.core.network.model.MealResponseDto
import com.bsnutrition.app.core.network.model.MessageResponseDto
import com.bsnutrition.app.core.network.model.UpdateMealEntryRequestDto
import com.bsnutrition.app.core.network.model.WaterLogListResponseDto
import com.bsnutrition.app.core.network.model.WaterLogResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DiaryApiService {

    @GET("diary/{date}")
    suspend fun getDiaryDay(
        @Path("date") date: String
    ): DiaryDayResponseDto

    @POST("diary/{date}/entries")
    suspend fun addMealEntry(
        @Path("date") date: String,
        @Body request: AddMealEntryRequestDto
    ): MealEntryResponseDto

    @PUT("diary/entries/{id}")
    suspend fun updateMealEntry(
        @Path("id") id: Long,
        @Body request: UpdateMealEntryRequestDto
    ): MealEntryResponseDto

    @DELETE("diary/entries/{id}")
    suspend fun deleteMealEntry(
        @Path("id") id: Long
    ): MessageResponseDto

    @POST("diary/copy-meal")
    suspend fun copyMeal(
        @Body request: CopyMealRequestDto
    ): MealResponseDto

    @POST("diary/copy-day")
    suspend fun copyDay(
        @Body request: CopyDayRequestDto
    ): DiaryDayResponseDto

    @GET("diary/{date}/water")
    suspend fun getWaterLogs(
        @Path("date") date: String
    ): WaterLogListResponseDto

    @POST("diary/{date}/water")
    suspend fun logWater(
        @Path("date") date: String,
        @Body request: LogWaterRequestDto
    ): WaterLogResponseDto

    @DELETE("diary/water/{id}")
    suspend fun deleteWaterLog(
        @Path("id") id: Long
    ): MessageResponseDto

    @GET("diary/{date}/summary")
    suspend fun getDailySummary(
        @Path("date") date: String
    ): DailySummaryResponseDto
}
