package com.bsnutrition.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.model.NutritionGoal
import com.bsnutrition.app.core.model.User
import com.bsnutrition.app.ui.theme.MacroCarbs
import com.bsnutrition.app.ui.theme.MacroFat
import com.bsnutrition.app.ui.theme.MacroProtein
import com.bsnutrition.app.ui.theme.MacroWater

@Composable
fun HomeScreen(
    user: User?,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val goal: NutritionGoal? = uiState.goal

    val calorieTarget = goal?.calorieTarget ?: 2000
    val proteinTarget = goal?.proteinTargetG ?: 150f
    val carbsTarget = goal?.carbohydrateTargetG ?: 220f
    val fatTarget = goal?.fatTargetG ?: 65f
    val waterTarget = goal?.waterTargetMl ?: 2500

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Hola, ${user?.name ?: "Usuario"} 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Resumen nutricional de hoy",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Calorie Summary Card
        BsnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Calorías Restantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Meta", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$calorieTarget kcal", fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Alimentos", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "0 kcal", fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Restante", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "$calorieTarget kcal",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Macro Targets Card
        BsnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Macronutrientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Proteínas", color = MacroProtein, fontWeight = FontWeight.Medium)
                    Text(text = "0 / $proteinTarget g", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Carbohidratos", color = MacroCarbs, fontWeight = FontWeight.Medium)
                    Text(text = "0 / $carbsTarget g", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Grasas", color = MacroFat, fontWeight = FontWeight.Medium)
                    Text(text = "0 / $fatTarget g", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Agua", color = MacroWater, fontWeight = FontWeight.Medium)
                    Text(text = "0 / $waterTarget ml", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

