package com.bsnutrition.app.feature.search

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.FoodRepository
import com.bsnutrition.app.core.model.FoodCategory
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodNutrient
import com.bsnutrition.app.core.model.FoodPortion
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.MacroBreakdown
import com.bsnutrition.app.core.model.NutritionCalculation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var foodRepository: FoodRepository
    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleFoodSummary = FoodSummary(
        id = 1L,
        canonicalName = "Mangú de Plátano Verde",
        category = FoodCategory(id = 12L, name = "Platos Preparados", slug = "platos-preparados"),
        countryCode = "DO",
        verified = true,
        macrosPer100g = MacroBreakdown(calories = 155, proteinG = 1.5, carbsG = 31.0, fatG = 3.2)
    )

    private val sampleFoodDetail = FoodDetail(
        id = 1L,
        canonicalName = "Mangú de Plátano Verde",
        countryCode = "DO",
        portions = listOf(
            FoodPortion(id = 10L, portionName = "1 taza (200g)", gramWeight = 200.0, isDefault = true),
            FoodPortion(id = 11L, portionName = "1 plato grande (300g)", gramWeight = 300.0, isDefault = false)
        ),
        nutrients = listOf(
            FoodNutrient(id = 1L, code = "calories", name = "Calorías", unit = "kcal", amount = 155.0)
        )
    )

    private val sampleCalculation = NutritionCalculation(
        foodId = 1L,
        foodName = "Mangú de Plátano Verde",
        quantity = 1.0,
        unit = "porción",
        grams = 200.0,
        caloriesSnapshot = 310,
        proteinSnapshot = 3.0,
        carbsSnapshot = 62.0,
        fatSnapshot = 6.4
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        foodRepository = mockk()

        coEvery { foodRepository.searchFoods(any(), any(), any(), any(), any()) } returns Result.Success(listOf(sampleFoodSummary))
        coEvery { foodRepository.getFoodDetail(1L) } returns Result.Success(sampleFoodDetail)
        coEvery { foodRepository.calculateNutrition(1L, any(), any(), any()) } returns Result.Success(sampleCalculation)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization triggers search and populates results`() = runTest {
        viewModel = SearchViewModel(foodRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.searchResults.size)
        assertEquals("Mangú de Plátano Verde", state.searchResults[0].canonicalName)
    }

    @Test
    fun `category selection updates selectedCategoryId and triggers search`() = runTest {
        viewModel = SearchViewModel(foodRepository)
        advanceUntilIdle()

        viewModel.onCategorySelected(12L)
        advanceUntilIdle()

        assertEquals(12L, viewModel.uiState.value.selectedCategoryId)
        coVerify { foodRepository.searchFoods(query = null, categoryId = 12L) }

        // Clicking again toggles off category
        viewModel.onCategorySelected(12L)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun `selectFood loads detail and calculates default portion nutrition`() = runTest {
        viewModel = SearchViewModel(foodRepository)
        advanceUntilIdle()

        viewModel.selectFood(1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.selectedFood)
        assertEquals("Mangú de Plátano Verde", state.selectedFood?.canonicalName)
        assertEquals(10L, state.selectedPortion?.id)
        assertNotNull(state.calculation)
        assertEquals(310, state.calculation?.caloriesSnapshot)
    }

    @Test
    fun `dismissFoodDetail clears selected food and calculation`() = runTest {
        viewModel = SearchViewModel(foodRepository)
        advanceUntilIdle()

        viewModel.selectFood(1L)
        advanceUntilIdle()

        viewModel.dismissFoodDetail()
        val state = viewModel.uiState.value

        assertNull(state.selectedFood)
        assertNull(state.calculation)
    }
}
