package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.dto.SubscriptionStatusResponse
import com.bsnutrition.app.core.network.dto.VerifyPurchaseRequest
import com.bsnutrition.app.core.network.dto.VerifyPurchaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SubscriptionApiService {

    @GET("billing/status")
    suspend fun getStatus(): Response<SubscriptionStatusResponse>

    @POST("billing/verify-play-purchase")
    suspend fun verifyPlayPurchase(
        @Body request: VerifyPurchaseRequest
    ): Response<VerifyPurchaseResponse>
}
