package com.bsnutrition.app.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.health.HealthConnectAvailability
import com.bsnutrition.app.core.health.HealthConnectManager
import com.bsnutrition.app.core.health.HealthConnectPreferences
import com.bsnutrition.app.core.health.HealthConnectSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HealthConnectUiState(
    val availability: HealthConnectAvailability = HealthConnectAvailability.AVAILABLE,
    val hasPermissions: Boolean = false,
    val settings: HealthConnectSettings = HealthConnectSettings(),
    val todaySteps: Long = 0L,
    val latestWeightKg: Double? = null,
    val activeCalories: Int = 0,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val healthConnectPreferences: HealthConnectPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthConnectUiState())
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            healthConnectPreferences.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
        checkStatus()
    }

    fun checkStatus() {
        viewModelScope.launch {
            val availability = healthConnectManager.checkAvailability()
            val hasPerms = if (availability == HealthConnectAvailability.AVAILABLE) {
                healthConnectManager.hasAllPermissions()
            } else {
                false
            }

            _uiState.value = _uiState.value.copy(
                availability = availability,
                hasPermissions = hasPerms
            )

            if (hasPerms && _uiState.value.settings.isEnabled) {
                syncMetricsNow()
            }
        }
    }

    fun toggleHealthConnect(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(isEnabled = enabled)
            healthConnectPreferences.updateSettings(updated)
            if (enabled) {
                syncMetricsNow()
            }
        }
    }

    fun updateSetting(updater: (HealthConnectSettings) -> HealthConnectSettings) {
        viewModelScope.launch {
            val updated = updater(_uiState.value.settings)
            healthConnectPreferences.updateSettings(updated)
        }
    }

    fun syncMetricsNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, errorMessage = null)

            val zone = ZoneId.systemDefault()
            val startOfDay = LocalDate.now().atStartOfDay(zone).toInstant()
            val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant()
            val pastMonth = LocalDate.now().minusDays(30).atStartOfDay(zone).toInstant()

            val steps = healthConnectManager.readDailySteps(startOfDay, endOfDay)
            val weight = healthConnectManager.readLatestWeight(pastMonth, endOfDay)
            val activeCals = healthConnectManager.readActiveCalories(startOfDay, endOfDay)

            _uiState.value = _uiState.value.copy(
                todaySteps = steps,
                latestWeightKg = weight,
                activeCalories = activeCals,
                isSyncing = false
            )
        }
    }
}
