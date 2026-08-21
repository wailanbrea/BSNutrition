package com.bsnutrition.app.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.model.MealLog
import com.bsnutrition.app.core.model.User
import com.bsnutrition.app.ui.theme.MacroCarbs
import com.bsnutrition.app.ui.theme.MacroFat
import com.bsnutrition.app.ui.theme.MacroProtein
import com.bsnutrition.app.ui.theme.MacroWater

@Composable
fun HomeScreen(
    user: User?,
    onNavigateToAddFood: (mealType: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header greeting
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${user?.name ?: "Usuario"} 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Resumen de hoy • Tu progreso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar datos"
                        )
                    }
                }
            }

            // 2. Calorie Summary Hero Card
            item {
                CalorieHeroCard(
                    consumedCalories = uiState.totalCaloriesConsumed,
                    targetCalories = uiState.targetCalories,
                    remainingCalories = uiState.remainingCalories,
                    calorieProgress = uiState.calorieProgress
                )
            }

            // 3. Macronutrients Breakdown Card
            item {
                MacroBreakdownCard(
                    proteinConsumed = uiState.totalProteinG,
                    proteinTarget = uiState.targetProteinG,
                    carbsConsumed = uiState.totalCarbsG,
                    carbsTarget = uiState.targetCarbsG,
                    fatConsumed = uiState.totalFatG,
                    fatTarget = uiState.targetFatG
                )
            }

            // 4. Quick Water Tracker Card
            item {
                HomeWaterTrackerCard(
                    waterConsumed = uiState.totalWaterMl,
                    waterTarget = uiState.targetWaterMl,
                    isLogging = uiState.isLoggingWater,
                    onAddWater = { amount -> viewModel.logWater(amount) }
                )
            }

            // 5. Meals Header
            item {
                Text(
                    text = "Comidas de Hoy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 6. Meal Quick Cards
            val meals = uiState.meals
            if (meals.isEmpty() && uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(meals, key = { it.id }) { meal ->
                    HomeMealCard(
                        meal = meal,
                        onAddFood = { onNavigateToAddFood(meal.mealType) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalorieHeroCard(
    consumedCalories: Int,
    targetCalories: Int,
    remainingCalories: Int,
    calorieProgress: Float,
    modifier: Modifier = Modifier
) {
    BsnCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Calorías Restantes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "$remainingCalories kcal",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingCalories >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 8.dp
                    )
                    CircularProgressIndicator(
                        progress = { calorieProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = if (remainingCalories >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        strokeWidth = 8.dp
                    )
                    Text(
                        text = "${(calorieProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Consumidas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$consumedCalories kcal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Meta Diaria",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$targetCalories kcal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MacroBreakdownCard(
    proteinConsumed: Double,
    proteinTarget: Double,
    carbsConsumed: Double,
    carbsTarget: Double,
    fatConsumed: Double,
    fatTarget: Double,
    modifier: Modifier = Modifier
) {
    BsnCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Macronutrientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            MacroBarRow(
                label = "Proteínas",
                consumedG = proteinConsumed,
                targetG = proteinTarget,
                color = MacroProtein
            )

            MacroBarRow(
                label = "Carbohidratos",
                consumedG = carbsConsumed,
                targetG = carbsTarget,
                color = MacroCarbs
            )

            MacroBarRow(
                label = "Grasas",
                consumedG = fatConsumed,
                targetG = fatTarget,
                color = MacroFat
            )
        }
    }
}

@Composable
fun MacroBarRow(
    label: String,
    consumedG: Double,
    targetG: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (targetG > 0) (consumedG / targetG).toFloat().coerceIn(0f, 1.2f) else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${consumedG.toInt()} / ${targetG.toInt()} g",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun HomeWaterTrackerCard(
    waterConsumed: Int,
    waterTarget: Int,
    isLogging: Boolean,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MacroWater, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = "Agua",
                        tint = Color.White
                    )
                }

                Column {
                    Text(
                        text = "Hidratación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Text(
                        text = "$waterConsumed / $waterTarget ml",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { onAddWater(250) },
                    enabled = !isLogging,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+250ml")
                }
                FilledTonalButton(
                    onClick = { onAddWater(500) },
                    enabled = !isLogging,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+500ml")
                }
            }
        }
    }
}

@Composable
fun HomeMealCard(
    meal: MealLog,
    onAddFood: () -> Unit,
    modifier: Modifier = Modifier
) {
    BsnCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    val itemsCount = meal.entries.size
                    Text(
                        text = if (itemsCount > 0) "$itemsCount alimento(s) • ${meal.totalCalories} kcal" else "Sin registrar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onAddFood) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar a ${meal.name}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
