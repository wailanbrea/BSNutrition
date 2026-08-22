package com.bsnutrition.app.feature.voice

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.AiPhotoRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class TextVoiceLoggingViewModelTest {

    private lateinit var aiRepository: AiPhotoRepository
    private lateinit var viewModel: TextVoiceLoggingViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleAnalysis = AiPhotoAnalysis(
        id = 12L,
        status = "completed",
        dishName = "Registro de Texto / Voz",
        summary = "Arroz blanco con habichuelas y pollo guisado",
        confidenceScore = 0.95,
        provider = "text_nlp",
        totalCalories = 620,
        totalProteinG = 38.0,
        totalCarbsG = 75.0,
        totalFatG = 16.0,
        items = listOf(
            AiPhotoItem(
                id = 1L,
                foodId = 201L,
                name = "Arroz blanco",
                matchedName = "Arroz Blanco Cocido",
                weightGrams = 180.0,
                portionDescription = "1 taza",
                preparationMethod = "hervido",
                confidence = 0.95,
                calories = 234,
                proteinG = 4.5,
                carbsG = 50.4,
                fatG = 0.5,
                candidates = emptyList()
            ),
            AiPhotoItem(
                id = 2L,
                foodId = 202L,
                name = "Pollo guisado",
                matchedName = "Pollo Guisado Dominicano",
                weightGrams = 150.0,
                portionDescription = "1 presa",
                preparationMethod = "guisado",
                confidence = 0.92,
                calories = 245,
                proteinG = 28.5,
                carbsG = 3.0,
                fatG = 12.5,
                candidates = emptyList()
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        aiRepository = mockk(relaxed = true)
        viewModel = TextVoiceLoggingViewModel(aiRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitText transitions to Review state with editable items`() = runTest {
        coEvery { aiRepository.parseMealText(any(), any(), any()) } returns Result.Success(sampleAnalysis)

        viewModel.onTextChanged("Arroz blanco y pollo guisado")
        viewModel.submitText()

        val state = viewModel.uiState.value
        assertTrue(state is TextVoiceLoggingUiState.Review)
        val reviewState = state as TextVoiceLoggingUiState.Review
        assertEquals(12L, reviewState.analysisId)
        assertEquals(2, reviewState.items.size)
        assertEquals(479, reviewState.totalCalories) // 234 + 245
    }

    @Test
    fun `updateItemWeight adjusts calories and macros proportionally`() = runTest {
        coEvery { aiRepository.parseMealText(any(), any(), any()) } returns Result.Success(sampleAnalysis)
        viewModel.submitText("test meal text")

        // Halve arroz weight from 180g (234 kcal) to 90g (117 kcal)
        viewModel.updateItemWeight(0, 90.0)

        val state = viewModel.uiState.value as TextVoiceLoggingUiState.Review
        assertEquals(90.0, state.items[0].weightGrams, 0.01)
        assertEquals(117, state.items[0].calories)
        assertEquals(362, state.totalCalories) // 117 + 245
    }

    @Test
    fun `confirmAndLog sends confirmed items to repository and transitions to LoggedSuccess`() = runTest {
        coEvery { aiRepository.parseMealText(any(), any(), any()) } returns Result.Success(sampleAnalysis)
        coEvery { aiRepository.confirmAnalysis(any(), any(), any(), any()) } returns Result.Success(2)

        viewModel.submitText("test meal text")
        viewModel.confirmAndLog("2026-08-21", "lunch")

        val state = viewModel.uiState.value
        assertTrue(state is TextVoiceLoggingUiState.LoggedSuccess)
        assertEquals(2, (state as TextVoiceLoggingUiState.LoggedSuccess).count)

        coVerify { aiRepository.confirmAnalysis(12L, "2026-08-21", "lunch", any()) }
    }
}
