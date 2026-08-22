package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.database.WeightLogDao
import com.bsnutrition.app.core.database.WeightLogEntity
import com.bsnutrition.app.core.network.api.ProgressApiService
import com.bsnutrition.app.core.network.dto.WeightDataDto
import com.bsnutrition.app.core.network.dto.WeightLogsResponse
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

class WeightRepositoryTest {

    private lateinit var weightLogDao: WeightLogDao
    private lateinit var progressApiService: ProgressApiService
    private lateinit var repository: WeightRepositoryImpl

    private val sampleEntity = WeightLogEntity(
        id = 1L,
        clientId = "client-weight-1",
        logDate = "2026-08-21",
        weightKg = 78.5,
        occurredAt = 1724250000000L,
        source = "manual"
    )

    @Before
    fun setUp() {
        weightLogDao = mockk(relaxed = true)
        progressApiService = mockk(relaxed = true)
        repository = WeightRepositoryImpl(weightLogDao, progressApiService)
    }

    @Test
    fun `observeAllWeightLogs returns Flow from DAO`() = runTest {
        coEvery { weightLogDao.observeAllWeightLogs() } returns flowOf(listOf(sampleEntity))

        val logs = repository.observeAllWeightLogs().first()
        assertEquals(1, logs.size)
        assertEquals(78.5, logs.first().weightKg, 0.01)
    }

    @Test
    fun `logWeight inserts into Room and calls network api`() = runTest {
        coEvery { weightLogDao.insertOrUpdate(any()) } returns 5L
        coEvery { progressApiService.logWeight(any()) } returns Response.success(
            WeightLogsResponse("success", WeightDataDto(78.5, 75.0, emptyList()))
        )

        val result = repository.logWeight("2026-08-21", 78.5, "manual", "ayunas")
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(78.5, data.weightKg, 0.01)
        assertEquals(5L, data.id)

        coVerify { weightLogDao.insertOrUpdate(any()) }
        coVerify { progressApiService.logWeight(any()) }
    }

    @Test
    fun `deleteWeightLog soft deletes in Room and calls API`() = runTest {
        coEvery { progressApiService.deleteWeightLog(1L) } returns Response.success(mapOf("status" to "success"))

        val result = repository.deleteWeightLog(1L)
        assertTrue(result is Result.Success)

        coVerify { weightLogDao.softDeleteById(1L) }
        coVerify { progressApiService.deleteWeightLog(1L) }
    }
}
