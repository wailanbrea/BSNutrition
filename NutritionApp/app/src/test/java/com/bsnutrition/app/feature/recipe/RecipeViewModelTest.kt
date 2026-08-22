package com.bsnutrition.app.feature.recipe

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.RecipeRepository
import com.bsnutrition.app.core.network.dto.RecipeDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var viewModel: RecipeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleRecipe = RecipeDto(
        id = 1L,
        name = "Mangú con Huevo",
        servings = 2,
        caloriesPerServing = 380,
        proteinPerServingG = 16.0,
        carbsPerServingG = 45.0,
        fatPerServingG = 14.0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        recipeRepository = mockk(relaxed = true)
        coEvery { recipeRepository.getRecipes(any()) } returns Result.Success(listOf(sampleRecipe))
        viewModel = RecipeViewModel(recipeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads recipe list successfully`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(1, state.recipes.size)
        assertEquals("Mangú con Huevo", state.recipes.first().name)
    }

    @Test
    fun `loadRecipeDetails updates selectedRecipe`() = runTest {
        coEvery { recipeRepository.getRecipe(1L) } returns Result.Success(sampleRecipe)

        viewModel.loadRecipeDetails(1L)

        val state = viewModel.uiState.value
        assertNotNull(state.selectedRecipe)
        assertEquals(1L, state.selectedRecipe?.id)
        assertEquals(380, state.selectedRecipe?.caloriesPerServing)
    }

    @Test
    fun `logRecipeToDiary calls repository and invokes callback`() = runTest {
        coEvery { recipeRepository.logRecipeToDiary(1L, any(), any(), any()) } returns Result.Success(Unit)

        var callbackCalled = false
        viewModel.logRecipeToDiary(1L, "2026-08-21", "dinner", 1.0) {
            callbackCalled = true
        }

        assertTrue(callbackCalled)
        coVerify { recipeRepository.logRecipeToDiary(1L, "2026-08-21", "dinner", 1.0) }
    }
}
