package com.bsnutrition.app.core.network.api

import com.bsnutrition.app.core.network.model.CalculateFoodNutritionRequestDto
import com.bsnutrition.app.core.network.model.CalculateFoodNutritionResponseDto
import com.bsnutrition.app.core.network.model.FoodDetailResponseDto
import com.bsnutrition.app.core.network.model.FoodSearchResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApiService {

    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String? = null,
        @Query("category_id") categoryId: Long? = null,
        @Query("country") country: String? = "DO",
        @Query("page") page: Int? = 1,
        @Query("per_page") perPage: Int? = 20
    ): FoodSearchResponseDto

    @GET("foods/{id}")
    suspend fun getFoodDetail(
        @Path("id") id: Long
    ): FoodDetailResponseDto

    @GET("foods/barcode/{barcode}")
    suspend fun getFoodByBarcode(
        @Path("barcode") barcode: String
    ): FoodDetailResponseDto

    @POST("foods/{id}/calculate")
    suspend fun calculateFoodNutrition(
        @Path("id") id: Long,
        @Body request: CalculateFoodNutritionRequestDto
    ): CalculateFoodNutritionResponseDto

    @GET("foods/favorites")
    suspend fun getFavoriteFoods(
        @Query("page") page: Int? = 1,
        @Query("per_page") perPage: Int? = 50
    ): FoodSearchResponseDto

    @POST("foods/{id}/favorite")
    suspend fun toggleFavoriteFood(
        @Path("id") id: Long
    ): com.bsnutrition.app.core.network.model.ToggleFavoriteResponseDto

    @GET("foods/{id}/favorite")
    suspend fun isFavoriteFood(
        @Path("id") id: Long
    ): com.bsnutrition.app.core.network.model.ToggleFavoriteResponseDto

    @GET("foods/recents")
    suspend fun getRecentFoods(
        @Query("page") page: Int? = 1,
        @Query("per_page") perPage: Int? = 50
    ): FoodSearchResponseDto

    @POST("foods/{id}/recent")
    suspend fun recordRecentFood(
        @Path("id") id: Long
    ): com.bsnutrition.app.core.network.model.MessageResponseDto

    @POST("foods/ocr/parse-label")
    suspend fun parseNutritionLabel(
        @Body request: com.bsnutrition.app.core.network.dto.ParseLabelRequest
    ): com.bsnutrition.app.core.network.dto.ParseLabelApiResponse

    @POST("foods/from-label")
    suspend fun createFoodFromLabel(
        @Body request: com.bsnutrition.app.core.network.dto.CreateFromLabelRequest
    ): com.bsnutrition.app.core.network.dto.CreateFromLabelApiResponse
}



