package com.bsnutrition.app.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Water DTOs
@Serializable
data class WaterLogsResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: WaterDataDto
)

@Serializable
data class WaterDataDto(
    @SerialName("total_ml") val totalMl: Int,
    @SerialName("target_ml") val targetMl: Int = 2500,
    @SerialName("logs") val logs: List<WaterLogDto> = emptyList()
)

@Serializable
data class WaterLogDto(
    @SerialName("id") val id: Long,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("log_date") val logDate: String,
    @SerialName("amount_ml") val amountMl: Int,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("source") val source: String = "manual"
)

@Serializable
data class LogWaterRequest(
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("log_date") val logDate: String,
    @SerialName("amount_ml") val amountMl: Int,
    @SerialName("occurred_at") val occurredAt: String? = null,
    @SerialName("source") val source: String = "manual"
)

// Weight DTOs
@Serializable
data class WeightLogsResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: WeightDataDto
)

@Serializable
data class WeightDataDto(
    @SerialName("current_weight_kg") val currentWeightKg: Double? = null,
    @SerialName("target_weight_kg") val targetWeightKg: Double? = null,
    @SerialName("logs") val logs: List<WeightLogDto> = emptyList()
)

@Serializable
data class WeightLogDto(
    @SerialName("id") val id: Long,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("log_date") val logDate: String,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("weight_lbs") val weightLbs: Double,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("source") val source: String = "manual",
    @SerialName("notes") val notes: String? = null
)

@Serializable
data class LogWeightRequest(
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("log_date") val logDate: String,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("occurred_at") val occurredAt: String? = null,
    @SerialName("source") val source: String = "manual",
    @SerialName("notes") val notes: String? = null
)

// Statistics DTOs
@Serializable
data class StatisticsResponse(
    @SerialName("status") val status: String,
    @SerialName("data") val data: StatisticsDataDto
)

@Serializable
data class StatisticsDataDto(
    @SerialName("period") val period: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("total_days") val totalDays: Int,
    @SerialName("tracked_days") val trackedDays: Int,
    @SerialName("targets") val targets: StatisticsTargetsDto,
    @SerialName("averages") val averages: StatisticsAveragesDto,
    @SerialName("macro_split") val macroSplit: MacroSplitDto,
    @SerialName("adherence_rate") val adherenceRate: Double,
    @SerialName("weight_summary") val weightSummary: WeightSummaryDto,
    @SerialName("daily_breakdown") val dailyBreakdown: List<DailyBreakdownDto> = emptyList()
)

@Serializable
data class StatisticsTargetsDto(
    @SerialName("calories") val calories: Int,
    @SerialName("protein_g") val proteinG: Double,
    @SerialName("carbs_g") val carbsG: Double,
    @SerialName("fat_g") val fatG: Double,
    @SerialName("water_ml") val waterMl: Int
)

@Serializable
data class StatisticsAveragesDto(
    @SerialName("calories") val calories: Int,
    @SerialName("protein_g") val proteinG: Double,
    @SerialName("carbs_g") val carbsG: Double,
    @SerialName("fat_g") val fatG: Double,
    @SerialName("water_ml") val waterMl: Int
)

@Serializable
data class MacroSplitDto(
    @SerialName("protein_pct") val proteinPct: Double,
    @SerialName("carbs_pct") val carbsPct: Double,
    @SerialName("fat_pct") val fatPct: Double
)

@Serializable
data class WeightSummaryDto(
    @SerialName("start_weight_kg") val startWeightKg: Double? = null,
    @SerialName("current_weight_kg") val currentWeightKg: Double? = null,
    @SerialName("change_kg") val changeKg: Double = 0.0
)

@Serializable
data class DailyBreakdownDto(
    @SerialName("date") val date: String,
    @SerialName("day_of_week") val dayOfWeek: String,
    @SerialName("calories") val calories: Int,
    @SerialName("protein_g") val proteinG: Double,
    @SerialName("carbs_g") val carbsG: Double,
    @SerialName("fat_g") val fatG: Double,
    @SerialName("water_ml") val waterMl: Int,
    @SerialName("target_calories") val targetCalories: Int,
    @SerialName("target_water_ml") val targetWaterMl: Int
)
