package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.model.ProfileContainerDto
import com.bsnutrition.app.core.network.model.UpdateProfileRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApiService {

    @GET("profile")
    suspend fun getProfile(): Response<ProfileContainerDto>

    @PUT("profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequestDto
    ): Response<ProfileContainerDto>
}
