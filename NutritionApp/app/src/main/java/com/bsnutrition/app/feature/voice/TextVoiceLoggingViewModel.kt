package com.bsnutrition.app.feature.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.AiPhotoRepository
import com.bsnutrition.app.core.data.repository.ConfirmedPhotoItem
import com.bsnutrition.app.core.model.FoodCandidate
import com.bsnutrition.app.feature.photo.AiFoodPhotoUiState
import com.bsnutrition.app.feature.photo.EditableFoodItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

sealed interface TextVoiceLoggingUiState {
    data class Input(
        val text: String = "",
        val isListening: Boolean = false
    ) : TextVoiceLoggingUiState

    data class Parsing(val message: String = "Interpretando alimentos y cantidades con IA...") : TextVoiceLoggingUiState

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
    ) : TextVoiceLoggingUiState

    data class LoggedSuccess(val count: Int) : TextVoiceLoggingUiState
    data class Error(val message: String) : TextVoiceLoggingUiState
}

@HiltViewModel
class TextVoiceLoggingViewModel @Inject constructor(
    private val aiRepository: AiPhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TextVoiceLoggingUiState>(TextVoiceLoggingUiState.Input())
    val uiState: StateFlow<TextVoiceLoggingUiState> = _uiState.asStateFlow()

    fun onTextChanged(text: String) {
        val current = _uiState.value
        if (current is TextVoiceLoggingUiState.Input) {
            _uiState.value = current.copy(text = text)
        } else {
            _uiState.value = TextVoiceLoggingUiState.Input(text = text)
        }
    }

    fun submitText(overrideText: String? = null, mealType: String = "lunch", locale: String = "DO") {
        val textToSubmit = overrideText ?: (_uiState.value as? TextVoiceLoggingUiState.Input)?.text ?: ""
        if (textToSubmit.isBlank()) return

        _uiState.value = TextVoiceLoggingUiState.Parsing()

        viewModelScope.launch {
            when (val result = aiRepository.parseMealText(textToSubmit, locale, mealType)) {
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
                    _uiState.value = calculateReviewState(
                        analysisId = analysis.id,
                        dishName = analysis.dishName,
                        summary = analysis.summary,
                        confidenceScore = analysis.confidenceScore,
                        items = editableItems
                    )
                }
                is Result.Error -> {
                    _uiState.value = TextVoiceLoggingUiState.Error(result.message ?: "No se pudo interpretar el texto.")
                }
                Result.Loading -> {}
            }
        }
    }

    fun updateItemWeight(itemIndex: Int, newWeightGrams: Double) {
        val currentState = _uiState.value as? TextVoiceLoggingUiState.Review ?: return
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
        val currentState = _uiState.value as? TextVoiceLoggingUiState.Review ?: return
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
        val currentState = _uiState.value as? TextVoiceLoggingUiState.Review ?: return
        if (itemIndex !in currentState.items.indices) return

        val updatedList = currentState.items.toMutableList().apply {
            removeAt(itemIndex)
        }

        if (updatedList.isEmpty()) {
            _uiState.value = TextVoiceLoggingUiState.Input()
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
        val currentState = _uiState.value as? TextVoiceLoggingUiState.Review ?: return
        _uiState.value = TextVoiceLoggingUiState.Parsing("Guardando alimentos en tu diario...")

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

            when (val result = aiRepository.confirmAnalysis(currentState.analysisId, date, mealType, confirmedItems)) {
                is Result.Success -> {
                    _uiState.value = TextVoiceLoggingUiState.LoggedSuccess(result.data)
                }
                is Result.Error -> {
                    _uiState.value = TextVoiceLoggingUiState.Error(result.message ?: "No se pudo registrar en el diario.")
                }
                Result.Loading -> {}
            }
        }
    }

    fun resetToInput() {
        _uiState.value = TextVoiceLoggingUiState.Input()
    }

    private fun calculateReviewState(
        analysisId: Long,
        dishName: String,
        summary: String,
        confidenceScore: Double,
        items: List<EditableFoodItem>
    ): TextVoiceLoggingUiState.Review {
        val totalCals = items.sumOf { it.calories }
        val totalProt = items.sumOf { it.proteinG }
        val totalCarbs = items.sumOf { it.carbsG }
        val totalFat = items.sumOf { it.fatG }

        return TextVoiceLoggingUiState.Review(
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
