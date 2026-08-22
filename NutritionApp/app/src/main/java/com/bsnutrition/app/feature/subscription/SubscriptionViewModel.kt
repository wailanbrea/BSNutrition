package com.bsnutrition.app.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.SubscriptionRepository
import com.bsnutrition.app.core.network.dto.SubscriptionDataDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val subscriptionData: SubscriptionDataDto? = null,
    val selectedProductSku: String = "bsnutrition_pro_yearly",
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = subscriptionRepository.getStatus()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        subscriptionData = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "No se pudo cargar el estado de la suscripción."
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun selectProduct(sku: String) {
        _uiState.value = _uiState.value.copy(selectedProductSku = sku)
    }

    fun purchaseSelectedPlan(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true, errorMessage = null)

            // Simulating Google Play purchase flow confirmation / mock token
            val fakeToken = "play_token_" + System.currentTimeMillis()
            when (val result = subscriptionRepository.verifyPlayPurchase(
                productId = _uiState.value.selectedProductSku,
                purchaseToken = fakeToken
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        subscriptionData = result.data,
                        isPurchasing = false,
                        successMessage = "¡Bienvenido a BSNutrition Pro!"
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isPurchasing = false,
                        errorMessage = result.message ?: "Error al procesar la compra."
                    )
                }
                Result.Loading -> {}
            }
        }
    }
}
