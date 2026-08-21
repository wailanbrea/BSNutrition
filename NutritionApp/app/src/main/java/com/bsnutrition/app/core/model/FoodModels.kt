package com.bsnutrition.app.core.model

data class FoodCategory(
    val id: Long,
    val name: String,
    val slug: String,
    val icon: String? = null
)

data class FoodPortion(
    val id: Long,
    val portionName: String,
    val gramWeight: Double,
    val amount: Double = 1.0,
    val unit: String = "porción",
    val isDefault: Boolean = false
)

data class FoodNutrient(
    val id: Long,
    val code: String,
    val name: String,
    val unit: String,
    val amount: Double,
    val basisAmount: Double = 100.0,
    val basisUnit: String = "g",
    val isMacro: Boolean = false
)

data class MacroBreakdown(
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double = 0.0
)

data class FoodSummary(
    val id: Long,
    val canonicalName: String,
    val brand: String? = null,
    val category: FoodCategory? = null,
    val countryCode: String? = null,
    val verified: Boolean = true,
    val source: String = "generic",
    val defaultBasisAmount: Double = 100.0,
    val defaultBasisUnit: String = "g",
    val macrosPer100g: MacroBreakdown,
    val defaultPortion: FoodPortion? = null
)

data class FoodDetail(
    val id: Long,
    val canonicalName: String,
    val brand: String? = null,
    val category: FoodCategory? = null,
    val countryCode: String? = null,
    val language: String = "es",
    val verified: Boolean = true,
    val source: String = "generic",
    val externalSourceId: String? = null,
    val defaultBasisAmount: Double = 100.0,
    val defaultBasisUnit: String = "g",
    val portions: List<FoodPortion> = emptyList(),
    val nutrients: List<FoodNutrient> = emptyList(),
    val barcodes: List<String> = emptyList(),
    val aliases: List<String> = emptyList()
)

data class NutritionCalculation(
    val foodId: Long,
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val grams: Double,
    val caloriesSnapshot: Int,
    val proteinSnapshot: Double,
    val carbsSnapshot: Double,
    val fatSnapshot: Double,
    val fiberSnapshot: Double = 0.0,
    val sodiumSnapshot: Double = 0.0,
    val sugarSnapshot: Double = 0.0,
    val nutrients: List<FoodNutrient> = emptyList(),
    val nutrientSnapshotJson: String? = null
)
