package com.bsnutrition.app.feature.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnButton
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.network.dto.StatisticsDataDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWeightDialog by remember { mutableStateOf(false) }

    if (showWeightDialog) {
        LogWeightDialog(
            onDismiss = { showWeightDialog = false },
            onConfirm = { weightKg, notes ->
                viewModel.logWeight(weightKg, notes)
                showWeightDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Mi Progreso", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7d" to "Últimos 7 Días", "30d" to "30 Días", "90d" to "90 Días").forEach { (period, label) ->
                    FilterChip(
                        selected = uiState.selectedPeriod == period,
                        onClick = { viewModel.setPeriod(period) },
                        label = { Text(label) }
                    )
                }
            }

            // Water Tracker Card
            WaterTrackerWidget(
                todayMl = uiState.todayWaterMl,
                targetMl = uiState.waterTargetMl,
                onAddWater = { amount -> viewModel.addWater(amount) }
            )

            // Weight Tracking Card
            WeightTrackerWidget(
                currentWeightKg = uiState.latestWeightKg,
                targetWeightKg = uiState.statistics?.targets?.let { uiState.latestWeightKg?.minus(2.0) },
                weightChangeKg = uiState.statistics?.weightSummary?.changeKg ?: 0.0,
                onLogWeightClick = { showWeightDialog = true }
            )

            // Statistics & Macronutrients Card
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.statistics != null) {
                NutritionStatsWidget(stats = uiState.statistics!!)
            }
        }
    }
}

@Composable
private fun WaterTrackerWidget(
    todayMl: Int,
    targetMl: Int,
    onAddWater: (Int) -> Unit
) {
    val progress = if (targetMl > 0) (todayMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    BsnCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text("Hidratación Diaria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("$todayMl / $targetMl ml ($percent%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF1E88E5),
                trackColor = Color(0xFFE0E0E0)
            )

            // Quick Add Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(250 to "+250 ml", 500 to "+500 ml", 750 to "+750 ml", 1000 to "+1 L").forEach { (amount, label) ->
                    OutlinedButton(
                        onClick = { onAddWater(amount) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightTrackerWidget(
    currentWeightKg: Double?,
    targetWeightKg: Double?,
    weightChangeKg: Double,
    onLogWeightClick: () -> Unit
) {
    BsnCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3E5F5),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MonitorWeight,
                                contentDescription = null,
                                tint = Color(0xFF8E24AA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text("Control de Peso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onLogWeightClick) {
                    Text("+ Registrar")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeightMetric(
                    label = "Peso Actual",
                    value = if (currentWeightKg != null) "${currentWeightKg} kg" else "-- kg",
                    subValue = if (currentWeightKg != null) "${roundTo1(currentWeightKg * 2.20462)} lbs" else null
                )
                WeightMetric(
                    label = "Peso Meta",
                    value = if (targetWeightKg != null) "${targetWeightKg} kg" else "-- kg",
                    subValue = null
                )
                WeightMetric(
                    label = "Cambio Período",
                    value = "${if (weightChangeKg > 0) "+" else ""}${weightChangeKg} kg",
                    color = if (weightChangeKg <= 0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun NutritionStatsWidget(stats: StatisticsDataDto) {
    BsnCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text("Consistencia y Nutrición", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${stats.trackedDays} de ${stats.totalDays} días registrados (${stats.adherenceRate}% meta)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Calorie Average Stat
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${stats.averages.calories} kcal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Promedio Diario", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${stats.targets.calories} kcal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Calorías Meta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Macro Split Distribution
            Text("Distribución de Macronutrientes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(stats.macroSplit.proteinPct.toFloat().coerceAtLeast(1f))
                        .fillMaxSize()
                        .background(Color(0xFF4CAF50))
                )
                Box(
                    modifier = Modifier
                        .weight(stats.macroSplit.carbsPct.toFloat().coerceAtLeast(1f))
                        .fillMaxSize()
                        .background(Color(0xFF2196F3))
                )
                Box(
                    modifier = Modifier
                        .weight(stats.macroSplit.fatPct.toFloat().coerceAtLeast(1f))
                        .fillMaxSize()
                        .background(Color(0xFFFF9800))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroIndicator("Proteína", "${stats.averages.proteinG}g", "${stats.macroSplit.proteinPct}%", Color(0xFF4CAF50))
                MacroIndicator("Carbohidratos", "${stats.averages.carbsG}g", "${stats.macroSplit.carbsPct}%", Color(0xFF2196F3))
                MacroIndicator("Grasas", "${stats.averages.fatG}g", "${stats.macroSplit.fatPct}%", Color(0xFFFF9800))
            }
        }
    }
}

@Composable
private fun WeightMetric(label: String, value: String, subValue: String? = null, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        if (subValue != null) {
            Text(text = subValue, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MacroIndicator(label: String, grams: String, percent: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Column {
            Text(text = "$label ($grams)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            Text(text = percent, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LogWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Peso Corporal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Peso en kg (ej: 75.5)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notas (ej: En ayunas)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = weightInput.toDoubleOrNull()
                    if (weight != null && weight > 20) {
                        onConfirm(weight, notesInput.ifBlank { null })
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun roundTo1(value: Double): Double {
    return Math.round(value * 10.0) / 10.0
}
