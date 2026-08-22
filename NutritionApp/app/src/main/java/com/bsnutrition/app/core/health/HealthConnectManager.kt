package com.bsnutrition.app.core.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Volume
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

enum class HealthConnectAvailability {
    AVAILABLE,
    NOT_INSTALLED,
    NOT_SUPPORTED
}

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val healthConnectClient by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
        HealthPermission.getWritePermission(NutritionRecord::class)
    )

    fun checkAvailability(): HealthConnectAvailability {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readDailySteps(startTime: Instant, endTime: Instant): Long {
        val client = healthConnectClient ?: return 0L
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun readLatestWeight(startTime: Instant, endTime: Instant): Double? {
        val client = healthConnectClient ?: return null
        return try {
            val request = ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                ascendingOrder = false,
                pageSize = 1
            )
            val response = client.readRecords(request)
            response.records.firstOrNull()?.weight?.inKilograms
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readActiveCalories(startTime: Instant, endTime: Instant): Int {
        val client = healthConnectClient ?: return 0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val energy = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
            energy?.inKilocalories?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun writeHydration(
        volumeLitres: Double,
        time: Instant,
        clientRecordId: String
    ): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val record = HydrationRecord(
                startTime = time,
                startZoneOffset = null,
                endTime = time.plusSeconds(60),
                endZoneOffset = null,
                volume = Volume.liters(volumeLitres),
                metadata = Metadata(clientRecordId = clientRecordId)
            )
            client.insertRecords(listOf(record))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun writeNutrition(
        caloriesKcal: Double,
        proteinG: Double,
        carbsG: Double,
        fatG: Double,
        time: Instant,
        clientRecordId: String
    ): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val record = NutritionRecord(
                startTime = time,
                startZoneOffset = null,
                endTime = time.plusSeconds(60),
                endZoneOffset = null,
                energy = Energy.kilocalories(caloriesKcal),
                protein = Mass.grams(proteinG),
                totalCarbohydrate = Mass.grams(carbsG),
                totalFat = Mass.grams(fatG),
                metadata = Metadata(clientRecordId = clientRecordId)
            )
            client.insertRecords(listOf(record))
            true
        } catch (e: Exception) {
            false
        }
    }
}
