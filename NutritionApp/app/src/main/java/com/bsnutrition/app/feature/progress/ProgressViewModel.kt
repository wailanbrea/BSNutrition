package com.bsnutrition.app.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.StatisticsRepository
import com.bsnutrition.app.core.data.repository.WaterRepository
import com.bsnutrition.app.core.data.repository.WeightRepository
import com.bsnutrition.app.core.database.WaterLogEntity
import com.bsnutrition.app.core.database.WeightLogEntity
import com.bsnutrition.app.core.network.dto.StatisticsDataDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProgressUiState(
    val selectedPeriod: String = "7d",
    val statistics: StatisticsDataDto? = null,
    val todayWaterMl: Int = 0,
    val waterTargetMl: Int = 2500,
    val waterLogs: List<WaterLogEntity> = emptyList(),
    val weightLogs: List<WeightLogEntity> = emptyList(),
    val latestWeightKg: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val waterRepository: WaterRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val todayStr = LocalDate.now().toString()

    init {
        observeLocalData()
        loadStatistics("7d")
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            waterRepository.observeTotalWater(todayStr).collect { total ->
                _uiState.value = _uiState.value.copy(todayWaterMl = total)
            }
        }

        viewModelScope.launch {
            waterRepository.observeWaterLogs(todayStr).collect { logs ->
                _uiState.value = _uiState.value.copy(waterLogs = logs)
            }
        }

        viewModelScope.launch {
            weightRepository.observeAllWeightLogs().collect { logs ->
                _uiState.value = _uiState.value.copy(
                    weightLogs = logs,
                    latestWeightKg = logs.firstOrNull()?.weightKg
                )
            }
        }
    }

    fun setPeriod(period: String) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStatistics(period)
    }

    fun loadStatistics(period: String = _uiState.value.selectedPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = statisticsRepository.getSummary(period)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        statistics = result.data,
                        waterTargetMl = result.data.targets.waterMl,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "No se pudieron cargar las estadísticas."
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.logWater(todayStr, amountMl)
        }
    }

    fun deleteWaterLog(id: Long) {
        viewModelScope.launch {
            waterRepository.deleteWaterLog(id)
        }
    }

    fun logWeight(weightKg: Double, notes: String? = null) {
        viewModelScope.launch {
            weightRepository.logWeight(todayStr, weightKg, notes = notes)
            loadStatistics()
        }
    }

    fun deleteWeightLog(id: Long) {
        viewModelScope.launch {
            weightRepository.deleteWeightLog(id)
            loadStatistics()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            waterRepository.syncWaterLogs(todayStr)
            weightRepository.syncWeightLogs()
            loadStatistics()
        }
    }
}
