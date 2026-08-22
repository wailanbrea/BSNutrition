package com.bsnutrition.app.feature.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.RecipeRepository
import com.bsnutrition.app.core.network.dto.CreateRecipeRequest
import com.bsnutrition.app.core.network.dto.RecipeDto
import com.bsnutrition.app.core.network.dto.RecipeIngredientDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class RecipeUiState(
    val recipes: List<RecipeDto> = emptyList(),
    val selectedRecipe: RecipeDto? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLogging: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes(query: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = recipeRepository.getRecipes(query)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        recipes = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "No se pudieron cargar las recetas."
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun searchRecipes(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadRecipes(query.ifBlank { null })
    }

    fun selectRecipe(recipe: RecipeDto) {
        _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
    }

    fun loadRecipeDetails(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = recipeRepository.getRecipe(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        selectedRecipe = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "No se pudo cargar el detalle de la receta."
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun createRecipe(
        name: String,
        description: String?,
        servings: Int,
        prepMinutes: Int?,
        cookMinutes: Int?,
        ingredients: List<RecipeIngredientDto>,
        steps: List<String>,
        onSuccess: (RecipeDto) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val request = CreateRecipeRequest(
                name = name,
                description = description,
                servings = servings,
                prepTimeMinutes = prepMinutes,
                cookTimeMinutes = cookMinutes,
                ingredients = ingredients,
                steps = steps
            )
            when (val result = recipeRepository.createRecipe(request)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        successMessage = "Receta guardada exitosamente."
                    )
                    loadRecipes()
                    onSuccess(result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = result.message ?: "No se pudo guardar la receta."
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun deleteRecipe(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = recipeRepository.deleteRecipe(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedRecipe = null)
                    loadRecipes()
                    onDone()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "No se pudo eliminar la receta."
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun logRecipeToDiary(
        recipeId: Long,
        date: String = LocalDate.now().toString(),
        mealType: String = "lunch",
        servings: Double = 1.0,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLogging = true, errorMessage = null)
            when (val result = recipeRepository.logRecipeToDiary(recipeId, date, mealType, servings)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLogging = false,
                        successMessage = "Receta agregada al diario."
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLogging = false,
                        errorMessage = result.message ?: "No se pudo registrar la receta en el diario."
                    )
                }
                Result.Loading -> {}
            }
        }
    }
}
