package com.bsnutrition.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionStatusResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: SubscriptionDataDto
)

@Serializable
data class SubscriptionDataDto(
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("tier") val tier: String = "free",
    @SerialName("active_subscription") val activeSubscription: ActiveSubscriptionDto? = null,
    @SerialName("entitlements") val entitlements: EntitlementsDto = EntitlementsDto(),
    @SerialName("products") val products: List<SubscriptionProductDto> = emptyList(),
    @SerialName("quotas") val quotas: QuotaStatusDto? = null
)

@Serializable
data class ActiveSubscriptionDto(
    @SerialName("id") val id: Long,
    @SerialName("plan_id") val planId: String,
    @SerialName("status") val status: String,
    @SerialName("provider") val provider: String,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("auto_renewing") val autoRenewing: Boolean = true
)

@Serializable
data class EntitlementsDto(
    @SerialName("unlimited_ai_photo") val unlimitedAiPhoto: Boolean = false,
    @SerialName("unlimited_ai_voice") val unlimitedAiVoice: Boolean = false,
    @SerialName("advanced_analytics_90d") val advancedAnalytics90d: Boolean = false,
    @SerialName("unlimited_recipes") val unlimitedRecipes: Boolean = true,
    @SerialName("health_connect_sync") val healthConnectSync: Boolean = true,
    @SerialName("ad_free") val adFree: Boolean = true
)

@Serializable
data class SubscriptionProductDto(
    @SerialName("sku") val sku: String,
    @SerialName("name") val name: String,
    @SerialName("price_usd") val priceUsd: String,
    @SerialName("billing_period") val billingPeriod: String,
    @SerialName("trial_days") val trialDays: Int? = null,
    @SerialName("discount_pct") val discountPct: Int? = null
)

@Serializable
data class QuotaStatusDto(
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("plan") val plan: String = "free",
    @SerialName("photo_analyses") val photoAnalyses: QuotaItemDto,
    @SerialName("text_parses") val textParses: QuotaItemDto
)

@Serializable
data class QuotaItemDto(
    @SerialName("unlimited") val unlimited: Boolean = false,
    @SerialName("used") val used: Int = 0,
    @SerialName("remaining") val remaining: Int = 3,
    @SerialName("limit") val limit: Int = 3
)

@Serializable
data class VerifyPurchaseRequest(
    @SerialName("product_id") val productId: String,
    @SerialName("purchase_token") val purchaseToken: String,
    @SerialName("order_id") val orderId: String? = null
)

@Serializable
data class VerifyPurchaseResponse(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String,
    @SerialName("data") val data: VerifyPurchaseDataDto
)

@Serializable
data class VerifyPurchaseDataDto(
    @SerialName("subscription") val subscription: ActiveSubscriptionDto,
    @SerialName("status") val status: SubscriptionDataDto
)
