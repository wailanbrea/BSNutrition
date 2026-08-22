package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.dto.CreateRecipeRequest
import com.bsnutrition.app.core.network.dto.LogRecipeToDiaryRequest
import com.bsnutrition.app.core.network.dto.RecipeListResponse
import com.bsnutrition.app.core.network.dto.SingleRecipeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApiService {

    @GET("recipes")
    suspend fun getRecipes(
        @Query("query") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 15
    ): Response<RecipeListResponse>

    @POST("recipes")
    suspend fun createRecipe(
        @Body request: CreateRecipeRequest
    ): Response<SingleRecipeResponse>

    @GET("recipes/{id}")
    suspend fun getRecipe(
        @Path("id") id: Long
    ): Response<SingleRecipeResponse>

    @PUT("recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: Long,
        @Body request: CreateRecipeRequest
    ): Response<SingleRecipeResponse>

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(
        @Path("id") id: Long
    ): Response<Map<String, String>>

    @POST("recipes/{id}/log-to-diary")
    suspend fun logToDiary(
        @Path("id") id: Long,
        @Body request: LogRecipeToDiaryRequest
    ): Response<Map<String, Any>>
}
