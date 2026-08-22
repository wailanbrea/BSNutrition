package com.bsnutrition.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParseLabelRequest(
    @SerialName("raw_text") val rawText: String
)

@Serializable
data class ParseLabelApiResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: ParsedLabelDataDto
)

@Serializable
data class ParsedLabelDataDto(
    @SerialName("serving") val serving: ParsedServingDto,
    @SerialName("per_serving") val perServing: ParsedNutrientsDto,
    @SerialName("per_100g") val per100g: ParsedNutrientsDto,
    @SerialName("confidence") val confidence: Double = 0.0
)

@Serializable
data class ParsedServingDto(
    @SerialName("name") val name: String = "1 porción",
    @SerialName("weight_grams") val weightGrams: Double = 100.0,
    @SerialName("unit") val unit: String = "g"
)

@Serializable
data class ParsedNutrientsDto(
    @SerialName("calories") val calories: Int? = null,
    @SerialName("fat_g") val fatG: Double? = null,
    @SerialName("saturated_fat_g") val saturatedFatG: Double? = null,
    @SerialName("trans_fat_g") val transFatG: Double? = null,
    @SerialName("sodium_mg") val sodiumMg: Double? = null,
    @SerialName("carbs_g") val carbsG: Double? = null,
    @SerialName("fiber_g") val fiberG: Double? = null,
    @SerialName("sugars_g") val sugarsG: Double? = null,
    @SerialName("protein_g") val proteinG: Double? = null
)

@Serializable
data class CreateFromLabelRequest(
    @SerialName("canonical_name") val canonicalName: String,
    @SerialName("brand_name") val brandName: String? = null,
    @SerialName("barcode") val barcode: String? = null,
    @SerialName("serving_name") val servingName: String? = "1 porción",
    @SerialName("serving_grams") val servingGrams: Double = 100.0,
    @SerialName("calories_100g") val calories100g: Int,
    @SerialName("protein_100g") val protein100g: Double,
    @SerialName("carbs_100g") val carbs100g: Double,
    @SerialName("fat_100g") val fat100g: Double,
    @SerialName("saturated_fat_100g") val saturatedFat100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null,
    @SerialName("sodium_100g") val sodium100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
    @SerialName("log_to_diary") val logToDiary: Boolean = false,
    @SerialName("diary_date") val diaryDate: String? = null,
    @SerialName("diary_meal_type") val diaryMealType: String? = null
)

@Serializable
data class CreateFromLabelApiResponse(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String,
    @SerialName("data") val data: CreateFromLabelDataDto
)

@Serializable
data class CreateFromLabelDataDto(
    @SerialName("food") val food: FoodDto
)
