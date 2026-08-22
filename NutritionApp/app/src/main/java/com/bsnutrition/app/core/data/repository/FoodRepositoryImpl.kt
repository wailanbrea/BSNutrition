package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.BsnDispatchers
import com.bsnutrition.app.core.common.Dispatcher
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.FavoriteFoodDao
import com.bsnutrition.app.core.database.FavoriteFoodEntity
import com.bsnutrition.app.core.database.RecentFoodDao
import com.bsnutrition.app.core.database.RecentFoodEntity
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.NutritionCalculation
import com.bsnutrition.app.core.network.api.FoodApiService
import com.bsnutrition.app.core.network.model.CalculateFoodNutritionRequestDto
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodApiService: FoodApiService,
    private val favoriteFoodDao: FavoriteFoodDao,
    private val recentFoodDao: RecentFoodDao,
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

    override suspend fun getFavorites(): Result<List<FoodSummary>> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.getFavoriteFoods()
        }

        return when (apiResult) {
            is Result.Success -> {
                val foods = apiResult.data.data.map { it.toDomain() }
                withContext(ioDispatcher) {
                    favoriteFoodDao.clearFavorites()
                    favoriteFoodDao.insertAll(foods.map { FavoriteFoodEntity.fromDomain(it) })
                }
                Result.Success(foods)
            }
            is Result.Error -> {
                // Fallback to offline Room cache
                val cached = withContext(ioDispatcher) {
                    favoriteFoodDao.getFavoriteFoods().map { it.toDomain() }
                }
                if (cached.isNotEmpty()) {
                    Result.Success(cached)
                } else {
                    Result.Error(apiResult.exception, apiResult.message, apiResult.code)
                }
            }
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun toggleFavorite(food: FoodSummary): Result<Boolean> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.toggleFavoriteFood(food.id)
        }

        return when (apiResult) {
            is Result.Success -> {
                val isFav = apiResult.data.isFavorite
                withContext(ioDispatcher) {
                    if (isFav) {
                        favoriteFoodDao.insertFavorite(FavoriteFoodEntity.fromDomain(food))
                    } else {
                        favoriteFoodDao.deleteFavorite(food.id)
                    }
                }
                Result.Success(isFav)
            }
            is Result.Error -> Result.Error(apiResult.exception, apiResult.message, apiResult.code)
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun isFavorite(foodId: Long): Boolean {
        return withContext(ioDispatcher) {
            favoriteFoodDao.isFavorite(foodId)
        }
    }

    override suspend fun getRecentFoods(): Result<List<FoodSummary>> {
        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.getRecentFoods()
        }

        return when (apiResult) {
            is Result.Success -> {
                val foods = apiResult.data.data.map { it.toDomain() }
                withContext(ioDispatcher) {
                    recentFoodDao.clearRecents()
                    recentFoodDao.insertAll(foods.map { RecentFoodEntity.fromDomain(it) })
                }
                Result.Success(foods)
            }
            is Result.Error -> {
                val cached = withContext(ioDispatcher) {
                    recentFoodDao.getRecentFoods().map { it.toDomain() }
                }
                if (cached.isNotEmpty()) {
                    Result.Success(cached)
                } else {
                    Result.Error(apiResult.exception, apiResult.message, apiResult.code)
                }
            }
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun recordRecentFood(food: FoodSummary): Result<Unit> {
        // Optimistically update local Room cache
        withContext(ioDispatcher) {
            recentFoodDao.insertRecent(RecentFoodEntity.fromDomain(food))
        }

        val apiResult = safeApiCall(ioDispatcher, json) {
            foodApiService.recordRecentFood(food.id)
        }

        return when (apiResult) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Success(Unit) // Local record succeeded
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun parseNutritionLabel(rawText: String): Result<com.bsnutrition.app.core.network.dto.ParsedLabelDataDto> = withContext(Dispatchers.IO) {
        try {
            val response = foodApiService.parseNutritionLabel(com.bsnutrition.app.core.network.dto.ParseLabelRequest(rawText))
            Result.Success(response.data)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error al procesar la etiqueta nutricional.")
        }
    }

    override suspend fun createFoodFromLabel(request: com.bsnutrition.app.core.network.dto.CreateFromLabelRequest): Result<FoodDetail> = withContext(Dispatchers.IO) {
        try {
            val response = foodApiService.createFoodFromLabel(request)
            val foodDto = response.data.food
            val foodDetail = FoodDetail(
                id = foodDto.id,
                canonicalName = foodDto.canonicalName,
                brandName = foodDto.brand?.name,
                categoryName = foodDto.category?.name ?: "General",
                countryCode = foodDto.countryCode ?: "DO",
                defaultBasisAmount = foodDto.defaultBasisAmount,
                defaultBasisUnit = foodDto.defaultBasisUnit,
                nutrients = foodDto.nutrients.map { n ->
                    com.bsnutrition.app.core.model.NutrientAmount(
                        name = n.name,
                        code = n.code,
                        amount = n.amount,
                        unit = n.unit
                    )
                },
                portions = foodDto.portions.map { p ->
                    com.bsnutrition.app.core.model.Portion(
                        id = p.id,
                        portionName = p.portionName,
                        gramWeight = p.gramWeight,
                        isDefault = p.isDefault
                    )
                }
            )
            Result.Success(foodDetail)
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error al crear el producto.")
        }
    }
}

