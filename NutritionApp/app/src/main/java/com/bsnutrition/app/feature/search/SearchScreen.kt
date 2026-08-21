package com.bsnutrition.app.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.model.FoodSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    onScanBarcodeClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Alimentos", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onScanBarcodeClick) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear código de barras"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Box
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Busca alimentos o platillos dominicanos...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category & Favorites Filter Chips
            val categories = listOf(
                Pair(null, "Todos"),
                Pair(1L, "Carnes"),
                Pair(4L, "Frutas"),
                Pair(6L, "Cereales"),
                Pair(7L, "Legumbres"),
                Pair(12L, "Platos Típicos")
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Favorites chip
                item {
                    FilterChip(
                        selected = uiState.isFavoritesTab,
                        onClick = { viewModel.onFavoritesTabToggled() },
                        label = { Text("⭐ Favoritos") }
                    )
                }

                // Recents chip
                item {
                    FilterChip(
                        selected = uiState.isRecentsTab,
                        onClick = { viewModel.onRecentsTabToggled() },
                        label = { Text("🕒 Recientes") }
                    )
                }

                items(categories) { (id, name) ->
                    FilterChip(
                        selected = !uiState.isFavoritesTab && !uiState.isRecentsTab && uiState.selectedCategoryId == id,
                        onClick = { viewModel.onCategorySelected(id) },
                        label = { Text(name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content State
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Error al buscar",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                uiState.searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val emptyMsg = when {
                            uiState.isFavoritesTab -> "No tienes alimentos favoritos guardados."
                            uiState.isRecentsTab -> "Aún no tienes alimentos registrados recientemente."
                            else -> "No se encontraron alimentos para '${uiState.query}'"
                        }
                        Text(
                            text = emptyMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.searchResults, key = { it.id }) { food ->
                            val isFav = uiState.favoriteFoodIds.contains(food.id)
                            FoodResultCard(
                                food = food,
                                isFavorite = isFav,
                                onToggleFavorite = { viewModel.toggleFavorite(food) },
                                onClick = { viewModel.selectFood(food.id) }
                            )
                        }
                    }
                }
            }
        }

        // Food Detail Bottom Sheet
        if (uiState.selectedFood != null) {
            val selectedFood = uiState.selectedFood!!
            val isFav = uiState.favoriteFoodIds.contains(selectedFood.id)

            FoodDetailSheet(
                food = selectedFood,
                selectedPortion = uiState.selectedPortion,
                quantity = uiState.customQuantity,
                calculation = uiState.calculation,
                isCalculating = uiState.isCalculating,
                isFavorite = isFav,
                onToggleFavorite = {
                    val summary = uiState.searchResults.firstOrNull { it.id == selectedFood.id }
                        ?: FoodSummary(
                            id = selectedFood.id,
                            canonicalName = selectedFood.canonicalName,
                            brand = selectedFood.brand,
                            category = selectedFood.category,
                            countryCode = selectedFood.countryCode,
                            verified = selectedFood.verified,
                            macrosPer100g = com.bsnutrition.app.core.model.MacroBreakdown(
                                calories = uiState.calculation?.caloriesSnapshot ?: 0,
                                proteinG = uiState.calculation?.proteinSnapshot ?: 0.0,
                                carbsG = uiState.calculation?.carbsSnapshot ?: 0.0,
                                fatG = uiState.calculation?.fatSnapshot ?: 0.0
                            )
                        )
                    viewModel.toggleFavorite(summary)
                },
                onPortionSelected = viewModel::onPortionChanged,
                onQuantityChanged = viewModel::onQuantityChanged,
                onDismiss = viewModel::dismissFoodDetail
            )
        }
    }
}

@Composable
fun FoodResultCard(
    food: FoodSummary,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (food.countryCode == "DO") {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "🇩🇴 RD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = food.canonicalName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    food.brand?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${food.macrosPer100g.calories} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Macros preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "P: ${food.macrosPer100g.proteinG}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "C: ${food.macrosPer100g.carbsG}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "G: ${food.macrosPer100g.fatG}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                food.defaultPortion?.let {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = it.portionName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
