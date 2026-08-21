package com.bsnutrition.app.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.data.repository.FoodRepository
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed interface BarcodeScannerUiState {
    data object Scanning : BarcodeScannerUiState
    data class Loading(val barcode: String) : BarcodeScannerUiState
    data class Success(val food: FoodDetail, val barcode: String) : BarcodeScannerUiState
    data class NotFound(val barcode: String) : BarcodeScannerUiState
    data class Error(val message: String) : BarcodeScannerUiState
    data class AddedToDiary(val foodName: String, val calories: Int) : BarcodeScannerUiState
}

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BarcodeScannerUiState>(BarcodeScannerUiState.Scanning)
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    private var lastScannedBarcode: String? = null

    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value is BarcodeScannerUiState.Loading || _uiState.value is BarcodeScannerUiState.Success) {
            return
        }

        lastScannedBarcode = barcode
        _uiState.update { BarcodeScannerUiState.Loading(barcode) }

        viewModelScope.launch {
            when (val result = foodRepository.getFoodByBarcode(barcode)) {
                is Result.Success -> {
                    _uiState.update { BarcodeScannerUiState.Success(result.data, barcode) }
                }
                is Result.Error -> {
                    if (result.code == 404 || result.message.contains("404") || result.message.contains("No se encontró")) {
                        _uiState.update { BarcodeScannerUiState.NotFound(barcode) }
                    } else {
                        _uiState.update { BarcodeScannerUiState.Error(result.message) }
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun addScannedFoodToDiary(
        food: FoodDetail,
        mealType: String = "lunch",
        date: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        quantity: Double = 1.0,
        portionId: Long? = null,
        calories: Int,
        proteinG: Double,
        carbsG: Double,
        fatG: Double,
        unit: String? = null
    ) {
        viewModelScope.launch {
            val result = diaryRepository.addMealEntry(
                date = date,
                mealType = mealType,
                foodId = food.id,
                portionId = portionId,
                quantity = quantity,
                unit = unit ?: food.portions.find { it.id == portionId }?.name ?: "porción",
                customName = "${food.canonicalName}${food.brand?.let { " (${it.name})" } ?: ""}",
                calories = calories,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG,
                source = "barcode"
            )

            if (result is Result.Success) {
                foodRepository.recordRecentFood(
                    FoodSummary(
                        id = food.id,
                        canonicalName = food.canonicalName,
                        brandName = food.brand?.name,
                        categoryName = food.category?.name,
                        calories100g = food.nutrients.find { it.nutrientCode == "ENERGY_KCAL" }?.amountPer100g ?: 0,
                        protein100g = food.nutrients.find { it.nutrientCode == "PROTEIN_G" }?.amountPer100g ?: 0.0,
                        carbs100g = food.nutrients.find { it.nutrientCode == "CARBS_G" }?.amountPer100g ?: 0.0,
                        fat100g = food.nutrients.find { it.nutrientCode == "FAT_G" }?.amountPer100g ?: 0.0,
                        countryCode = food.countryCode,
                        verified = food.verified
                    )
                )
                _uiState.update { BarcodeScannerUiState.AddedToDiary(food.canonicalName, calories) }
            } else if (result is Result.Error) {
                _uiState.update { BarcodeScannerUiState.Error(result.message) }
            }
        }
    }

    fun resumeScanning() {
        _uiState.update { BarcodeScannerUiState.Scanning }
    }
}
