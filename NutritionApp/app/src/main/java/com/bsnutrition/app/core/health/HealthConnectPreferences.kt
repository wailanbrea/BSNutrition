package com.bsnutrition.app.core.health

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class HealthConnectSettings(
    val isEnabled: Boolean = false,
    val syncSteps: Boolean = true,
    val syncWeight: Boolean = true,
    val syncActiveCalories: Boolean = true,
    val exportNutrition: Boolean = true,
    val exportHydration: Boolean = true
)

@Singleton
class HealthConnectPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val keyIsEnabled = booleanPreferencesKey("hc_is_enabled")
    private val keySyncSteps = booleanPreferencesKey("hc_sync_steps")
    private val keySyncWeight = booleanPreferencesKey("hc_sync_weight")
    private val keySyncActiveCalories = booleanPreferencesKey("hc_sync_active_calories")
    private val keyExportNutrition = booleanPreferencesKey("hc_export_nutrition")
    private val keyExportHydration = booleanPreferencesKey("hc_export_hydration")

    val settingsFlow: Flow<HealthConnectSettings> = dataStore.data.map { prefs ->
        HealthConnectSettings(
            isEnabled = prefs[keyIsEnabled] ?: false,
            syncSteps = prefs[keySyncSteps] ?: true,
            syncWeight = prefs[keySyncWeight] ?: true,
            syncActiveCalories = prefs[keySyncActiveCalories] ?: true,
            exportNutrition = prefs[keyExportNutrition] ?: true,
            exportHydration = prefs[keyExportHydration] ?: true
        )
    }

    suspend fun updateSettings(settings: HealthConnectSettings) {
        dataStore.edit { prefs ->
            prefs[keyIsEnabled] = settings.isEnabled
            prefs[keySyncSteps] = settings.syncSteps
            prefs[keySyncWeight] = settings.syncWeight
            prefs[keySyncActiveCalories] = settings.syncActiveCalories
            prefs[keyExportNutrition] = settings.exportNutrition
            prefs[keyExportHydration] = settings.exportHydration
        }
    }
}
