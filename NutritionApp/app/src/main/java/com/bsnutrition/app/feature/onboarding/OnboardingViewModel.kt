package com.bsnutrition.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateBirthAndSex(birthDate: String, sex: String) {
        _uiState.update { it.copy(birthDate = birthDate, sex = sex, errorMessage = null) }
    }

    fun updateHeightAndWeight(heightCm: Float, weightKg: Float) {
        _uiState.update { it.copy(heightCm = heightCm, weightKg = weightKg, errorMessage = null) }
    }

    fun updateActivityLevel(activityLevel: String) {
        _uiState.update { it.copy(activityLevel = activityLevel, errorMessage = null) }
    }

    fun updateGoalAndRate(goalType: String, weeklyGoalRate: Float) {
        _uiState.update { it.copy(goalType = goalType, weeklyGoalRate = weeklyGoalRate, errorMessage = null) }
    }

    fun updateUnitSystem(unitSystem: String) {
        _uiState.update { it.copy(unitSystem = unitSystem, errorMessage = null) }
    }

    fun nextStep() {
        val current = _uiState.value.currentStep
        when (current) {
            OnboardingStep.BIRTH_SEX -> _uiState.update { it.copy(currentStep = OnboardingStep.HEIGHT_WEIGHT) }
            OnboardingStep.HEIGHT_WEIGHT -> _uiState.update { it.copy(currentStep = OnboardingStep.ACTIVITY) }
            OnboardingStep.ACTIVITY -> _uiState.update { it.copy(currentStep = OnboardingStep.GOAL_RATE) }
            OnboardingStep.GOAL_RATE -> _uiState.update { it.copy(currentStep = OnboardingStep.UNITS) }
            OnboardingStep.UNITS -> {
                _uiState.update { it.copy(currentStep = OnboardingStep.REVIEW) }
                calculateGoals()
            }
            OnboardingStep.REVIEW -> completeOnboarding()
        }
    }

    fun previousStep() {
        val current = _uiState.value.currentStep
        when (current) {
            OnboardingStep.BIRTH_SEX -> {} // No back on first step
            OnboardingStep.HEIGHT_WEIGHT -> _uiState.update { it.copy(currentStep = OnboardingStep.BIRTH_SEX) }
            OnboardingStep.ACTIVITY -> _uiState.update { it.copy(currentStep = OnboardingStep.HEIGHT_WEIGHT) }
            OnboardingStep.GOAL_RATE -> _uiState.update { it.copy(currentStep = OnboardingStep.ACTIVITY) }
            OnboardingStep.UNITS -> _uiState.update { it.copy(currentStep = OnboardingStep.GOAL_RATE) }
            OnboardingStep.REVIEW -> _uiState.update { it.copy(currentStep = OnboardingStep.UNITS) }
        }
    }

    fun calculateGoals() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = goalRepository.calculateGoal(
                birthDate = state.birthDate,
                sex = state.sex,
                height = state.heightCm,
                currentWeight = state.weightKg,
                activityLevel = state.activityLevel,
                goalType = state.goalType,
                weeklyGoalRate = state.weeklyGoalRate
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            calculatedGoal = result.data,
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
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun completeOnboarding() {
        val goal = _uiState.value.calculatedGoal
        if (goal == null) {
            calculateGoals()
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = goalRepository.saveGoal(goal)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOnboardingComplete = true
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
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
