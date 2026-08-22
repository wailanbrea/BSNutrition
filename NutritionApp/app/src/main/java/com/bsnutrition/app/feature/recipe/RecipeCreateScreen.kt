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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnButton
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.network.dto.RecipeIngredientDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreateScreen(
    modifier: Modifier = Modifier,
    viewModel: RecipeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onRecipeCreated: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var servings by remember { mutableIntStateOf(2) }
    var prepTime by remember { mutableStateOf("15") }
    var cookTime by remember { mutableStateOf("20") }

    val ingredients = remember {
        mutableStateListOf(
            RecipeIngredientDto(customName = "Pechuga de Pollo", grams = 300.0, calories = 495, proteinG = 93.0, carbsG = 0.0, fatG = 10.5),
            RecipeIngredientDto(customName = "Arroz Blanco", grams = 200.0, calories = 260, proteinG = 5.0, carbsG = 56.0, fatG = 1.0)
        )
    }

    val steps = remember {
        mutableStateListOf(
            "Cortar la pechuga en tiras y sazonar al gusto.",
            "Cocinar a fuego medio durante 15 minutos junto con el arroz."
        )
    }

    var newIngredientName by remember { mutableStateOf("") }
    var newIngredientGrams by remember { mutableStateOf("") }
    var newIngredientCals by remember { mutableStateOf("") }
    var newStepText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Crear Nueva Receta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
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

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Receta *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = servings.toString(),
                        onValueChange = { servings = it.toIntOrNull() ?: 1 },
                        label = { Text("Porciones *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Prep (min)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        label = { Text("Cocción (min)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Ingredients Section
                BsnCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ingredientes (${ingredients.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        ingredients.forEachIndexed { index, ing ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = ing.customName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${ing.grams.toInt()}g • ${ing.calories} kcal • P: ${ing.proteinG}g",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { ingredients.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            if (index < ingredients.size - 1) HorizontalDivider()
                        }

                        // Add Ingredient Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newIngredientName,
                                onValueChange = { newIngredientName = it },
                                label = { Text("Ingrediente") },
                                modifier = Modifier.weight(2f)
                            )
                            OutlinedTextField(
                                value = newIngredientGrams,
                                onValueChange = { newIngredientGrams = it },
                                label = { Text("Gramos") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (newIngredientName.isNotBlank()) {
                                    val g = newIngredientGrams.toDoubleOrNull() ?: 100.0
                                    val c = newIngredientCals.toIntOrNull() ?: (g * 1.5).toInt()
                                    ingredients.add(
                                        RecipeIngredientDto(
                                            customName = newIngredientName,
                                            grams = g,
                                            calories = c,
                                            proteinG = Math.round(g * 0.1 * 10.0) / 10.0,
                                            carbsG = Math.round(g * 0.2 * 10.0) / 10.0,
                                            fatG = Math.round(g * 0.05 * 10.0) / 10.0
                                        )
                                    )
                                    newIngredientName = ""
                                    newIngredientGrams = ""
                                    newIngredientCals = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Añadir Ingrediente")
                        }
                    }
                }

                // Steps Section
                BsnCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Instrucciones de Preparación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(text = step, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { steps.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newStepText,
                            onValueChange = { newStepText = it },
                            placeholder = { Text("Escribe un paso...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = {
                                if (newStepText.isNotBlank()) {
                                    steps.add(newStepText)
                                    newStepText = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Añadir Paso")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (uiState.isSaving) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        BsnButton(
                            text = "Guardar Receta",
                            onClick = {
                                if (name.isNotBlank() && ingredients.isNotEmpty()) {
                                    viewModel.createRecipe(
                                        name = name,
                                        description = description.ifBlank { null },
                                        servings = servings,
                                        prepMinutes = prepTime.toIntOrNull(),
                                        cookMinutes = cookTime.toIntOrNull(),
                                        ingredients = ingredients.toList(),
                                        steps = steps.toList(),
                                        onSuccess = { created ->
                                            onRecipeCreated(created.id)
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
