package com.bsnutrition.app.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.data.repository.GoalRepository
import com.bsnutrition.app.core.model.MealLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        loadCurrentGoal()
        loadDiaryForDate(_uiState.value.selectedDate)
    }

    fun onPreviousDay() {
        val prevDate = _uiState.value.selectedDate.minusDays(1)
        onDateSelected(prevDate)
    }

    fun onNextDay() {
        val nextDate = _uiState.value.selectedDate.plusDays(1)
        onDateSelected(nextDate)
    }

    fun onToday() {
        onDateSelected(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadDiaryForDate(date)
    }

    fun loadDiaryForDate(date: LocalDate) {
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = diaryRepository.getDiaryDay(dateStr)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            diary = result.data,
                            error = null
                        )
                    }
                    loadWaterLogs(dateStr)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Error al cargar el diario"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadCurrentGoal() {
        viewModelScope.launch {
            when (val result = goalRepository.getCurrentGoal()) {
                is Result.Success -> {
                    _uiState.update { it.copy(goal = result.data) }
                }
                is Result.Error -> Unit
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadWaterLogs(dateStr: String) {
        viewModelScope.launch {
            when (val result = diaryRepository.getWaterLogs(dateStr)) {
                is Result.Success -> {
                    _uiState.update { it.copy(waterLogs = result.data) }
                }
                is Result.Error -> Unit
                is Result.Loading -> Unit
            }
        }
    }

    fun logWater(amountMl: Int) {
        val dateStr = _uiState.value.formattedDate
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingWater = true) }
            when (val result = diaryRepository.logWater(dateStr, amountMl)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoggingWater = false,
                            waterLogs = it.waterLogs + result.data,
                            userMessage = "+${amountMl}ml de agua registrados"
                        )
                    }
                    // Refresh diary summary to update total water
                    refreshDiarySummary(dateStr)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoggingWater = false,
                            error = result.message ?: "No se pudo registrar agua"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun deleteWaterLog(id: Long) {
        val dateStr = _uiState.value.formattedDate
        viewModelScope.launch {
            when (val result = diaryRepository.deleteWaterLog(id)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            waterLogs = state.waterLogs.filter { it.id != id }
                        )
                    }
                    refreshDiarySummary(dateStr)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message ?: "Error al eliminar agua") }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun deleteMealEntry(entryId: Long) {
        val dateStr = _uiState.value.formattedDate
        viewModelScope.launch {
            when (val result = diaryRepository.deleteMealEntry(entryId)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        val updatedMeals = state.diary?.meals?.map { meal ->
                            meal.copy(entries = meal.entries.filter { it.id != entryId })
                        } ?: emptyList()

                        state.copy(
                            diary = state.diary?.copy(meals = updatedMeals),
                            userMessage = "Alimento eliminado de la comida"
                        )
                    }
                    // Refresh whole day to recalculate calories & macros accurately
                    loadDiaryForDate(_uiState.value.selectedDate)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message ?: "No se pudo eliminar el alimento") }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun copyMeal(sourceMeal: MealLog, targetDate: LocalDate, targetMealType: String) {
        val targetDateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showCopyMealDialog = false) }
            when (val result = diaryRepository.copyMeal(sourceMeal.id, targetDateStr, targetMealType)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "${sourceMeal.name} copiado a $targetDateStr"
                        )
                    }
                    if (targetDate == _uiState.value.selectedDate) {
                        loadDiaryForDate(targetDate)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Error al copiar comida"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun copyDay(targetDate: LocalDate) {
        val sourceDateStr = _uiState.value.formattedDate
        val targetDateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showCopyDayDialog = false) }
            when (val result = diaryRepository.copyDay(sourceDateStr, targetDateStr)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Día copiado a $targetDateStr con éxito"
                        )
                    }
                    if (targetDate == _uiState.value.selectedDate) {
                        loadDiaryForDate(targetDate)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Error al copiar día"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun openCopyMealDialog(meal: MealLog) {
        _uiState.update { it.copy(showCopyMealDialog = true, mealToCopy = meal) }
    }

    fun dismissCopyMealDialog() {
        _uiState.update { it.copy(showCopyMealDialog = false, mealToCopy = null) }
    }

    fun openCopyDayDialog() {
        _uiState.update { it.copy(showCopyDayDialog = true) }
    }

    fun dismissCopyDayDialog() {
        _uiState.update { it.copy(showCopyDayDialog = false) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun refreshDiarySummary(dateStr: String) {
        viewModelScope.launch {
            when (val result = diaryRepository.getDailySummary(dateStr)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(diary = state.diary?.copy(summary = result.data))
                    }
                }
                is Result.Error -> Unit
                is Result.Loading -> Unit
            }
        }
    }
}
