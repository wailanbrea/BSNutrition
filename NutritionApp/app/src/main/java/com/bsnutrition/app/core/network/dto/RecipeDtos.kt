package com.bsnutrition.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeListResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: RecipePaginationDto
)

@Serializable
data class RecipePaginationDto(
    @SerialName("data") val data: List<RecipeDto> = emptyList(),
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("last_page") val lastPage: Int = 1,
    @SerialName("total") val total: Int = 0
)

@Serializable
data class SingleRecipeResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: RecipeDto
)

@Serializable
data class RecipeDto(
    @SerialName("id") val id: Long,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("servings") val servings: Int = 1,
    @SerialName("prep_time_minutes") val prepTimeMinutes: Int? = null,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
    @SerialName("total_weight_grams") val totalWeightGrams: Double = 0.0,
    @SerialName("calories_per_serving") val caloriesPerServing: Int = 0,
    @SerialName("protein_per_serving_g") val proteinPerServingG: Double = 0.0,
    @SerialName("carbs_per_serving_g") val carbsPerServingG: Double = 0.0,
    @SerialName("fat_per_serving_g") val fatPerServingG: Double = 0.0,
    @SerialName("fiber_per_serving_g") val fiberPerServingG: Double? = null,
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("ingredients") val ingredients: List<RecipeIngredientDto> = emptyList(),
    @SerialName("steps") val steps: List<RecipeStepDto> = emptyList()
)

@Serializable
data class RecipeIngredientDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("food_id") val foodId: Long? = null,
    @SerialName("portion_id") val portionId: Long? = null,
    @SerialName("custom_name") val customName: String,
    @SerialName("quantity") val quantity: Double = 1.0,
    @SerialName("unit") val unit: String = "g",
    @SerialName("grams") val grams: Double = 100.0,
    @SerialName("calories") val calories: Int = 0,
    @SerialName("protein_g") val proteinG: Double = 0.0,
    @SerialName("carbs_g") val carbsG: Double = 0.0,
    @SerialName("fat_g") val fatG: Double = 0.0
)

@Serializable
data class RecipeStepDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("step_number") val stepNumber: Int = 1,
    @SerialName("instruction") val instruction: String
)

@Serializable
data class CreateRecipeRequest(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("servings") val servings: Int = 1,
    @SerialName("prep_time_minutes") val prepTimeMinutes: Int? = null,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("ingredients") val ingredients: List<RecipeIngredientDto>,
    @SerialName("steps") val steps: List<String> = emptyList()
)

@Serializable
data class LogRecipeToDiaryRequest(
    @SerialName("date") val date: String,
    @SerialName("meal_type") val mealType: String,
    @SerialName("servings") val servings: Double = 1.0,
    @SerialName("client_id") val clientId: String? = null
)
