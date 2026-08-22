package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.api.RecipeApiService
import com.bsnutrition.app.core.network.dto.CreateRecipeRequest
import com.bsnutrition.app.core.network.dto.RecipeDto
import com.bsnutrition.app.core.network.dto.RecipeIngredientDto
import com.bsnutrition.app.core.network.dto.RecipeListResponse
import com.bsnutrition.app.core.network.dto.RecipePaginationDto
import com.bsnutrition.app.core.network.dto.SingleRecipeResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class RecipeRepositoryTest {

    private lateinit var recipeApiService: RecipeApiService
    private lateinit var repository: RecipeRepositoryImpl

    private val sampleRecipe = RecipeDto(
        id = 10L,
        name = "Pollo a la Criolla",
        servings = 4,
        caloriesPerServing = 350,
        proteinPerServingG = 40.0,
        carbsPerServingG = 20.0,
        fatPerServingG = 10.0,
        ingredients = listOf(
            RecipeIngredientDto(customName = "Pechuga", grams = 500.0, calories = 800)
        )
    )

    @Before
    fun setUp() {
        recipeApiService = mockk(relaxed = true)
        repository = RecipeRepositoryImpl(recipeApiService)
    }

    @Test
    fun `getRecipes returns list from API`() = runTest {
        coEvery { recipeApiService.getRecipes(any(), any(), any()) } returns Response.success(
            RecipeListResponse("success", RecipePaginationDto(listOf(sampleRecipe)))
        )

        val result = repository.getRecipes()
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals("Pollo a la Criolla", data.first().name)
    }

    @Test
    fun `createRecipe calls API with request payload`() = runTest {
        coEvery { recipeApiService.createRecipe(any()) } returns Response.success(
            SingleRecipeResponse("success", sampleRecipe)
        )

        val request = CreateRecipeRequest(
            name = "Pollo a la Criolla",
            servings = 4,
            ingredients = listOf(RecipeIngredientDto(customName = "Pechuga", grams = 500.0))
        )

        val result = repository.createRecipe(request)
        assertTrue(result is Result.Success)
        assertEquals(10L, (result as Result.Success).data.id)

        coVerify { recipeApiService.createRecipe(request) }
    }

    @Test
    fun `logRecipeToDiary sends request with generated client_id`() = runTest {
        coEvery { recipeApiService.logToDiary(10L, any()) } returns Response.success(emptyMap())

        val result = repository.logRecipeToDiary(10L, "2026-08-21", "lunch", 1.0)
        assertTrue(result is Result.Success)

        coVerify { recipeApiService.logToDiary(10L, any()) }
    }
}
