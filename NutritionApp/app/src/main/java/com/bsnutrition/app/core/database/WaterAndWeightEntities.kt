package com.bsnutrition.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_logs",
    indices = [
        Index(value = ["log_date"]),
        Index(value = ["client_id"], unique = true),
        Index(value = ["sync_status"]),
        Index(value = ["is_deleted"])
    ]
)
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val client_id: String? = null,
    val user_id: Long = 0,
    val log_date: String,
    val amount_ml: Int,
    val occurred_at: String? = null,
    val source: String = "manual",
    val is_deleted: Boolean = false,
    val sync_status: String = "synced", // synced, pending_create, pending_delete
    val updated_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "weight_logs",
    indices = [
        Index(value = ["log_date"]),
        Index(value = ["client_id"], unique = true),
        Index(value = ["sync_status"]),
        Index(value = ["is_deleted"])
    ]
)
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val client_id: String? = null,
    val user_id: Long = 0,
    val log_date: String,
    val weight_kg: Double,
    val body_fat_percentage: Double? = null,
    val notes: String? = null,
    val is_deleted: Boolean = false,
    val sync_status: String = "synced",
    val updated_at: Long = System.currentTimeMillis()
)
