package com.bsnutrition.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods_cache",
    indices = [
        Index(value = ["name"]),
        Index(value = ["cached_at"])
    ]
)
data class FoodCacheEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val brand_name: String? = null,
    val category_name: String? = null,
    val serving_size: Double = 100.0,
    val serving_unit: String = "g",
    val calories: Double,
    val protein_g: Double,
    val carbs_g: Double,
    val fat_g: Double,
    val fiber_g: Double = 0.0,
    val sodium_mg: Double = 0.0,
    val is_dominican: Boolean = false,
    val cached_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["entity_type"]),
        Index(value = ["created_at"]),
        Index(value = ["client_id"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entity_type: String, // meal_entry, water_log, weight_log, profile, goal
    val entity_id: Long? = null,
    val client_id: String? = null,
    val operation: String, // CREATE, UPDATE, DELETE
    val payload_json: String,
    val attempts: Int = 0,
    val last_error: String? = null,
    val created_at: Long = System.currentTimeMillis()
)
