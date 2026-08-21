package com.bsnutrition.app.core.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "diaries",
    indices = [
        Index(value = ["user_id", "diary_date"], unique = true),
        Index(value = ["diary_date"])
    ]
)
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val user_id: Long,
    val diary_date: String,
    val timezone: String = "America/Santo_Domingo",
    val notes: String? = null,
    val sync_status: String = "synced", // synced, pending_create, pending_update
    val updated_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "meals",
    indices = [
        Index(value = ["diary_id", "meal_type"], unique = true),
        Index(value = ["diary_id"])
    ]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val diary_id: Long,
    val meal_type: String,
    val name: String,
    val sort_order: Int = 0,
    val sync_status: String = "synced"
)

@Entity(
    tableName = "meal_entries",
    indices = [
        Index(value = ["meal_id"]),
        Index(value = ["client_id"], unique = true),
        Index(value = ["sync_status"]),
        Index(value = ["is_deleted"])
    ]
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val client_id: String? = null,
    val meal_id: Long,
    val food_id: Long? = null,
    val portion_id: Long? = null,
    val custom_name: String,
    val quantity: Double,
    val unit: String = "porción",
    val grams: Double = 100.0,
    val calories_snapshot: Int,
    val protein_snapshot: Double,
    val carbs_snapshot: Double,
    val fat_snapshot: Double,
    val fiber_snapshot: Double? = null,
    val sodium_snapshot: Double? = null,
    val sugar_snapshot: Double? = null,
    val source: String = "catalog",
    val version: Int = 1,
    val is_deleted: Boolean = false,
    val sync_status: String = "synced", // synced, pending_create, pending_update, pending_delete
    val updated_at: Long = System.currentTimeMillis()
)

data class MealWithEntries(
    @Embedded val meal: MealEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "meal_id"
    )
    val entries: List<MealEntryEntity> = emptyList()
)

data class DiaryWithMeals(
    @Embedded val diary: DiaryEntity,
    @Relation(
        entity = MealEntity::class,
        parentColumn = "id",
        entityColumn = "diary_id"
    )
    val mealsWithEntries: List<MealWithEntries> = emptyList()
)
