package com.bsnutrition.app.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.MacroBreakdown

@Entity(tableName = "favorite_foods")
data class FavoriteFoodEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "canonical_name")
    val canonicalName: String,
    val brand: String? = null,
    @ColumnInfo(name = "category_name")
    val categoryName: String? = null,
    @ColumnInfo(name = "country_code")
    val countryCode: String? = null,
    val calories: Int,
    @ColumnInfo(name = "protein_g")
    val proteinG: Double,
    @ColumnInfo(name = "carbs_g")
    val carbsG: Double,
    @ColumnInfo(name = "fat_g")
    val fatG: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = FoodSummary(
        id = id,
        canonicalName = canonicalName,
        brand = brand,
        countryCode = countryCode,
        verified = true,
        macrosPer100g = MacroBreakdown(
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG
        )
    )

    companion object {
        fun fromDomain(food: FoodSummary) = FavoriteFoodEntity(
            id = food.id,
            canonicalName = food.canonicalName,
            brand = food.brand,
            categoryName = food.category?.name,
            countryCode = food.countryCode,
            calories = food.macrosPer100g.calories,
            proteinG = food.macrosPer100g.proteinG,
            carbsG = food.macrosPer100g.carbsG,
            fatG = food.macrosPer100g.fatG
        )
    }
}
