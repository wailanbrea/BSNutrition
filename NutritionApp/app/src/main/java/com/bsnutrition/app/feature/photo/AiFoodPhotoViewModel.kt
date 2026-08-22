package com.bsnutrition.app.feature.photo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.AiPhotoRepository
import com.bsnutrition.app.core.data.repository.ConfirmedPhotoItem
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.model.AiPhotoAnalysis
import com.bsnutrition.app.core.model.FoodCandidate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

data class EditableFoodItem(
    val id: Long,
    val originalName: String,
    val selectedFoodId: Long?,
    val displayName: String,
    val weightGrams: Double,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val confidence: Double,
    val candidates: List<FoodCandidate>
)

sealed interface AiFoodPhotoUiState {
    data object Capture : AiFoodPhotoUiState
    data class Analyzing(val message: String = "Analizando plato con Inteligencia Artificial...") : AiFoodPhotoUiState
    data class Review(
        val analysisId: Long,
        val dishName: String,
        val summary: String,
        val confidenceScore: Double,
        val items: List<EditableFoodItem>,
        val totalCalories: Int,
        val totalProteinG: Double,
        val totalCarbsG: Double,
        val totalFatG: Double
    ) : AiFoodPhotoUiState
    data class LoggedSuccess(val count: Int) : AiFoodPhotoUiState
    data class Error(val message: String) : AiFoodPhotoUiState
}

