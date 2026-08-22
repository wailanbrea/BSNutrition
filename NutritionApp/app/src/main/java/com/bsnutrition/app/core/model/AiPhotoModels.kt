package com.bsnutrition.app.core.model

data class AiPhotoAnalysis(
    val id: Long,
    val status: String,
    val dishName: String,
    val summary: String,
    val confidenceScore: Double,
    val provider: String,
    val totalCalories: Int,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double,
    val items: List<AiPhotoItem>
)

data class AiPhotoItem(
    val id: Long,
    val foodId: Long?,
    val name: String,
    val matchedName: String?,
    val weightGrams: Double,
    val portionDescription: String,
    val preparationMethod: String?,
    val confidence: Double,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val candidates: List<FoodCandidate>
)

data class FoodCandidate(
    val foodId: Long,
    val canonicalName: String,
    val brandName: String?,
    val score: Double,
    val matchType: String,
    val calories100g: Double,
    val protein100g: Double,
    val carbs100g: Double,
    val fat100g: Double
)
