package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.network.api.ProgressApiService
import com.bsnutrition.app.core.network.dto.StatisticsDataDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val progressApiService: ProgressApiService
) : StatisticsRepository {

    override suspend fun getSummary(period: String): Result<StatisticsDataDto> = withContext(Dispatchers.IO) {
        try {
            val response = progressApiService.getStatistics(period)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(Exception("Error al obtener estadísticas (${response.code()})"), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error al cargar estadísticas.")
        }
    }
}
