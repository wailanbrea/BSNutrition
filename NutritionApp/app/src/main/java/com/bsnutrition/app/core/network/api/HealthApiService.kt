package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.model.HealthResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface HealthApiService {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponseDto>
}
