package com.bsnutrition.app.feature.subscription

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.SubscriptionRepository
import com.bsnutrition.app.core.network.dto.SubscriptionDataDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var viewModel: SubscriptionViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleData = SubscriptionDataDto(
        isPro = false,
        tier = "free"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        subscriptionRepository = mockk(relaxed = true)
        coEvery { subscriptionRepository.getStatus() } returns Result.Success(sampleData)
        viewModel = SubscriptionViewModel(subscriptionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads subscription status`() = runTest {
        val state = viewModel.uiState.value
        assertNotNull(state.subscriptionData)
        assertEquals("free", state.subscriptionData?.tier)
    }

    @Test
    fun `selectProduct updates selected SKU`() = runTest {
        viewModel.selectProduct("bsnutrition_pro_monthly")
        assertEquals("bsnutrition_pro_monthly", viewModel.uiState.value.selectedProductSku)
    }

    @Test
    fun `purchaseSelectedPlan calls repository and triggers callback on success`() = runTest {
        coEvery {
            subscriptionRepository.verifyPlayPurchase(any(), any(), any())
        } returns Result.Success(sampleData.copy(isPro = true, tier = "pro"))

        var successCallbackCalled = false
        viewModel.purchaseSelectedPlan {
            successCallbackCalled = true
        }

        assertTrue(successCallbackCalled)
        assertTrue(viewModel.uiState.value.subscriptionData?.isPro == true)
        coVerify { subscriptionRepository.verifyPlayPurchase(any(), any(), any()) }
    }
}
