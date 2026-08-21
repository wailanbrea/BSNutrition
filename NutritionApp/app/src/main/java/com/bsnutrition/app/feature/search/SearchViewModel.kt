package com.bsnutrition.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.FoodRepository
import com.bsnutrition.app.core.model.FoodDetail
import com.bsnutrition.app.core.model.FoodPortion
import com.bsnutrition.app.core.model.FoodSummary
import com.bsnutrition.app.core.model.MacroBreakdown
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
        loadFavorites()
        loadRecents()

        _queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (!_uiState.value.isFavoritesTab && !_uiState.value.isRecentsTab) {
                    searchFoods(query = query, categoryId = _uiState.value.selectedCategoryId)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        _queryFlow.value = newQuery
    }

    fun onCategorySelected(categoryId: Long?) {
        val nextCategoryId = if (_uiState.value.selectedCategoryId == categoryId) null else categoryId
        _uiState.update { it.copy(selectedCategoryId = nextCategoryId, isFavoritesTab = false, isRecentsTab = false) }
        searchFoods(query = _uiState.value.query, categoryId = nextCategoryId)
    }

    fun onFavoritesTabToggled() {
        val nextState = !_uiState.value.isFavoritesTab
        _uiState.update { it.copy(isFavoritesTab = nextState, isRecentsTab = false, selectedCategoryId = null) }
        if (nextState) {
            loadFavorites(displayAsResults = true)
        } else {
            searchFoods(query = _uiState.value.query, categoryId = null)
        }
    }

    fun onRecentsTabToggled() {
        val nextState = !_uiState.value.isRecentsTab
        _uiState.update { it.copy(isRecentsTab = nextState, isFavoritesTab = false, selectedCategoryId = null) }
        if (nextState) {
            loadRecents(displayAsResults = true)
        } else {
            searchFoods(query = _uiState.value.query, categoryId = null)
        }
    }

    fun loadFavorites(displayAsResults: Boolean = false) {
        viewModelScope.launch {
            if (displayAsResults) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            when (val result = foodRepository.getFavorites()) {
                is Result.Success -> {
                    val favorites = result.data
                    val favIds = favorites.map { it.id }.toSet()
                    _uiState.update {
                        it.copy(
                            favoriteFoodIds = favIds,
                            searchResults = if (displayAsResults) favorites else it.searchResults,
                            isLoading = if (displayAsResults) false else it.isLoading,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    if (displayAsResults) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Error al cargar favoritos"
                            )
                        }
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadRecents(displayAsResults: Boolean = false) {
        viewModelScope.launch {
            if (displayAsResults) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            when (val result = foodRepository.getRecentFoods()) {
                is Result.Success -> {
                    val recents = result.data
                    _uiState.update {
                        it.copy(
                            recentFoods = recents,
                            searchResults = if (displayAsResults) recents else it.searchResults,
                            isLoading = if (displayAsResults) false else it.isLoading,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    if (displayAsResults) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Error al cargar alimentos recientes"
                            )
                        }
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun toggleFavorite(food: FoodSummary) {
        viewModelScope.launch {
            when (val result = foodRepository.toggleFavorite(food)) {
                is Result.Success -> {
                    val isFav = result.data
                    _uiState.update { state ->
                        val updatedSet = if (isFav) {
                            state.favoriteFoodIds + food.id
                        } else {
                            state.favoriteFoodIds - food.id
                        }
                        val updatedResults = if (state.isFavoritesTab && !isFav) {
                            state.searchResults.filter { it.id != food.id }
                        } else {
                            state.searchResults
                        }
                        state.copy(
                            favoriteFoodIds = updatedSet,
                            searchResults = updatedResults
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message ?: "No se pudo actualizar favorito") }
                }
                is Result.Loading -> Unit
            }
        }
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

                    // Record as recent food
                    val summary = FoodSummary(
                        id = foodDetail.id,
                        canonicalName = foodDetail.canonicalName,
                        brand = foodDetail.brand,
                        category = foodDetail.category,
                        countryCode = foodDetail.countryCode,
                        verified = foodDetail.verified,
                        macrosPer100g = MacroBreakdown(
                            calories = 0,
                            proteinG = 0.0,
                            carbsG = 0.0,
                            fatG = 0.0
                        )
                    )
                    foodRepository.recordRecentFood(summary)

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
