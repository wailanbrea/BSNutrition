package com.bsnutrition.app.feature.diary

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.model.FoodLogEntry
import com.bsnutrition.app.core.model.MealLog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onNavigateToAddFood: (mealType: String, date: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Diario Nutricional",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.openCopyDayDialog() }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar día completo"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Date Navigation Header
            DateNavigationHeader(
                displayDate = uiState.displayDate,
                selectedDate = uiState.selectedDate,
                onPreviousDay = { viewModel.onPreviousDay() },
                onNextDay = { viewModel.onNextDay() },
                onToday = { viewModel.onToday() }
            )

            if (uiState.isLoading && uiState.diary == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Daily Calories & Macros Summary Card
                    item {
                        DailyProgressCard(
                            consumedCalories = uiState.totalCaloriesConsumed,
                            targetCalories = uiState.targetCalories,
                            remainingCalories = uiState.remainingCalories,
                            calorieProgress = uiState.calorieProgress,
                            proteinConsumed = uiState.totalProteinG,
                            proteinTarget = uiState.targetProteinG,
                            carbsConsumed = uiState.totalCarbsG,
                            carbsTarget = uiState.targetCarbsG,
                            fatConsumed = uiState.totalFatG,
                            fatTarget = uiState.targetFatG
                        )
                    }

                    // 2. Water Tracker Widget
                    item {
                        WaterTrackerCard(
                            waterMl = uiState.totalWaterMl,
                            isLogging = uiState.isLoggingWater,
                            onAddWater = { amount -> viewModel.logWater(amount) }
                        )
                    }

                    // 3. Meal Sections (Breakfast, Lunch, Dinner, Snack)
                    val meals = uiState.diary?.meals ?: emptyList()
                    items(meals, key = { it.id }) { meal ->
                        MealCard(
                            meal = meal,
                            onAddFood = { onNavigateToAddFood(meal.mealType, uiState.formattedDate) },
                            onDeleteEntry = { entryId -> viewModel.deleteMealEntry(entryId) },
                            onCopyMeal = { viewModel.openCopyMealDialog(meal) }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Copy Day
    if (uiState.showCopyDayDialog) {
        CopyDayDialog(
            sourceDate = uiState.formattedDate,
            onDismiss = { viewModel.dismissCopyDayDialog() },
            onConfirm = { targetDate -> viewModel.copyDay(targetDate) }
        )
    }

    // Dialog: Copy Meal
    if (uiState.showCopyMealDialog && uiState.mealToCopy != null) {
        CopyMealDialog(
            meal = uiState.mealToCopy!!,
            currentDate = uiState.selectedDate,
            onDismiss = { viewModel.dismissCopyMealDialog() },
            onConfirm = { meal, targetDate, targetType ->
                viewModel.copyMeal(meal, targetDate, targetType)
            }
        )
    }
}

@Composable
fun DateNavigationHeader(
    displayDate: String,
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Día anterior"
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (selectedDate != LocalDate.now()) {
                    IconButton(
                        onClick = onToday,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Ir a Hoy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onNextDay) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Día siguiente"
                )
            }
        }
    }
}

@Composable
fun DailyProgressCard(
    consumedCalories: Int,
    targetCalories: Int,
    remainingCalories: Int,
    calorieProgress: Float,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calorías restantes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$remainingCalories kcal",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingCalories >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Consumidas: $consumedCalories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Meta: $targetCalories kcal",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            LinearProgressIndicator(
                progress = { calorieProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (remainingCalories >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Macro Nutrients Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroSummaryItem(
                    label = "Proteína",
                    consumed = proteinConsumed,
                    target = proteinTarget,
                    color = Color(0xFF4CAF50)
                )
                MacroSummaryItem(
                    label = "Carbohidratos",
                    consumed = carbsConsumed,
                    target = carbsTarget,
                    color = Color(0xFFFF9800)
                )
                MacroSummaryItem(
                    label = "Grasas",
                    consumed = fatConsumed,
                    target = fatTarget,
                    color = Color(0xFFE91E63)
                )
            }
        }
    }
}

@Composable
fun MacroSummaryItem(
    label: String,
    consumed: Double,
    target: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (consumed / target).toFloat().coerceIn(0f, 1.2f) else 0f

    Column(
        modifier = modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${consumed.toInt()} / ${target.toInt()}g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun WaterTrackerCard(
    waterMl: Int,
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
                        .size(40.dp)
                        .background(Color(0xFF2196F3), CircleShape),
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
                        text = "Agua",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    val glasses = waterMl / 250
                    Text(
                        text = "$waterMl ml ($glasses vasos)",
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
fun MealCard(
    meal: MealLog,
    onAddFood: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onCopyMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    BsnCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Meal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${meal.totalCalories} kcal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (meal.entries.isNotEmpty()) {
                        IconButton(onClick = onCopyMeal, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar comida",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entries List
            if (meal.entries.isEmpty()) {
                Text(
                    text = "Sin alimentos registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                meal.entries.forEach { entry ->
                    MealEntryRow(
                        entry = entry,
                        onDelete = { onDeleteEntry(entry.id) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add Food Button
            OutlinedButton(
                onClick = onAddFood,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Agregar alimento")
            }
        }
    }
}

@Composable
fun MealEntryRow(
    entry: FoodLogEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.customName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${entry.quantity} ${entry.unit} (${entry.grams.toInt()}g) • P: ${entry.proteinSnapshot}g C: ${entry.carbsSnapshot}g G: ${entry.fatSnapshot}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${entry.caloriesSnapshot} kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CopyDayDialog(
    sourceDate: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val tomorrow = LocalDate.now().plusDays(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copiar día completo") },
        text = {
            Text("¿Deseas copiar todas las comidas del día $sourceDate al día de mañana (${tomorrow})?")
        },
        confirmButton = {
            Button(onClick = { onConfirm(tomorrow) }) {
                Text("Copiar a Mañana")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CopyMealDialog(
    meal: MealLog,
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (MealLog, LocalDate, String) -> Unit
) {
    val tomorrow = currentDate.plusDays(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copiar ${meal.name}") },
        text = {
            Text("¿Copiar los alimentos de ${meal.name} al día de mañana en la misma comida?")
        },
        confirmButton = {
            Button(onClick = { onConfirm(meal, tomorrow, meal.mealType) }) {
                Text("Copiar a Mañana")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
