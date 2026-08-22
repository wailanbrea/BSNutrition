package com.bsnutrition.app.feature.ocr

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.FoodRepository
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.network.dto.ParsedLabelDataDto
import com.bsnutrition.app.core.network.dto.ParsedNutrientsDto
import com.bsnutrition.app.core.network.dto.ParsedServingDto
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionLabelScanViewModelTest {

    private lateinit var foodRepository: FoodRepository
    private lateinit var viewModel: NutritionLabelScanViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleParsedDto = ParsedLabelDataDto(
        serving = ParsedServingDto(
            name = "1 porción",
            weightGrams = 40.0,
            unit = "g"
        ),
        perServing = ParsedNutrientsDto(
            calories = 180,
            fatG = 7.0,
            carbsG = 26.0,
            proteinG = 3.0
        ),
        per100g = ParsedNutrientsDto(
            calories = 450,
            fatG = 17.5,
            carbsG = 65.0,
            proteinG = 7.5,
            sodiumMg = 200.0,
            fiberG = 4.0,
            sugarsG = 15.0
        ),
        confidence = 1.0
    )

    private val sampleCreatedFood = FoodDetail(
        id = 99L,
        canonicalName = "Galletas de Chocolate",
        brandName = "Dominicana Sweet",
        categoryName = "Snacks",
        countryCode = "DO",
        defaultBasisAmount = 100.0,
        defaultBasisUnit = "g",
        nutrients = emptyList(),
        portions = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        foodRepository = mockk(relaxed = true)
        viewModel = NutritionLabelScanViewModel(foodRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onRawTextCaptured transitions to EditAndConfirm on parse success`() = runTest {
        coEvery { foodRepository.parseNutritionLabel(any()) } returns Result.Success(sampleParsedDto)

        viewModel.onRawTextCaptured("Calories 180 Total Fat 7g Total Carbohydrate 26g Protein 3g", "7461234567890")

        val state = viewModel.uiState.value
        assertTrue(state is NutritionLabelUiState.EditAndConfirm)
        val editState = state as NutritionLabelUiState.EditAndConfirm
        assertEquals("7461234567890", editState.draft.barcode)
        assertEquals(40.0, editState.draft.servingGrams, 0.01)
        assertEquals(450, editState.draft.calories100g)
        assertEquals(7.5, editState.draft.protein100g, 0.01)
        assertEquals(65.0, editState.draft.carbs100g, 0.01)
        assertEquals(17.5, editState.draft.fat100g, 0.01)
    }

    @Test
    fun `saveProduct validates canonical name and fails if blank`() = runTest {
        coEvery { foodRepository.parseNutritionLabel(any()) } returns Result.Success(sampleParsedDto)
        viewModel.onRawTextCaptured("test raw text")

        viewModel.saveProduct()

        val state = viewModel.uiState.value
        assertTrue(state is NutritionLabelUiState.Error)
        assertEquals("Por favor ingresa el nombre del producto.", (state as NutritionLabelUiState.Error).message)
    }

    @Test
    fun `saveProduct calls createFoodFromLabel and transitions to Success state`() = runTest {
        coEvery { foodRepository.parseNutritionLabel(any()) } returns Result.Success(sampleParsedDto)
        coEvery { foodRepository.createFoodFromLabel(any()) } returns Result.Success(sampleCreatedFood)

        viewModel.onRawTextCaptured("test raw text")
        val currentDraft = (viewModel.uiState.value as NutritionLabelUiState.EditAndConfirm).draft
        viewModel.updateDraft(currentDraft.copy(canonicalName = "Galletas de Chocolate", brandName = "Dominicana Sweet"))

        viewModel.saveProduct(logToDiary = true)

        val state = viewModel.uiState.value
        assertTrue(state is NutritionLabelUiState.Success)
        assertEquals("Galletas de Chocolate", (state as NutritionLabelUiState.Success).food.canonicalName)

        coVerify {
            foodRepository.createFoodFromLabel(
                match { req ->
                    req.canonicalName == "Galletas de Chocolate" &&
                    req.brandName == "Dominicana Sweet" &&
                    req.calories100g == 450
                }
            )
        }
    }
}