@HiltViewModel
class AiFoodPhotoViewModel @Inject constructor(
    private val aiPhotoRepository: AiPhotoRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiFoodPhotoUiState>(AiFoodPhotoUiState.Capture)
    val uiState: StateFlow<AiFoodPhotoUiState> = _uiState.asStateFlow()

    fun analyzePhoto(imageFile: File, mealType: String = "lunch", locale: String = "DO") {
        _uiState.value = AiFoodPhotoUiState.Analyzing("Reconociendo ingredientes y calculando porciones...")
        viewModelScope.launch {
            when (val result = aiPhotoRepository.analyzeFoodPhoto(imageFile, locale, mealType)) {
                is Result.Success -> {
                    val analysis = result.data
                    val editableItems = analysis.items.map { item ->
                        EditableFoodItem(
                            id = item.id,
                            originalName = item.name,
                            selectedFoodId = item.foodId,
                            displayName = item.matchedName ?: item.name,
                            weightGrams = item.weightGrams,
                            calories = item.calories,
                            proteinG = item.proteinG,
                            carbsG = item.carbsG,
                            fatG = item.fatG,
                            confidence = item.confidence,
                            candidates = item.candidates
                        )
                    }
                    _uiState.value = calculateReviewState(analysis.id, analysis.dishName, analysis.summary, analysis.confidenceScore, editableItems)
                }
                is Result.Error -> {
                    _uiState.value = AiFoodPhotoUiState.Error(result.message ?: "No se pudo analizar la foto.")
                }
                Result.Loading -> {}
            }
        }
    }

    fun updateItemWeight(itemIndex: Int, newWeightGrams: Double) {
        val currentState = _uiState.value as? AiFoodPhotoUiState.Review ?: return
        if (itemIndex !in currentState.items.indices || newWeightGrams <= 0) return

        val currentItem = currentState.items[itemIndex]
        val ratio = newWeightGrams / currentItem.weightGrams
        val updatedItem = currentItem.copy(
            weightGrams = newWeightGrams,
            calories = (currentItem.calories * ratio).roundToInt(),
            proteinG = (currentItem.proteinG * ratio * 10.0).roundToInt() / 10.0,
            carbsG = (currentItem.carbsG * ratio * 10.0).roundToInt() / 10.0,
            fatG = (currentItem.fatG * ratio * 10.0).roundToInt() / 10.0
        )

        val updatedList = currentState.items.toMutableList().apply {
            set(itemIndex, updatedItem)
        }

        _uiState.value = calculateReviewState(
            currentState.analysisId,
            currentState.dishName,
            currentState.summary,
            currentState.confidenceScore,
            updatedList
        )
    }

    fun selectCandidate(itemIndex: Int, candidate: FoodCandidate) {
        val currentState = _uiState.value as? AiFoodPhotoUiState.Review ?: return
        if (itemIndex !in currentState.items.indices) return

        val currentItem = currentState.items[itemIndex]
        val weightG = currentItem.weightGrams
        val newCals = ((candidate.calories100g * weightG) / 100.0).roundToInt()
        val newProt = (((candidate.protein100g * weightG) / 100.0) * 10.0).roundToInt() / 10.0
        val newCarbs = (((candidate.carbs100g * weightG) / 100.0) * 10.0).roundToInt() / 10.0
        val newFat = (((candidate.fat100g * weightG) / 100.0) * 10.0).roundToInt() / 10.0

        val updatedItem = currentItem.copy(
            selectedFoodId = candidate.foodId,
            displayName = candidate.canonicalName,
            calories = newCals,
            proteinG = newProt,
            carbsG = newCarbs,
            fatG = newFat
        )

        val updatedList = currentState.items.toMutableList().apply {
            set(itemIndex, updatedItem)
        }

        _uiState.value = calculateReviewState(
            currentState.analysisId,
            currentState.dishName,
            currentState.summary,
            currentState.confidenceScore,
            updatedList
        )
    }

    fun removeItem(itemIndex: Int) {
        val currentState = _uiState.value as? AiFoodPhotoUiState.Review ?: return
        if (itemIndex !in currentState.items.indices) return

        val updatedList = currentState.items.toMutableList().apply {
            removeAt(itemIndex)
        }

        if (updatedList.isEmpty()) {
            _uiState.value = AiFoodPhotoUiState.Capture
            return
        }

        _uiState.value = calculateReviewState(
            currentState.analysisId,
            currentState.dishName,
            currentState.summary,
            currentState.confidenceScore,
            updatedList
        )
    }

    fun confirmAndLog(date: String = LocalDate.now().toString(), mealType: String = "lunch") {
        val currentState = _uiState.value as? AiFoodPhotoUiState.Review ?: return
        _uiState.value = AiFoodPhotoUiState.Analyzing("Guardando alimentos en tu diario...")

        viewModelScope.launch {
            val confirmedItems = currentState.items.map { item ->
                ConfirmedPhotoItem(
                    name = item.displayName,
                    foodId = item.selectedFoodId,
                    portionId = null,
                    quantity = 1.0,
                    weightGrams = item.weightGrams,
                    calories = item.calories,
                    proteinG = item.proteinG,
                    carbsG = item.carbsG,
                    fatG = item.fatG
                )
            }

            when (val result = aiPhotoRepository.confirmAnalysis(currentState.analysisId, date, mealType, confirmedItems)) {
                is Result.Success -> {
                    _uiState.value = AiFoodPhotoUiState.LoggedSuccess(result.data)
                }
                is Result.Error -> {
                    _uiState.value = AiFoodPhotoUiState.Error(result.message ?: "No se pudo registrar en el diario.")
                }
                Result.Loading -> {}
            }
        }
    }

    fun resetToCapture() {
        _uiState.value = AiFoodPhotoUiState.Capture
    }

    private fun calculateReviewState(
        analysisId: Long,
        dishName: String,
        summary: String,
        confidenceScore: Double,
        items: List<EditableFoodItem>
    ): AiFoodPhotoUiState.Review {
        val totalCals = items.sumOf { it.calories }
        val totalProt = items.sumOf { it.proteinG }
        val totalCarbs = items.sumOf { it.carbsG }
        val totalFat = items.sumOf { it.fatG }

        return AiFoodPhotoUiState.Review(
            analysisId = analysisId,
            dishName = dishName,
            summary = summary,
            confidenceScore = confidenceScore,
            items = items,
            totalCalories = totalCals,
            totalProteinG = (totalProt * 10.0).roundToInt() / 10.0,
            totalCarbsG = (totalCarbs * 10.0).roundToInt() / 10.0,
            totalFatG = (totalFat * 10.0).roundToInt() / 10.0
        )
    }
}
