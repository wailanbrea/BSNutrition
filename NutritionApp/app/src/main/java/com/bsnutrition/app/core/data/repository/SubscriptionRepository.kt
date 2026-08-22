package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.dto.SubscriptionDataDto

interface SubscriptionRepository {
    suspend fun getStatus(): Result<SubscriptionDataDto>
    suspend fun verifyPlayPurchase(productId: String, purchaseToken: String, orderId: String? = null): Result<SubscriptionDataDto>
}
