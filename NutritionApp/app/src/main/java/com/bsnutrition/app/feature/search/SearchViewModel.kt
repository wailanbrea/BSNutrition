package com.bsnutrition.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.FoodRepository
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodPortion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryFlow = MutableStateFlow("")

    init {
        // Initial search to display popular / verified Dominican foods
        searchFoods(query = "", categoryId = null)

        _queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                searchFoods(query = query, categoryId = _uiState.value.selectedCategoryId)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        _queryFlow.value = newQuery
    }

    fun onCategorySelected(categoryId: Long?) {
        val nextCategoryId = if (_uiState.value.selectedCategoryId == categoryId) null else categoryId
        _uiState.update { it.copy(selectedCategoryId = nextCategoryId) }
        searchFoods(query = _uiState.value.query, categoryId = nextCategoryId)
    }

    fun searchFoods(query: String, categoryId: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = foodRepository.searchFoods(
                query = query.ifBlank { null },
                categoryId = categoryId
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = result.data,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Error al buscar alimentos"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun selectFood(foodId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, error = null) }
            when (val result = foodRepository.getFoodDetail(foodId)) {
                is Result.Success -> {
                    val foodDetail = result.data
                    val defaultPortion = foodDetail.portions.firstOrNull { it.isDefault }
                        ?: foodDetail.portions.firstOrNull()

                    _uiState.update {
                        it.copy(
                            isLoadingDetail = false,
                            selectedFood = foodDetail,
                            selectedPortion = defaultPortion,
                            customQuantity = 1.0
                        )
                    }
                    calculateNutrition(foodDetail, defaultPortion, 1.0)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetail = false,
                            error = result.message ?: "No se pudo cargar el detalle del alimento"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, error = null) }
            when (val result = foodRepository.getFoodByBarcode(barcode)) {
                is Result.Success -> {
                    val foodDetail = result.data
                    val defaultPortion = foodDetail.portions.firstOrNull { it.isDefault }
                        ?: foodDetail.portions.firstOrNull()

                    _uiState.update {
                        it.copy(
                            isLoadingDetail = false,
                            selectedFood = foodDetail,
                            selectedPortion = defaultPortion,
                            customQuantity = 1.0
                        )
                    }
                    calculateNutrition(foodDetail, defaultPortion, 1.0)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingDetail = false,
                            error = result.message ?: "Producto no encontrado por código de barras"
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onPortionChanged(portion: FoodPortion) {
        _uiState.update { it.copy(selectedPortion = portion) }
        val food = _uiState.value.selectedFood ?: return
        calculateNutrition(food, portion, _uiState.value.customQuantity)
    }

    fun onQuantityChanged(quantity: Double) {
        _uiState.update { it.copy(customQuantity = quantity) }
        val food = _uiState.value.selectedFood ?: return
        calculateNutrition(food, _uiState.value.selectedPortion, quantity)
    }

    fun dismissFoodDetail() {
        _uiState.update {
            it.copy(
                selectedFood = null,
                selectedPortion = null,
                calculation = null,
                customQuantity = 1.0
            )
        }
    }

    private fun calculateNutrition(food: FoodDetail, portion: FoodPortion?, quantity: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true) }
            when (val result = foodRepository.calculateNutrition(
                foodId = food.id,
                quantity = quantity,
                portionId = portion?.id,
                unit = portion?.unit ?: "g"
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isCalculating = false,
                            calculation = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isCalculating = false) }
                }
                is Result.Loading -> Unit
            }
        }
    }
}
