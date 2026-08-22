package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.dto.LogWaterRequest
import com.bsnutrition.app.core.network.dto.LogWeightRequest
import com.bsnutrition.app.core.network.dto.StatisticsResponse
import com.bsnutrition.app.core.network.dto.WaterLogsResponse
import com.bsnutrition.app.core.network.dto.WeightLogsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProgressApiService {

    // Water endpoints
    @GET("water/logs")
    suspend fun getWaterLogs(
        @Query("date") date: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<WaterLogsResponse>

    @POST("water/logs")
    suspend fun logWater(
        @Body request: LogWaterRequest
    ): Response<WaterLogsResponse>

    @DELETE("water/logs/{id}")
    suspend fun deleteWaterLog(
        @Path("id") id: Long
    ): Response<Map<String, String>>

    // Weight endpoints
    @GET("weight/logs")
    suspend fun getWeightLogs(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("limit") limit: Int? = 30
    ): Response<WeightLogsResponse>

    @POST("weight/logs")
    suspend fun logWeight(
        @Body request: LogWeightRequest
    ): Response<WeightLogsResponse>

    @DELETE("weight/logs/{id}")
    suspend fun deleteWeightLog(
        @Path("id") id: Long
    ): Response<Map<String, String>>

    // Statistics endpoint
    @GET("statistics/summary")
    suspend fun getStatistics(
        @Query("period") period: String = "7d"
    ): Response<StatisticsResponse>
}
