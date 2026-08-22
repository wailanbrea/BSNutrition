package com.bsnutrition.app.feature.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnButton
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.network.dto.RecipeDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    modifier: Modifier = Modifier,
    viewModel: RecipeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onRecipeLogged: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipeDetails(recipeId)
    }

    val recipe = uiState.selectedRecipe

    if (showLogDialog && recipe != null) {
        LogRecipeDialog(
            recipe = recipe,
            onDismiss = { showLogDialog = false },
            onConfirm = { mealType, servings ->
                viewModel.logRecipeToDiary(
                    recipeId = recipe.id,
                    mealType = mealType,
                    servings = servings,
                    onSuccess = {
                        showLogDialog = false
                        onRecipeLogged()
                    }
                )
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(recipe?.name ?: "Detalle de Receta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (recipe != null) {
                        IconButton(onClick = {
                            viewModel.deleteRecipe(recipe.id) { onNavigateBack() }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading || recipe == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    if (!recipe.description.isNullOrBlank()) {
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Nutrition Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Información Nutricional (Por Porción)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                MacroColumn("Calorías", "${recipe.caloriesPerServing} kcal", MaterialTheme.colorScheme.primary)
                                MacroColumn("Proteína", "${recipe.proteinPerServingG}g", Color(0xFF4CAF50))
                                MacroColumn("Carbos", "${recipe.carbsPerServingG}g", Color(0xFF2196F3))
                                MacroColumn("Grasas", "${recipe.fatPerServingG}g", Color(0xFFFF9800))
                            }
                        }
                    }

                    // Ingredients Card
                    BsnCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ingredientes (${recipe.ingredients.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            recipe.ingredients.forEachIndexed { index, ing ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = ing.customName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${ing.grams.toInt()}g • ${ing.calories} kcal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (index < recipe.ingredients.size - 1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    // Preparation Steps Card
                    if (recipe.steps.isNotEmpty()) {
                        BsnCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Pasos de Preparación",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                recipe.steps.forEach { step ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${step.stepNumber}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = step.instruction,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Bottom Log CTA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        BsnButton(
                            text = "Añadir a Diario (+${recipe.caloriesPerServing} kcal)",
                            onClick = { showLogDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogRecipeDialog(
    recipe: RecipeDto,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedMealType by remember { mutableStateOf("lunch") }
    var servings by remember { mutableDoubleStateOf(1.0) }
    var expandedMealDropdown by remember { mutableStateOf(false) }

    val mealOptions = listOf(
        "breakfast" to "Desayuno",
        "lunch" to "Almuerzo",
        "dinner" to "Cena",
        "snack_1" to "Snack / Merienda"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar en Diario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "¿A qué comida deseas añadir ${recipe.name}?")

                ExposedDropdownMenuBox(
                    expanded = expandedMealDropdown,
                    onExpandedChange = { expandedMealDropdown = it }
                ) {
                    OutlinedTextField(
                        value = mealOptions.firstOrNull { it.first == selectedMealType }?.second ?: "Almuerzo",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMealDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMealDropdown,
                        onDismissRequest = { expandedMealDropdown = false }
                    ) {
                        mealOptions.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedMealType = type
                                    expandedMealDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Porciones: $servings")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (servings > 0.5) servings -= 0.5 }) {
                            Icon(Icons.Default.Remove, contentDescription = "-0.5")
                        }
                        IconButton(onClick = { servings += 0.5 }) {
                            Icon(Icons.Default.Add, contentDescription = "+0.5")
                        }
                    }
                }

                val totalCals = (recipe.caloriesPerServing * servings).toInt()
                Text(
                    text = "Total a registrar: $totalCals kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMealType, servings) }) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
