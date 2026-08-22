package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.api.SubscriptionApiService
import com.bsnutrition.app.core.network.dto.SubscriptionDataDto
import com.bsnutrition.app.core.network.dto.VerifyPurchaseRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionApiService: SubscriptionApiService
) : SubscriptionRepository {

    override suspend fun getStatus(): Result<SubscriptionDataDto> = withContext(Dispatchers.IO) {
        try {
            val response = subscriptionApiService.getStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(Exception("Error al consultar suscripción (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de red al consultar suscripción.")
        }
    }

    override suspend fun verifyPlayPurchase(
        productId: String,
        purchaseToken: String,
        orderId: String?
    ): Result<SubscriptionDataDto> = withContext(Dispatchers.IO) {
        try {
            val response = subscriptionApiService.verifyPlayPurchase(
                VerifyPurchaseRequest(
                    productId = productId,
                    purchaseToken = purchaseToken,
                    orderId = orderId
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data.status)
            } else {
                Result.Error(Exception("Error al validar compra (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de red al validar compra con Google Play.")
        }
    }
}
