package com.bsnutrition.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.data.repository.GoalRepository
import com.bsnutrition.app.core.model.DailyDiary
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.NutritionGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val goal: NutritionGoal? = null,
    val diary: DailyDiary? = null,
    val isLoading: Boolean = false,
    val isLoggingWater: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null
) {
    val totalCaloriesConsumed: Int
        get() = diary?.summary?.calories ?: 0

    val targetCalories: Int
        get() = goal?.targetCalories ?: 2000

    val remainingCalories: Int
        get() = targetCalories - totalCaloriesConsumed

    val calorieProgress: Float
        get() = if (targetCalories > 0) (totalCaloriesConsumed.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1.5f) else 0f

    val totalProteinG: Double
        get() = diary?.summary?.proteinG ?: 0.0

    val targetProteinG: Double
        get() = goal?.targetProteinG ?: 150.0

    val totalCarbsG: Double
        get() = diary?.summary?.carbsG ?: 0.0

    val targetCarbsG: Double
        get() = goal?.targetCarbsG ?: 200.0

    val totalFatG: Double
        get() = diary?.summary?.fatG ?: 0.0

    val targetFatG: Double
        get() = goal?.targetFatG ?: 65.0

    val totalWaterMl: Int
        get() = diary?.summary?.waterMl ?: 0

    val targetWaterMl: Int
        get() = goal?.targetWaterMl ?: 2500

    val meals: List<MealLog>
        get() = diary?.meals ?: emptyList()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadCurrentGoal()
        loadTodayDiary()
    }

    fun loadCurrentGoal() {
        viewModelScope.launch {
            when (val result = goalRepository.getCurrentGoal()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            goal = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadTodayDiary() {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            when (val result = diaryRepository.getDiaryDay(todayStr)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            diary = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun logWater(amountMl: Int) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingWater = true) }
            when (val result = diaryRepository.logWater(todayStr, amountMl)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoggingWater = false,
                            userMessage = "+${amountMl}ml de agua registrados"
                        )
                    }
                    loadTodayDiary()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoggingWater = false,
                            errorMessage = result.message ?: "No se pudo registrar agua"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
