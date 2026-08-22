package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.api.RecipeApiService
import com.bsnutrition.app.core.network.dto.CreateRecipeRequest
import com.bsnutrition.app.core.network.dto.LogRecipeToDiaryRequest
import com.bsnutrition.app.core.network.dto.RecipeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeApiService: RecipeApiService
) : RecipeRepository {

    override suspend fun getRecipes(query: String?, page: Int): Result<List<RecipeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = recipeApiService.getRecipes(query = query, page = page)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data.data)
            } else {
                Result.Error(Exception("Error al cargar recetas (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de conexión al cargar recetas.")
        }
    }

    override suspend fun getRecipe(id: Long): Result<RecipeDto> = withContext(Dispatchers.IO) {
        try {
            val response = recipeApiService.getRecipe(id)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(Exception("Error al cargar la receta (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de conexión al obtener receta.")
        }
    }

    override suspend fun createRecipe(request: CreateRecipeRequest): Result<RecipeDto> = withContext(Dispatchers.IO) {
        try {
            val response = recipeApiService.createRecipe(request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(Exception("Error al crear la receta (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de conexión al guardar receta.")
        }
    }

    override suspend fun updateRecipe(id: Long, request: CreateRecipeRequest): Result<RecipeDto> = withContext(Dispatchers.IO) {
        try {
            val response = recipeApiService.updateRecipe(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(Exception("Error al actualizar la receta (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de conexión al actualizar receta.")
        }
    }

    override suspend fun deleteRecipe(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = recipeApiService.deleteRecipe(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Error al eliminar la receta (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de conexión al eliminar receta.")
        }
    }

    override suspend fun logRecipeToDiary(
        recipeId: Long,
        date: String,
        mealType: String,
        servings: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val clientId = UUID.randomUUID().toString()
            val response = recipeApiService.logToDiary(
                recipeId,
                LogRecipeToDiaryRequest(
                    date = date,
                    mealType = mealType,
                    servings = servings,
                    clientId = clientId
                )
            )
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Error al agregar receta al diario (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error al registrar receta en el diario.")
        }
    }
}
