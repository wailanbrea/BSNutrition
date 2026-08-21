package com.bsnutrition.app.feature.onboarding

import com.bsnutrition.app.core.model.NutritionGoal

enum class OnboardingStep(val index: Int, val title: String) {
    BIRTH_SEX(1, "Datos Básicos"),
    HEIGHT_WEIGHT(2, "Medidas Corporales"),
    ACTIVITY(3, "Nivel de Actividad"),
    GOAL_RATE(4, "Objetivo y Ritmo"),
    UNITS(5, "Preferencias"),
    REVIEW(6, "Tus Metas")
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.BIRTH_SEX,
    val birthDate: String = "1998-05-15",
    val sex: String = "male", // "male" | "female"
    val heightCm: Float = 175f,
    val weightKg: Float = 75f,
    val activityLevel: String = "moderate", // "sedentary", "light", "moderate", "active", "very_active"
    val goalType: String = "lose_weight", // "lose_weight", "maintain_weight", "gain_muscle"
    val weeklyGoalRate: Float = 0.5f,
    val unitSystem: String = "metric", // "metric" | "imperial"
    val calculatedGoal: NutritionGoal? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOnboardingComplete: Boolean = false
)
