package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.WaterLogDao
import com.bsnutrition.app.core.database.WaterLogEntity
import com.bsnutrition.app.core.network.api.ProgressApiService
import com.bsnutrition.app.core.network.dto.WaterDataDto
import com.bsnutrition.app.core.network.dto.WaterLogDto
import com.bsnutrition.app.core.network.dto.WaterLogsResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class WaterRepositoryTest {

    private lateinit var waterLogDao: WaterLogDao
    private lateinit var progressApiService: ProgressApiService
    private lateinit var repository: WaterRepositoryImpl

    private val sampleEntity = WaterLogEntity(
        id = 1L,
        clientId = "client-water-1",
        logDate = "2026-08-21",
        amountMl = 500,
        occurredAt = 1724250000000L,
        source = "quick_add"
    )

    @Before
    fun setUp() {
        waterLogDao = mockk(relaxed = true)
        progressApiService = mockk(relaxed = true)
        repository = WaterRepositoryImpl(waterLogDao, progressApiService)
    }

    @Test
    fun `observeWaterLogs delegates to DAO`() = runTest {
        coEvery { waterLogDao.observeWaterLogsForDate("2026-08-21") } returns flowOf(listOf(sampleEntity))

        val logs = repository.observeWaterLogs("2026-08-21").first()
        assertEquals(1, logs.size)
        assertEquals(500, logs.first().amountMl)
    }

    @Test
    fun `logWater inserts into Room and calls network api`() = runTest {
        coEvery { waterLogDao.insertOrUpdate(any()) } returns 10L
        coEvery { progressApiService.logWater(any()) } returns Response.success(
            WaterLogsResponse("success", WaterDataDto(500, 2500, emptyList()))
        )

        val result = repository.logWater("2026-08-21", 500)
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(500, data.amountMl)
        assertEquals(10L, data.id)

        coVerify { waterLogDao.insertOrUpdate(any()) }
        coVerify { progressApiService.logWater(any()) }
    }

    @Test
    fun `deleteWaterLog soft deletes in Room and calls API`() = runTest {
        coEvery { progressApiService.deleteWaterLog(1L) } returns Response.success(mapOf("status" to "success"))

        val result = repository.deleteWaterLog(1L)
        assertTrue(result is Result.Success)

        coVerify { waterLogDao.softDeleteById(1L) }
        coVerify { progressApiService.deleteWaterLog(1L) }
    }
}
