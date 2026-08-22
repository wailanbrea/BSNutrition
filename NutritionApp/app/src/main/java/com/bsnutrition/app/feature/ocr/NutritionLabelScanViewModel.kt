package com.bsnutrition.app.feature.ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.FoodRepository
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.network.dto.CreateFromLabelRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LabelProductDraft(
    val canonicalName: String = "",
    val brandName: String = "",
    val barcode: String = "",
    val servingName: String = "1 porción",
    val servingGrams: Double = 100.0,
    val calories100g: Int = 0,
    val protein100g: Double = 0.0,
    val carbs100g: Double = 0.0,
    val fat100g: Double = 0.0,
    val saturatedFat100g: Double? = null,
    val fiber100g: Double? = null,
    val sodium100g: Double? = null,
    val sugars100g: Double? = null,
    val confidence: Double = 1.0
)

sealed interface NutritionLabelUiState {
    data object Scanning : NutritionLabelUiState
    data class Parsing(val message: String = "Procesando tabla nutricional...") : NutritionLabelUiState
    data class EditAndConfirm(val draft: LabelProductDraft) : NutritionLabelUiState
    data class Success(val food: FoodDetail, val message: String = "¡Producto guardado exitosamente!") : NutritionLabelUiState
    data class Error(val message: String) : NutritionLabelUiState
}

@HiltViewModel
class NutritionLabelScanViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NutritionLabelUiState>(NutritionLabelUiState.Scanning)
    val uiState: StateFlow<NutritionLabelUiState> = _uiState.asStateFlow()

    private var initialBarcodeParam: String? = null

    fun setInitialBarcode(barcode: String?) {
        initialBarcodeParam = barcode
    }

    fun onRawTextCaptured(rawText: String, barcode: String? = null) {
        if (_uiState.value is NutritionLabelUiState.Parsing) return
        _uiState.value = NutritionLabelUiState.Parsing()

        val activeBarcode = barcode ?: initialBarcodeParam ?: ""

        viewModelScope.launch {
            when (val result = foodRepository.parseNutritionLabel(rawText)) {
                is Result.Success -> {
                    val dto = result.data
                    val draft = LabelProductDraft(
                        canonicalName = "",
                        brandName = "",
                        barcode = activeBarcode,
                        servingName = dto.serving.name,
                        servingGrams = dto.serving.weightGrams,
                        calories100g = dto.per100g.calories ?: 0,
                        protein100g = dto.per100g.proteinG ?: 0.0,
                        carbs100g = dto.per100g.carbsG ?: 0.0,
                        fat100g = dto.per100g.fatG ?: 0.0,
                        saturatedFat100g = dto.per100g.saturatedFatG,
                        fiber100g = dto.per100g.fiberG,
                        sodium100g = dto.per100g.sodiumMg,
                        sugars100g = dto.per100g.sugarsG,
                        confidence = dto.confidence
                    )
                    _uiState.value = NutritionLabelUiState.EditAndConfirm(draft)
                }
                is Result.Error -> {
                    _uiState.value = NutritionLabelUiState.Error(result.message ?: "No se pudo interpretar la etiqueta nutricional.")
                }
                Result.Loading -> {}
            }
        }
    }

    fun updateDraft(draft: LabelProductDraft) {
        _uiState.value = NutritionLabelUiState.EditAndConfirm(draft)
    }

    fun saveProduct(
        logToDiary: Boolean = true,
        diaryDate: String = LocalDate.now().toString(),
        diaryMealType: String = "snack"
    ) {
        val currentState = _uiState.value as? NutritionLabelUiState.EditAndConfirm ?: return
        val draft = currentState.draft

        if (draft.canonicalName.isBlank()) {
            _uiState.value = NutritionLabelUiState.Error("Por favor ingresa el nombre del producto.")
            return
        }

        _uiState.value = NutritionLabelUiState.Parsing("Guardando producto en la base de datos...")

        viewModelScope.launch {
            val request = CreateFromLabelRequest(
                canonicalName = draft.canonicalName,
                brandName = draft.brandName.ifBlank { null },
                barcode = draft.barcode.ifBlank { null },
                servingName = draft.servingName,
                servingGrams = draft.servingGrams,
                calories100g = draft.calories100g,
                protein100g = draft.protein100g,
                carbs100g = draft.carbs100g,
                fat100g = draft.fat100g,
                saturatedFat100g = draft.saturatedFat100g,
                fiber100g = draft.fiber100g,
                sodium100g = draft.sodium100g,
                sugars100g = draft.sugars100g,
                logToDiary = logToDiary,
                diaryDate = diaryDate,
                diaryMealType = diaryMealType
            )

            when (val result = foodRepository.createFoodFromLabel(request)) {
                is Result.Success -> {
                    _uiState.value = NutritionLabelUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = NutritionLabelUiState.Error(result.message ?: "Error al guardar el producto.")
                }
                Result.Loading -> {}
            }
        }
    }

    fun resetToScanning() {
        _uiState.value = NutritionLabelUiState.Scanning
    }
}
