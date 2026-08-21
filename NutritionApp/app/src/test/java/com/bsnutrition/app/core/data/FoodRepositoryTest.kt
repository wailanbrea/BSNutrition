package com.bsnutrition.app.core.data

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.FoodRepositoryImpl
import com.bsnutrition.app.core.database.FavoriteFoodDao
import com.bsnutrition.app.core.model.FoodCategory
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.MacroBreakdown
import com.bsnutrition.app.core.network.api.FoodApiService
import com.bsnutrition.app.core.network.model.BrandDto
import com.bsnutrition.app.core.network.model.CalculateFoodNutritionResponseDto
import com.bsnutrition.app.core.network.model.FoodCategoryDto
import com.bsnutrition.app.core.network.model.FoodDetailDto
import com.bsnutrition.app.core.network.model.FoodDetailResponseDto
import com.bsnutrition.app.core.network.model.FoodNutrientDto
import com.bsnutrition.app.core.network.model.FoodPortionDto
import com.bsnutrition.app.core.network.model.FoodSearchResponseDto
import com.bsnutrition.app.core.network.model.FoodSummaryDto
import com.bsnutrition.app.core.network.model.MacroBreakdownDto
import com.bsnutrition.app.core.network.model.NutritionCalculationDto
import com.bsnutrition.app.core.network.model.ToggleFavoriteResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FoodRepositoryTest {

    private lateinit var foodApiService: FoodApiService
    private lateinit var favoriteFoodDao: FavoriteFoodDao
    private lateinit var repository: FoodRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        foodApiService = mockk()
        favoriteFoodDao = mockk(relaxed = true)
        repository = FoodRepositoryImpl(
            foodApiService = foodApiService,
            favoriteFoodDao = favoriteFoodDao,
            ioDispatcher = testDispatcher,
            json = json
        )
    }

    @Test
    fun `searchFoods returns mapped domain list on success`() = runTest(testDispatcher) {
        val mockDto = FoodSearchResponseDto(
            data = listOf(
                FoodSummaryDto(
                    id = 1L,
                    canonicalName = "Mangú de Plátano Verde",
                    brand = null,
                    category = FoodCategoryDto(id = 12L, name = "Platos Preparados", slug = "platos-preparados"),
                    countryCode = "DO",
                    verified = true,
                    macrosPer100g = MacroBreakdownDto(
                        calories = 155,
                        proteinG = 1.5,
                        carbsG = 31.0,
                        fatG = 3.2
                    )
                )
            )
        )

        coEvery { foodApiService.searchFoods(query = "mangu", categoryId = null, country = "DO", page = 1, perPage = 20) } returns mockDto

        val result = repository.searchFoods(query = "mangu")

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals("Mangú de Plátano Verde", data[0].canonicalName)
        assertEquals("DO", data[0].countryCode)
        assertEquals(155, data[0].macrosPer100g.calories)
    }

    @Test
    fun `getFoodDetail returns complete food detail on success`() = runTest(testDispatcher) {
        val mockDetail = FoodDetailResponseDto(
            data = FoodDetailDto(
                id = 1L,
                canonicalName = "Mangú de Plátano Verde",
                brand = BrandDto(id = 1L, name = "Criollo"),
                countryCode = "DO",
                portions = listOf(
                    FoodPortionDto(id = 10L, portionName = "1 porción (200g)", gramWeight = 200.0, isDefault = true)
                ),
                nutrients = listOf(
                    FoodNutrientDto(id = 1L, code = "calories", name = "Calorías", unit = "kcal", amount = 155.0)
                )
            )
        )

        coEvery { foodApiService.getFoodDetail(1L) } returns mockDetail

        val result = repository.getFoodDetail(1L)

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1L, data.id)
        assertEquals("Mangú de Plátano Verde", data.canonicalName)
        assertEquals(1, data.portions.size)
        assertEquals(200.0, data.portions[0].gramWeight, 0.01)
    }

    @Test
    fun `calculateNutrition returns calculation result`() = runTest(testDispatcher) {
        val mockCalc = CalculateFoodNutritionResponseDto(
            data = NutritionCalculationDto(
                foodId = 1L,
                foodName = "Mangú de Plátano Verde",
                quantity = 1.5,
                unit = "porción",
                grams = 300.0,
                caloriesSnapshot = 465,
                proteinSnapshot = 4.5,
                carbsSnapshot = 93.0,
                fatSnapshot = 9.6
            )
        )

        coEvery { foodApiService.calculateFoodNutrition(1L, any()) } returns mockCalc

        val result = repository.calculateNutrition(foodId = 1L, quantity = 1.5, portionId = 10L, unit = "porción")

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(300.0, data.grams, 0.01)
        assertEquals(465, data.caloriesSnapshot)
    }

    @Test
    fun `toggleFavorite calls API and caches in Room`() = runTest(testDispatcher) {
        val food = FoodSummary(
            id = 1L,
            canonicalName = "Mangú",
            macrosPer100g = MacroBreakdown(155, 1.5, 31.0, 3.2)
        )

        coEvery { foodApiService.toggleFavoriteFood(1L) } returns ToggleFavoriteResponseDto(isFavorite = true, foodId = 1L)

        val result = repository.toggleFavorite(food)

        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
        coVerify { favoriteFoodDao.insertFavorite(any()) }
    }
}
