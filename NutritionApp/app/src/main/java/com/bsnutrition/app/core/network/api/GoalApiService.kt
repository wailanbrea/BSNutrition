package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.model.CalculateGoalRequestDto
import com.bsnutrition.app.core.network.model.CalculatedGoalResponseDto
import com.bsnutrition.app.core.network.model.GoalContainerDto
import com.bsnutrition.app.core.network.model.SaveGoalRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface GoalApiService {

    @POST("goals/calculate")
    suspend fun calculateGoal(
        @Body request: CalculateGoalRequestDto
    ): Response<CalculatedGoalResponseDto>

    @GET("goals/current")
    suspend fun getCurrentGoal(): Response<GoalContainerDto>

    @PUT("goals")
    suspend fun updateGoal(
        @Body request: SaveGoalRequestDto
    ): Response<GoalContainerDto>
}
