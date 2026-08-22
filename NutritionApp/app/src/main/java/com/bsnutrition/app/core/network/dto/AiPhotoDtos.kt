package com.bsnutrition.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiPhotoAnalysisApiResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: AiPhotoAnalysisDataDto
)

@Serializable
data class AiPhotoAnalysisDataDto(
    @SerialName("id") val id: Long,
    @SerialName("status") val status: String,
    @SerialName("dish_name") val dishName: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("confidence_score") val confidenceScore: Double? = null,
    @SerialName("provider") val provider: String? = null,
    @SerialName("model") val model: String? = null,
    @SerialName("totals") val totals: AiPhotoTotalsDto? = null,
    @SerialName("items") val items: List<AiPhotoItemDto> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AiPhotoTotalsDto(
    @SerialName("calories") val calories: Int = 0,
    @SerialName("protein_g") val proteinG: Double = 0.0,
    @SerialName("carbs_g") val carbsG: Double = 0.0,
    @SerialName("fat_g") val fatG: Double = 0.0
)

@Serializable
data class AiPhotoItemDto(
    @SerialName("id") val id: Long,
    @SerialName("food_id") val foodId: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("matched_name") val matchedName: String? = null,
    @SerialName("estimated_weight_grams") val estimatedWeightGrams: Double = 100.0,
    @SerialName("portion_description") val portionDescription: String? = null,
    @SerialName("preparation_method") val preparationMethod: String? = null,
    @SerialName("confidence") val confidence: Double = 0.85,
    @SerialName("calories") val calories: Int = 0,
    @SerialName("protein_g") val proteinG: Double = 0.0,
    @SerialName("carbs_g") val carbsG: Double = 0.0,
    @SerialName("fat_g") val fatG: Double = 0.0,
    @SerialName("candidates") val candidates: List<FoodMatchCandidateDto> = emptyList()
)

@Serializable
data class FoodMatchCandidateDto(
    @SerialName("food_id") val foodId: Long,
    @SerialName("canonical_name") val canonicalName: String,
    @SerialName("brand_name") val brandName: String? = null,
    @SerialName("score") val score: Double = 0.0,
    @SerialName("match_type") val matchType: String = "token",
    @SerialName("matched_alias") val matchedAlias: String? = null,
    @SerialName("calories_100g") val calories100g: Double = 0.0,
    @SerialName("protein_100g") val protein100g: Double = 0.0,
    @SerialName("carbs_100g") val carbs100g: Double = 0.0,
    @SerialName("fat_100g") val fat100g: Double = 0.0
)

@Serializable
data class ConfirmPhotoAnalysisRequest(
    @SerialName("date") val date: String,
    @SerialName("meal_type") val mealType: String,
    @SerialName("items") val items: List<ConfirmPhotoItemRequest>
)

@Serializable
data class ConfirmPhotoItemRequest(
    @SerialName("name") val name: String,
    @SerialName("food_id") val foodId: Long? = null,
    @SerialName("portion_id") val portionId: Long? = null,
    @SerialName("quantity") val quantity: Double = 1.0,
    @SerialName("weight_grams") val weightGrams: Double = 100.0,
    @SerialName("calories") val calories: Int = 0,
    @SerialName("protein_g") val proteinG: Double = 0.0,
    @SerialName("carbs_g") val carbsG: Double = 0.0,
    @SerialName("fat_g") val fatG: Double = 0.0
)

@Serializable
data class ConfirmPhotoAnalysisResponse(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String,
    @SerialName("data") val data: ConfirmPhotoDataDto
)

@Serializable
data class ConfirmPhotoDataDto(
    @SerialName("analysis_id") val analysisId: Long,
    @SerialName("logged_entries_count") val loggedEntriesCount: Int
)

@Serializable
data class ParseTextRequest(
    @SerialName("text") val text: String,
    @SerialName("locale") val locale: String? = "DO",
    @SerialName("meal_type") val mealType: String? = "lunch"
)

