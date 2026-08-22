package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.api.SubscriptionApiService
import com.bsnutrition.app.core.network.dto.ActiveSubscriptionDto
import com.bsnutrition.app.core.network.dto.SubscriptionDataDto
import com.bsnutrition.app.core.network.dto.SubscriptionStatusResponse
import com.bsnutrition.app.core.network.dto.VerifyPurchaseDataDto
import com.bsnutrition.app.core.network.dto.VerifyPurchaseResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SubscriptionRepositoryTest {

    private lateinit var subscriptionApiService: SubscriptionApiService
    private lateinit var repository: SubscriptionRepositoryImpl

    private val sampleSubData = SubscriptionDataDto(
        isPro = true,
        tier = "pro",
        activeSubscription = ActiveSubscriptionDto(
            id = 1L,
            planId = "pro_monthly",
            status = "active",
            provider = "google_play"
        )
    )

    @Before
    fun setUp() {
        subscriptionApiService = mockk(relaxed = true)
        repository = SubscriptionRepositoryImpl(subscriptionApiService)
    }

    @Test
    fun `getStatus returns subscription status from API`() = runTest {
        coEvery { subscriptionApiService.getStatus() } returns Response.success(
            SubscriptionStatusResponse("success", sampleSubData)
        )

        val result = repository.getStatus()
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.isPro)
        assertEquals("pro", data.tier)
    }

    @Test
    fun `verifyPlayPurchase sends token and receives updated status`() = runTest {
        coEvery { subscriptionApiService.verifyPlayPurchase(any()) } returns Response.success(
            VerifyPurchaseResponse(
                status = "success",
                message = "Pro activado",
                data = VerifyPurchaseDataDto(
                    subscription = sampleSubData.activeSubscription!!,
                    status = sampleSubData
                )
            )
        )

        val result = repository.verifyPlayPurchase("bsnutrition_pro_monthly", "token_123")
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.isPro)

        coVerify { subscriptionApiService.verifyPlayPurchase(any()) }
    }
}
