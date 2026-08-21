package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.BsnDispatchers
import com.bsnutrition.app.core.common.Dispatcher
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.NutritionCalculation
import com.bsnutrition.app.core.network.api.FoodApiService
import com.bsnutrition.app.core.network.model.CalculateFoodNutritionRequestDto
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodApiService: FoodApiService,
    @Dispatcher(BsnDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : FoodRepository {

    override suspend fun searchFoods(
        query: String?,
        categoryId: Long?,
        country: String?,
        page: Int?,
        perPage: Int?
    ): Result<List<FoodSummary>> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.searchFoods(
                query = query,
                categoryId = categoryId,
                country = country,
                page = page,
                perPage = perPage
            )
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.map { it.toDomain() })
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun getFoodDetail(id: Long): Result<FoodDetail> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.getFoodDetail(id)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun getFoodByBarcode(barcode: String): Result<FoodDetail> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.getFoodByBarcode(barcode)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun calculateNutrition(
        foodId: Long,
        quantity: Double,
        portionId: Long?,
        unit: String?
    ): Result<NutritionCalculation> {
        val request = CalculateFoodNutritionRequestDto(
            quantity = quantity,
            portionId = portionId,
            unit = unit
        )

        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.calculateFoodNutrition(foodId, request)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(apiResult.data.data.toDomain())
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }
}
