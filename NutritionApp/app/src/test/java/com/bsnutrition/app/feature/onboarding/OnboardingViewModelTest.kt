package com.bsnutrition.app.feature.onboarding

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.GoalRepository
import com.bsnutrition.app.core.model.NutritionGoal
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val goalRepository: GoalRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(goalRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onboarding_initialState_startsAtBirthSexStep() {
        val state = viewModel.uiState.value
        assertEquals(OnboardingStep.BIRTH_SEX, state.currentStep)
        assertFalse(state.isOnboardingComplete)
    }

    @Test
    fun onboarding_stepProgression_movesSequentiallyForwardAndBackward() {
        viewModel.nextStep()
        assertEquals(OnboardingStep.HEIGHT_WEIGHT, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(OnboardingStep.ACTIVITY, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(OnboardingStep.GOAL_RATE, viewModel.uiState.value.currentStep)

        viewModel.nextStep()
        assertEquals(OnboardingStep.UNITS, viewModel.uiState.value.currentStep)

        viewModel.previousStep()
        assertEquals(OnboardingStep.GOAL_RATE, viewModel.uiState.value.currentStep)
    }

    @Test
    fun calculateGoals_whenSuccessful_populatesCalculatedGoal() = runTest {
        val goal = NutritionGoal(
            calorieTarget = 1800,
            proteinTargetG = 135f,
            carbohydrateTargetG = 180f,
            fatTargetG = 60f,
            waterTargetMl = 2500
        )

        coEvery {
            goalRepository.calculateGoal(any(), any(), any(), any(), any(), any(), any())
        } returns Result.Success(goal)

        viewModel.calculateGoals()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.calculatedGoal)
        assertEquals(1800, state.calculatedGoal?.calorieTarget)
        assertFalse(state.isLoading)
    }

    @Test
    fun completeOnboarding_whenSuccessful_marksComplete() = runTest {
        val goal = NutritionGoal(
            calorieTarget = 2000,
            proteinTargetG = 150f,
            carbohydrateTargetG = 200f,
            fatTargetG = 65f
        )

        coEvery { goalRepository.saveGoal(any()) } returns Result.Success(goal)

        // Set state to review with calculated goal
        viewModel.updateHeightAndWeight(180f, 80f)
        viewModel.nextStep() // HEIGHT_WEIGHT
        viewModel.nextStep() // ACTIVITY
        viewModel.nextStep() // GOAL_RATE
        viewModel.nextStep() // UNITS
        viewModel.nextStep() // REVIEW

        viewModel.calculateGoals()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.completeOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOnboardingComplete)
    }
}
