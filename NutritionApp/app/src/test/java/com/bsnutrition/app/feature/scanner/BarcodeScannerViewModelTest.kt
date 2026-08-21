package com.bsnutrition.app.feature.scanner

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.data.repository.FoodRepository
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodNutrient
import com.bsnutrition.app.core.model.FoodPortion
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
class BarcodeScannerViewModelTest {

    private lateinit var foodRepository: FoodRepository
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var viewModel: BarcodeScannerViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleFoodDetail = FoodDetail(
        id = 10L,
        canonicalName = "Leche Evaporada Carnation",
        brand = null,
        category = null,
        countryCode = "DO",
        verified = true,
        portions = listOf(
            FoodPortion(id = 1L, name = "lata", gramWeight = 315.0, isDefault = true)
        ),
        nutrients = listOf(
            FoodNutrient(nutrientCode = "ENERGY_KCAL", nutrientName = "Calorías", amountPer100g = 135.0, unitName = "kcal")
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        foodRepository = mockk(relaxed = true)
        diaryRepository = mockk(relaxed = true)
        viewModel = BarcodeScannerViewModel(
            foodRepository = foodRepository,
            diaryRepository = diaryRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onBarcodeDetected loads and transitions to Success when product found`() = runTest {
        coEvery { foodRepository.getFoodByBarcode("7460123456789") } returns Result.Success(sampleFoodDetail)

        viewModel.onBarcodeDetected("7460123456789")

        val state = viewModel.uiState.value
        assertTrue(state is BarcodeScannerUiState.Success)
        assertEquals("Leche Evaporada Carnation", (state as BarcodeScannerUiState.Success).food.canonicalName)
        assertEquals("7460123456789", state.barcode)
    }

    @Test
    fun `onBarcodeDetected transitions to NotFound when barcode 404s`() = runTest {
        coEvery { foodRepository.getFoodByBarcode("0000000000000") } returns Result.Error(
            exception = Exception("Not Found"),
            message = "No se encontró ningún producto con el código de barras 0000000000000.",
            code = 404
        )

        viewModel.onBarcodeDetected("0000000000000")

        val state = viewModel.uiState.value
        assertTrue(state is BarcodeScannerUiState.NotFound)
        assertEquals("0000000000000", (state as BarcodeScannerUiState.NotFound).barcode)
    }

    @Test
    fun `addScannedFoodToDiary logs meal entry and records recent food`() = runTest {
        coEvery {
            diaryRepository.addMealEntry(
                date = any(),
                mealType = any(),
                foodId = any(),
                portionId = any(),
                quantity = any(),
                unit = any(),
                customName = any(),
                calories = any(),
                proteinG = any(),
                carbsG = any(),
                fatG = any(),
                source = "barcode"
            )
        } returns Result.Success(mockk(relaxed = true))

        viewModel.addScannedFoodToDiary(
            food = sampleFoodDetail,
            mealType = "breakfast",
            quantity = 1.0,
            portionId = 1L,
            calories = 425,
            proteinG = 20.0,
            carbsG = 30.0,
            fatG = 24.0
        )

        val state = viewModel.uiState.value
        assertTrue(state is BarcodeScannerUiState.AddedToDiary)
        assertEquals("Leche Evaporada Carnation", (state as BarcodeScannerUiState.AddedToDiary).foodName)
        assertEquals(425, state.calories)

        coVerify { diaryRepository.addMealEntry(date = any(), mealType = "breakfast", foodId = 10L, any(), any(), any(), any(), calories = 425, any(), any(), any(), source = "barcode") }
        coVerify { foodRepository.recordRecentFood(any()) }
    }

    @Test
    fun `resumeScanning resets uiState to Scanning`() = runTest {
        coEvery { foodRepository.getFoodByBarcode(any()) } returns Result.Success(sampleFoodDetail)
        viewModel.onBarcodeDetected("12345")

        viewModel.resumeScanning()

        assertEquals(BarcodeScannerUiState.Scanning, viewModel.uiState.value)
    }
}
