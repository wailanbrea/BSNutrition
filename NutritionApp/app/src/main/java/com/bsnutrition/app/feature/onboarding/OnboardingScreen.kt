package com.bsnutrition.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.designsystem.component.BsnLoadingIndicator
import com.bsnutrition.app.core.designsystem.component.BsnPrimaryButton
import com.bsnutrition.app.core.designsystem.component.BsnSecondaryButton
import com.bsnutrition.app.core.designsystem.component.BsnTextField
import com.bsnutrition.app.core.model.NutritionGoal
import com.bsnutrition.app.ui.theme.MacroCarbs
import com.bsnutrition.app.ui.theme.MacroFat
import com.bsnutrition.app.ui.theme.MacroProtein
import com.bsnutrition.app.ui.theme.MacroWater

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isOnboardingComplete) {
        onOnboardingFinished()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paso ${uiState.currentStep.index} de 6: ${uiState.currentStep.title}",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    if (uiState.currentStep != OnboardingStep.BIRTH_SEX) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { uiState.currentStep.index / 6f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                AnimatedContent(
                    targetState = uiState.currentStep,
                    label = "OnboardingSteps"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (step) {
                            OnboardingStep.BIRTH_SEX -> BirthSexStepView(uiState, viewModel)
                            OnboardingStep.HEIGHT_WEIGHT -> HeightWeightStepView(uiState, viewModel)
                            OnboardingStep.ACTIVITY -> ActivityStepView(uiState, viewModel)
                            OnboardingStep.GOAL_RATE -> GoalRateStepView(uiState, viewModel)
                            OnboardingStep.UNITS -> UnitsStepView(uiState, viewModel)
                            OnboardingStep.REVIEW -> ReviewStepView(uiState)
                        }
                    }
                }
            }

            // Bottom action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BsnPrimaryButton(
                    text = if (uiState.currentStep == OnboardingStep.REVIEW) "Comenzar mi Plan" else "Continuar",
                    onClick = { viewModel.nextStep() },
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun BirthSexStepView(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Text(
        text = "¿Cuál es tu sexo biológico y fecha de nacimiento?",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Utilizamos esta información para calcular tu gasto calórico basal con precisión clínica.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OptionCard(
            title = "Hombre",
            selected = state.sex == "male",
            onClick = { viewModel.updateBirthAndSex(state.birthDate, "male") },
            modifier = Modifier.weight(1f)
        )
        OptionCard(
            title = "Mujer",
            selected = state.sex == "female",
            onClick = { viewModel.updateBirthAndSex(state.birthDate, "female") },
            modifier = Modifier.weight(1f)
        )
    }

    BsnTextField(
        value = state.birthDate,
        onValueChange = { viewModel.updateBirthAndSex(it, state.sex) },
        label = "Fecha de nacimiento (AAAA-MM-DD)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun HeightWeightStepView(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Text(
        text = "Tus medidas actuales",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Ingresa tu altura y peso para calcular tu metabolismo basal.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    BsnTextField(
        value = if (state.heightCm > 0) state.heightCm.toInt().toString() else "",
        onValueChange = {
            val h = it.toFloatOrNull() ?: 0f
            viewModel.updateHeightAndWeight(h, state.weightKg)
        },
        label = "Altura en centímetros (cm)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    BsnTextField(
        value = if (state.weightKg > 0) state.weightKg.toString() else "",
        onValueChange = {
            val w = it.toFloatOrNull() ?: 0f
            viewModel.updateHeightAndWeight(state.heightCm, w)
        },
        label = "Peso actual en kilogramos (kg)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun ActivityStepView(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Text(
        text = "¿Cuál es tu nivel de actividad física?",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    val activities = listOf(
        Pair("sedentary", Pair("Sedentario", "Poco o ningún ejercicio, trabajo de oficina (x1.2)")),
        Pair("light", Pair("Ligero", "Ejercicio ligero 1-3 días por semana (x1.375)")),
        Pair("moderate", Pair("Moderado", "Ejercicio moderado 3-5 días por semana (x1.55)")),
        Pair("active", Pair("Activo", "Ejercicio intenso 6-7 días por semana (x1.725)")),
        Pair("very_active", Pair("Muy Activo", "Ejercicio muy intenso, doble turno o labor física (x1.9)"))
    )

    activities.forEach { (key, info) ->
        OptionCard(
            title = info.first,
            subtitle = info.second,
            selected = state.activityLevel == key,
            onClick = { viewModel.updateActivityLevel(key) }
        )
    }
}

@Composable
private fun GoalRateStepView(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Text(
        text = "¿Cuál es tu objetivo nutricional?",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    val goals = listOf(
        Pair("lose_weight", "Perder peso (Déficit calórico)"),
        Pair("maintain_weight", "Mantener peso (Equilibrio calórico)"),
        Pair("gain_muscle", "Ganar masa muscular (Superávit)")
    )

    goals.forEach { (key, title) ->
        OptionCard(
            title = title,
            selected = state.goalType == key,
            onClick = { viewModel.updateGoalAndRate(key, state.weeklyGoalRate) }
        )
    }

    if (state.goalType != "maintain_weight") {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ritmo semanal deseado:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        val rates = listOf(
            Pair(0.25f, "0.25 kg / semana (Suave)"),
            Pair(0.5f, "0.5 kg / semana (Recomendado)"),
            Pair(0.75f, "0.75 kg / semana (Acelerado)"),
            Pair(1.0f, "1.0 kg / semana (Intenso)")
        )

        rates.forEach { (rate, label) ->
            OptionCard(
                title = label,
                selected = state.weeklyGoalRate == rate,
                onClick = { viewModel.updateGoalAndRate(state.goalType, rate) }
            )
        }
    }
}

@Composable
private fun UnitsStepView(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Text(
        text = "Sistema de unidades",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    OptionCard(
        title = "Sistema Métrico",
        subtitle = "Kilogramos (kg), Centímetros (cm), Mililitros (ml)",
        selected = state.unitSystem == "metric",
        onClick = { viewModel.updateUnitSystem("metric") }
    )

    OptionCard(
        title = "Sistema Imperial",
        subtitle = "Libras (lbs), Pies/Pulgadas (ft/in), Onzas líquidas (fl oz)",
        selected = state.unitSystem == "imperial",
        onClick = { viewModel.updateUnitSystem("imperial") }
    )
}

@Composable
private fun ReviewStepView(
    state: OnboardingUiState
) {
    Text(
        text = "Tus Metas Personalizadas",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Calculadas con el algoritmo Mifflin-St Jeor (mifflin_v1.0).",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (state.isLoading) {
        BsnLoadingIndicator(modifier = Modifier.height(150.dp))
    } else if (state.calculatedGoal != null) {
        val goal: NutritionGoal = state.calculatedGoal

        BsnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Meta Diaria de Calorías",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${goal.calorieTarget} kcal",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        BsnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Distribución de Macronutrientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Proteínas:", color = MacroProtein, fontWeight = FontWeight.Medium)
                    Text(text = "${goal.proteinTargetG} g", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Carbohidratos:", color = MacroCarbs, fontWeight = FontWeight.Medium)
                    Text(text = "${goal.carbohydrateTargetG} g", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Grasas:", color = MacroFat, fontWeight = FontWeight.Medium)
                    Text(text = "${goal.fatTargetG} g", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Agua:", color = MacroWater, fontWeight = FontWeight.Medium)
                    Text(text = "${goal.waterTargetMl ?: 2000} ml", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
