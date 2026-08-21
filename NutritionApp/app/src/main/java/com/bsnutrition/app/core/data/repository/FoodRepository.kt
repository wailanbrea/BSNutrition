package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.NutritionCalculation

interface FoodRepository {

    suspend fun searchFoods(
        query: String? = null,
        categoryId: Long? = null,
        country: String? = "DO",
        page: Int? = 1,
        perPage: Int? = 20
    ): Result<List<FoodSummary>>

    suspend fun getFoodDetail(id: Long): Result<FoodDetail>

    suspend fun getFoodByBarcode(barcode: String): Result<FoodDetail>

    suspend fun calculateNutrition(
        foodId: Long,
        quantity: Double,
        portionId: Long? = null,
        unit: String? = "g"
    ): Result<NutritionCalculation>

    suspend fun getFavorites(): Result<List<FoodSummary>>

    suspend fun toggleFavorite(food: FoodSummary): Result<Boolean>

    suspend fun isFavorite(foodId: Long): Boolean

    suspend fun getRecentFoods(): Result<List<FoodSummary>>

    suspend fun recordRecentFood(food: FoodSummary): Result<Unit>
}


