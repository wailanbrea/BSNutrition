package com.bsnutrition.app.core.network.model

import com.bsnutrition.app.core.model.FoodCategory
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodNutrient
import com.bsnutrition.app.core.model.FoodPortion
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.MacroBreakdown
import com.bsnutrition.app.core.model.NutritionCalculation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodCategoryDto(
    val id: Long,
    val name: String,
    val slug: String,
    val icon: String? = null
) {
    fun toDomain() = FoodCategory(
        id = id,
        name = name,
        slug = slug,
        icon = icon
    )
}

@Serializable
data class FoodPortionDto(
    val id: Long,
    @SerialName("portion_name") val portionName: String,
    @SerialName("gram_weight") val gramWeight: Double,
    val amount: Double = 1.0,
    val unit: String = "porción",
    @SerialName("is_default") val isDefault: Boolean = false
) {
    fun toDomain() = FoodPortion(
        id = id,
        portionName = portionName,
        gramWeight = gramWeight,
        amount = amount,
        unit = unit,
        isDefault = isDefault
    )
}

@Serializable
data class FoodNutrientDto(
    val id: Long,
    val code: String,
    val name: String,
    val unit: String,
    val amount: Double,
    @SerialName("basis_amount") val basisAmount: Double = 100.0,
    @SerialName("basis_unit") val basisUnit: String = "g",
    @SerialName("is_macro") val isMacro: Boolean = false
) {
    fun toDomain() = FoodNutrient(
        id = id,
        code = code,
        name = name,
        unit = unit,
        amount = amount,
        basisAmount = basisAmount,
        basisUnit = basisUnit,
        isMacro = isMacro
    )
}

@Serializable
data class MacroBreakdownDto(
    val calories: Int,
    @SerialName("protein_g") val proteinG: Double,
    @SerialName("carbs_g") val carbsG: Double,
    @SerialName("fat_g") val fatG: Double,
    @SerialName("fiber_g") val fiberG: Double = 0.0
) {
    fun toDomain() = MacroBreakdown(
        calories = calories,
        proteinG = proteinG,
        carbsG = carbsG,
        fatG = fatG,
        fiberG = fiberG
    )
}

@Serializable
data class FoodSummaryDto(
    val id: Long,
    @SerialName("canonical_name") val canonicalName: String,
    val brand: String? = null,
    val category: FoodCategoryDto? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val verified: Boolean = true,
    val source: String = "generic",
    @SerialName("default_basis_amount") val defaultBasisAmount: Double = 100.0,
    @SerialName("default_basis_unit") val defaultBasisUnit: String = "g",
    @SerialName("macros_per_100g") val macrosPer100g: MacroBreakdownDto,
    @SerialName("default_portion") val defaultPortion: FoodPortionDto? = null
) {
    fun toDomain() = FoodSummary(
        id = id,
        canonicalName = canonicalName,
        brand = brand,
        category = category?.toDomain(),
        countryCode = countryCode,
        verified = verified,
        source = source,
        defaultBasisAmount = defaultBasisAmount,
        defaultBasisUnit = defaultBasisUnit,
        macrosPer100g = macrosPer100g.toDomain(),
        defaultPortion = defaultPortion?.toDomain()
    )
}

@Serializable
data class FoodSearchResponseDto(
    val data: List<FoodSummaryDto>
)

@Serializable
data class BrandDto(
    val id: Long,
    val name: String
)

@Serializable
data class FoodDetailDto(
    val id: Long,
    @SerialName("canonical_name") val canonicalName: String,
    val brand: BrandDto? = null,
    val category: FoodCategoryDto? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val language: String = "es",
    val verified: Boolean = true,
    val source: String = "generic",
    @SerialName("external_source_id") val externalSourceId: String? = null,
    @SerialName("default_basis_amount") val defaultBasisAmount: Double = 100.0,
    @SerialName("default_basis_unit") val defaultBasisUnit: String = "g",
    val portions: List<FoodPortionDto> = emptyList(),
    val nutrients: List<FoodNutrientDto> = emptyList(),
    val barcodes: List<String> = emptyList(),
    val aliases: List<String> = emptyList()
) {
    fun toDomain() = FoodDetail(
        id = id,
        canonicalName = canonicalName,
        brand = brand?.name,
        category = category?.toDomain(),
        countryCode = countryCode,
        language = language,
        verified = verified,
        source = source,
        externalSourceId = externalSourceId,
        defaultBasisAmount = defaultBasisAmount,
        defaultBasisUnit = defaultBasisUnit,
        portions = portions.map { it.toDomain() },
        nutrients = nutrients.map { it.toDomain() },
        barcodes = barcodes,
        aliases = aliases
    )
}

@Serializable
data class FoodDetailResponseDto(
    val data: FoodDetailDto
)

@Serializable
data class CalculateFoodNutritionRequestDto(
    val quantity: Double,
    @SerialName("portion_id") val portionId: Long? = null,
    val unit: String? = "g"
)

@Serializable
data class NutritionCalculationDto(
    @SerialName("food_id") val foodId: Long,
    @SerialName("food_name") val foodName: String,
    val quantity: Double,
    val unit: String,
    val grams: Double,
    @SerialName("calories_snapshot") val caloriesSnapshot: Int,
    @SerialName("protein_snapshot") val proteinSnapshot: Double,
    @SerialName("carbs_snapshot") val carbsSnapshot: Double,
    @SerialName("fat_snapshot") val fatSnapshot: Double,
    @SerialName("fiber_snapshot") val fiberSnapshot: Double = 0.0,
    @SerialName("sodium_snapshot") val sodiumSnapshot: Double = 0.0,
    @SerialName("sugar_snapshot") val sugarSnapshot: Double = 0.0,
    val nutrients: List<FoodNutrientDto> = emptyList(),
    @SerialName("nutrient_snapshot_json") val nutrientSnapshotJson: String? = null
) {
    fun toDomain() = NutritionCalculation(
        foodId = foodId,
        foodName = foodName,
        quantity = quantity,
        unit = unit,
        grams = grams,
        caloriesSnapshot = caloriesSnapshot,
        proteinSnapshot = proteinSnapshot,
        carbsSnapshot = carbsSnapshot,
        fatSnapshot = fatSnapshot,
        fiberSnapshot = fiberSnapshot,
        sodiumSnapshot = sodiumSnapshot,
        sugarSnapshot = sugarSnapshot,
        nutrients = nutrients.map { it.toDomain() },
        nutrientSnapshotJson = nutrientSnapshotJson
    )
}

@Serializable
data class CalculateFoodNutritionResponseDto(
    val data: NutritionCalculationDto
)

@Serializable
data class ToggleFavoriteResponseDto(
    @SerialName("is_favorite") val isFavorite: Boolean,
    val message: String? = null,
    @SerialName("food_id") val foodId: Long
)

