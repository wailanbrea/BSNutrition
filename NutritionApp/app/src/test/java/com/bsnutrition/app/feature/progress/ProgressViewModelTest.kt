package com.bsnutrition.app.feature.progress

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.StatisticsRepository
import com.bsnutrition.app.core.data.repository.WaterRepository
import com.bsnutrition.app.core.data.repository.WeightRepository
import com.bsnutrition.app.core.database.WaterLogEntity
import com.bsnutrition.app.core.database.WeightLogEntity
import com.bsnutrition.app.core.network.dto.MacroSplitDto
import com.bsnutrition.app.core.network.dto.StatisticsAveragesDto
import com.bsnutrition.app.core.network.dto.StatisticsDataDto
import com.bsnutrition.app.core.network.dto.StatisticsTargetsDto
import com.bsnutrition.app.core.network.dto.WeightSummaryDto
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var waterRepository: WaterRepository
    private lateinit var weightRepository: WeightRepository
    private lateinit var viewModel: ProgressViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleStats = StatisticsDataDto(
        period = "7d",
        startDate = "2026-08-15",
        endDate = "2026-08-21",
        totalDays = 7,
        trackedDays = 6,
        targets = StatisticsTargetsDto(2000, 150.0, 200.0, 65.0, 2500),
        averages = StatisticsAveragesDto(1950, 145.0, 190.0, 60.0, 2400),
        macroSplit = MacroSplitDto(30.0, 45.0, 25.0),
        adherenceRate = 85.7,
        weightSummary = WeightSummaryDto(79.0, 78.2, -0.8),
        dailyBreakdown = emptyList()
    )

    private val sampleWeightEntity = WeightLogEntity(
        id = 1L,
        clientId = "w-1",
        logDate = "2026-08-21",
        weightKg = 78.2,
        occurredAt = 1724250000000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        statisticsRepository = mockk(relaxed = true)
        waterRepository = mockk(relaxed = true)
        weightRepository = mockk(relaxed = true)

        coEvery { waterRepository.observeTotalWater(any()) } returns flowOf(1750)
        coEvery { waterRepository.observeWaterLogs(any()) } returns flowOf(emptyList())
        coEvery { weightRepository.observeAllWeightLogs() } returns flowOf(listOf(sampleWeightEntity))
        coEvery { statisticsRepository.getSummary(any()) } returns Result.Success(sampleStats)

        viewModel = ProgressViewModel(statisticsRepository, waterRepository, weightRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads local flows and fetches 7d statistics`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(1750, state.todayWaterMl)
        assertEquals(78.2, state.latestWeightKg!!, 0.01)
        assertNotNull(state.statistics)
        assertEquals("7d", state.selectedPeriod)
        assertEquals(1950, state.statistics?.averages?.calories)
    }

    @Test
    fun `setPeriod reloads statistics for 30d`() = runTest {
        coEvery { statisticsRepository.getSummary("30d") } returns Result.Success(sampleStats.copy(period = "30d", totalDays = 30))

        viewModel.setPeriod("30d")

        val state = viewModel.uiState.value
        assertEquals("30d", state.selectedPeriod)
        assertEquals(30, state.statistics?.totalDays)
        coVerify { statisticsRepository.getSummary("30d") }
    }

    @Test
    fun `addWater calls waterRepository logWater`() = runTest {
        viewModel.addWater(500)
        coVerify { waterRepository.logWater(any(), 500, any()) }
    }

    @Test
    fun `logWeight calls weightRepository logWeight`() = runTest {
        viewModel.logWeight(77.9, "En ayunas")
        coVerify { weightRepository.logWeight(any(), 77.9, any(), "En ayunas") }
    }
}
