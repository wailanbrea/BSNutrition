package com.bsnutrition.app.feature.photo

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.AiPhotoRepository
import com.bsnutrition.app.core.data.repository.DiaryRepository
import com.bsnutrition.app.core.model.AiPhotoAnalysis
import com.bsnutrition.app.core.model.AiPhotoItem
import com.bsnutrition.app.core.model.FoodCandidate
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AiFoodPhotoViewModelTest {

    private lateinit var aiPhotoRepository: AiPhotoRepository
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var viewModel: AiFoodPhotoViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleAnalysis = AiPhotoAnalysis(
        id = 42L,
        status = "completed",
        dishName = "Mangú con Tres Golpes",
        summary = "Mangú tradicional con salami, queso y huevo frito",
        confidenceScore = 0.95,
        provider = "openai",
        totalCalories = 655,
        totalProteinG = 29.8,
        totalCarbsG = 64.9,
        totalFatG = 30.4,
        items = listOf(
            AiPhotoItem(
                id = 1L,
                foodId = 100L,
                name = "Mangú",
                matchedName = "Mangú de Plátano Verde",
                weightGrams = 200.0,
                portionDescription = "1 taza",
                preparationMethod = "hervido",
                confidence = 0.95,
                calories = 310,
                proteinG = 3.0,
                carbsG = 62.0,
                fatG = 6.4,
                candidates = listOf(
                    FoodCandidate(100L, "Mangú de Plátano Verde", null, 0.98, "exact", 155.0, 1.5, 31.0, 3.2),
                    FoodCandidate(101L, "Mangú de Plátano Maduro", null, 0.85, "token", 160.0, 1.2, 35.0, 2.8)
                )
            ),
            AiPhotoItem(
                id = 2L,
                foodId = 102L,
                name = "Salami frito",
                matchedName = "Salami Dominicano Frito",
                weightGrams = 60.0,
                portionDescription = "2 rodajas",
                preparationMethod = "frito",
                confidence = 0.92,
                calories = 195,
                proteinG = 9.5,
                carbsG = 1.5,
                fatG = 17.0,
                candidates = emptyList()
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        aiPhotoRepository = mockk(relaxed = true)
        diaryRepository = mockk(relaxed = true)
        viewModel = AiFoodPhotoViewModel(aiPhotoRepository, diaryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `analyzePhoto transitions to Review state with editable items on success`() = runTest {
        val fakeFile = mockk<File>()
        coEvery { aiPhotoRepository.analyzeFoodPhoto(any(), any(), any()) } returns Result.Success(sampleAnalysis)

        viewModel.analyzePhoto(fakeFile)

        val state = viewModel.uiState.value
        assertTrue(state is AiFoodPhotoUiState.Review)
        val reviewState = state as AiFoodPhotoUiState.Review
        assertEquals(42L, reviewState.analysisId)
        assertEquals("Mangú con Tres Golpes", reviewState.dishName)
        assertEquals(2, reviewState.items.size)
        assertEquals("Mangú de Plátano Verde", reviewState.items[0].displayName)
        assertEquals(505, reviewState.totalCalories) // 310 + 195
    }

    @Test
    fun `updateItemWeight recalculates calories and macros proportionally`() = runTest {
        val fakeFile = mockk<File>()
        coEvery { aiPhotoRepository.analyzeFoodPhoto(any(), any(), any()) } returns Result.Success(sampleAnalysis)
        viewModel.analyzePhoto(fakeFile)

        // Change Mangú from 200g (310 kcal) to 100g (155 kcal)
        viewModel.updateItemWeight(0, 100.0)

        val state = viewModel.uiState.value as AiFoodPhotoUiState.Review
        assertEquals(100.0, state.items[0].weightGrams, 0.01)
        assertEquals(155, state.items[0].calories)
        assertEquals(350, state.totalCalories) // 155 + 195
    }

    @Test
    fun `selectCandidate updates food selection and recalculates nutrients`() = runTest {
        val fakeFile = mockk<File>()
        coEvery { aiPhotoRepository.analyzeFoodPhoto(any(), any(), any()) } returns Result.Success(sampleAnalysis)
        viewModel.analyzePhoto(fakeFile)

        val newCandidate = sampleAnalysis.items[0].candidates[1] // Mangú de Plátano Maduro
        viewModel.selectCandidate(0, newCandidate)

        val state = viewModel.uiState.value as AiFoodPhotoUiState.Review
        assertEquals(101L, state.items[0].selectedFoodId)
        assertEquals("Mangú de Plátano Maduro", state.items[0].displayName)
        assertEquals(320, state.items[0].calories) // (160 * 200) / 100
    }

    @Test
    fun `removeItem removes item and updates total calories`() = runTest {
        val fakeFile = mockk<File>()
        coEvery { aiPhotoRepository.analyzeFoodPhoto(any(), any(), any()) } returns Result.Success(sampleAnalysis)
        viewModel.analyzePhoto(fakeFile)

        viewModel.removeItem(1) // Remove salami

        val state = viewModel.uiState.value as AiFoodPhotoUiState.Review
        assertEquals(1, state.items.size)
        assertEquals("Mangú de Plátano Verde", state.items[0].displayName)
        assertEquals(310, state.totalCalories)
    }

    @Test
    fun `confirmAndLog calls repository confirmAnalysis and transitions to LoggedSuccess`() = runTest {
        val fakeFile = mockk<File>()
        coEvery { aiPhotoRepository.analyzeFoodPhoto(any(), any(), any()) } returns Result.Success(sampleAnalysis)
        coEvery { aiPhotoRepository.confirmAnalysis(any(), any(), any(), any()) } returns Result.Success(2)

        viewModel.analyzePhoto(fakeFile)
        viewModel.confirmAndLog("2026-08-21", "breakfast")

        val state = viewModel.uiState.value
        assertTrue(state is AiFoodPhotoUiState.LoggedSuccess)
        assertEquals(2, (state as AiFoodPhotoUiState.LoggedSuccess).count)

        coVerify { aiPhotoRepository.confirmAnalysis(42L, "2026-08-21", "breakfast", any()) }
    }
}
