package com.bsnutrition.app.feature.health

import com.bsnutrition.app.core.health.HealthConnectAvailability
import com.bsnutrition.app.core.health.HealthConnectManager
import com.bsnutrition.app.core.health.HealthConnectPreferences
import com.bsnutrition.app.core.health.HealthConnectSettings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HealthConnectViewModelTest {

    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var healthConnectPreferences: HealthConnectPreferences
    private lateinit var viewModel: HealthConnectViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val initialSettings = HealthConnectSettings(
        isEnabled = true,
        syncSteps = true,
        syncWeight = true,
        syncActiveCalories = true,
        exportNutrition = true,
        exportHydration = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        healthConnectManager = mockk(relaxed = true)
        healthConnectPreferences = mockk(relaxed = true)

        coEvery { healthConnectPreferences.settingsFlow } returns flowOf(initialSettings)
        coEvery { healthConnectManager.checkAvailability() } returns HealthConnectAvailability.AVAILABLE
        coEvery { healthConnectManager.hasAllPermissions() } returns true
        coEvery { healthConnectManager.readDailySteps(any(), any()) } returns 8450L
        coEvery { healthConnectManager.readLatestWeight(any(), any()) } returns 76.5
        coEvery { healthConnectManager.readActiveCalories(any(), any()) } returns 420

        viewModel = HealthConnectViewModel(healthConnectManager, healthConnectPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkStatus verifies availability, permissions, and syncs initial metrics`() = runTest {
        viewModel.checkStatus()

        val state = viewModel.uiState.value
        assertEquals(HealthConnectAvailability.AVAILABLE, state.availability)
        assertTrue(state.hasPermissions)
        assertEquals(8450L, state.todaySteps)
        assertEquals(76.5, state.latestWeightKg!!, 0.01)
        assertEquals(420, state.activeCalories)
    }

    @Test
    fun `toggleHealthConnect updates preferences`() = runTest {
        viewModel.toggleHealthConnect(false)

        coVerify {
            healthConnectPreferences.updateSettings(
                match { !it.isEnabled }
            )
        }
    }

    @Test
    fun `updateSetting updates individual metric synchronization toggles`() = runTest {
        viewModel.updateSetting { it.copy(syncSteps = false, exportNutrition = false) }

        coVerify {
            healthConnectPreferences.updateSettings(
                match { !it.syncSteps && !it.exportNutrition }
            )
        }
    }
}
