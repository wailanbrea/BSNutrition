package com.bsnutrition.app.feature.search

import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodPortion
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.NutritionCalculation

data class SearchUiState(
    val query: String = "",
    val selectedCategoryId: Long? = null,
    val isLoading: Boolean = false,
    val searchResults: List<FoodSummary> = emptyList(),
    val error: String? = null,
    val selectedFood: FoodDetail? = null,
    val isLoadingDetail: Boolean = false,
    val selectedPortion: FoodPortion? = null,
    val customQuantity: Double = 1.0,
    val calculation: NutritionCalculation? = null,
    val isCalculating: Boolean = false,
    val isFavoritesTab: Boolean = false,
    val favoriteFoodIds: Set<Long> = emptySet()
)

