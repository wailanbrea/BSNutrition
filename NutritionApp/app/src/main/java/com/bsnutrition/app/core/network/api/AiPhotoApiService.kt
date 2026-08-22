package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.dto.AiPhotoAnalysisApiResponse
import com.bsnutrition.app.core.network.dto.ConfirmPhotoAnalysisRequest
import com.bsnutrition.app.core.network.dto.ConfirmPhotoAnalysisResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AiPhotoApiService {

    @Multipart
    @POST("ai/photo/analyze")
    suspend fun analyzePhoto(
        @Part image: MultipartBody.Part,
        @Part("locale") locale: RequestBody? = null,
        @Part("meal_type") mealType: RequestBody? = null
    ): Response<AiPhotoAnalysisApiResponse>

    @GET("ai/photo/analyses/{id}")
    suspend fun getAnalysis(
        @Path("id") id: Long
    ): Response<AiPhotoAnalysisApiResponse>

    @POST("ai/photo/analyses/{id}/confirm")
    suspend fun confirmAnalysis(
        @Path("id") id: Long,
        @Body request: ConfirmPhotoAnalysisRequest
    ): Response<ConfirmPhotoAnalysisResponse>

    @POST("ai/text/parse")
    suspend fun parseMealText(
        @Body request: com.bsnutrition.app.core.network.dto.ParseTextRequest
    ): Response<AiPhotoAnalysisApiResponse>
}

