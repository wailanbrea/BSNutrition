package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.dto.StatisticsDataDto

interface StatisticsRepository {
    suspend fun getSummary(period: String = "7d"): Result<StatisticsDataDto>
}
