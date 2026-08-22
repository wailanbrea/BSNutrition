package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.dto.CreateRecipeRequest
import com.bsnutrition.app.core.network.dto.RecipeDto

interface RecipeRepository {
    suspend fun getRecipes(query: String? = null, page: Int = 1): Result<List<RecipeDto>>
    suspend fun getRecipe(id: Long): Result<RecipeDto>
    suspend fun createRecipe(request: CreateRecipeRequest): Result<RecipeDto>
    suspend fun updateRecipe(id: Long, request: CreateRecipeRequest): Result<RecipeDto>
    suspend fun deleteRecipe(id: Long): Result<Unit>
    suspend fun logRecipeToDiary(recipeId: Long, date: String, mealType: String, servings: Double): Result<Unit>
}
