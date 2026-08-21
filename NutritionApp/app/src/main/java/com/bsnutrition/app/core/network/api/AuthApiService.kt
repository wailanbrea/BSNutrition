package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.model.AuthResponseDto
import com.bsnutrition.app.core.network.model.LoginRequestDto
import com.bsnutrition.app.core.network.model.MessageResponseDto
import com.bsnutrition.app.core.network.model.RegisterRequestDto
import com.bsnutrition.app.core.network.model.UserContainerDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponseDto>

    @GET("me")
    suspend fun getMe(): Response<UserContainerDto>

    @DELETE("me")
    suspend fun deleteMe(): Response<MessageResponseDto>
}
