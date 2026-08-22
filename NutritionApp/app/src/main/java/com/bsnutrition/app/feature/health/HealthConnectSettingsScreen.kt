package com.bsnutrition.app.feature.health

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnButton
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.health.HealthConnectAvailability

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: HealthConnectViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        viewModel.checkStatus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Health Connect", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            BsnCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text(
                            text = "Google Health Connect",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Conecta con Google Fit, Samsung Health, Garmin y otras apps de salud de forma segura y privada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.availability != HealthConnectAvailability.AVAILABLE) {
                BsnCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Health Connect no disponible",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.availability == HealthConnectAvailability.NOT_INSTALLED)
                                "Se requiere instalar o actualizar Google Health Connect desde Google Play Store."
                            else
                                "Este dispositivo no es compatible con Health Connect.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (!uiState.hasPermissions) {
                BsnCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Permisos Requeridos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Otorga acceso para sincronizar pasos, peso, calorías quemadas y registrar tu ingesta de alimentos y agua.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BsnButton(
                            text = "Conceder Permisos",
                            onClick = {
                                // Request permissions
                                viewModel.syncMetricsNow()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Sync Options Card
            BsnCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sincronización Automática",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Activar lectura y exportación en segundo plano",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.settings.isEnabled,
                            onCheckedChange = { viewModel.toggleHealthConnect(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingToggleRow(
                        title = "Importar Pasos Diarios",
                        checked = uiState.settings.syncSteps,
                        enabled = uiState.settings.isEnabled,
                        onCheckedChange = { v -> viewModel.updateSetting { it.copy(syncSteps = v) } }
                    )

                    SettingToggleRow(
                        title = "Importar Peso Corporal",
                        checked = uiState.settings.syncWeight,
                        enabled = uiState.settings.isEnabled,
                        onCheckedChange = { v -> viewModel.updateSetting { it.copy(syncWeight = v) } }
                    )

                    SettingToggleRow(
                        title = "Importar Calorías Activas",
                        checked = uiState.settings.syncActiveCalories,
                        enabled = uiState.settings.isEnabled,
                        onCheckedChange = { v -> viewModel.updateSetting { it.copy(syncActiveCalories = v) } }
                    )

                    SettingToggleRow(
                        title = "Exportar Nutrición (Calorías/Macros)",
                        checked = uiState.settings.exportNutrition,
                        enabled = uiState.settings.isEnabled,
                        onCheckedChange = { v -> viewModel.updateSetting { it.copy(exportNutrition = v) } }
                    )

                    SettingToggleRow(
                        title = "Exportar Hidratación (Agua)",
                        checked = uiState.settings.exportHydration,
                        enabled = uiState.settings.isEnabled,
                        onCheckedChange = { v -> viewModel.updateSetting { it.copy(exportHydration = v) } }
                    )
                }
            }

            // Live Metrics Preview Card
            if (uiState.settings.isEnabled) {
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
                            Text(
                                text = "Métricas Sincronizadas (Hoy)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                IconButton(
                                    onClick = { viewModel.syncMetricsNow() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                                }
                            }
                        }

                        MetricItem(
                            icon = Icons.Default.DirectionsWalk,
                            label = "Pasos Hoy",
                            value = "${uiState.todaySteps} pasos"
                        )

                        MetricItem(
                            icon = Icons.Default.MonitorWeight,
                            label = "Último Peso",
                            value = if (uiState.latestWeightKg != null) "${uiState.latestWeightKg} kg" else "Sin registros"
                        )

                        MetricItem(
                            icon = Icons.Default.LocalFireDepartment,
                            label = "Calorías Activas",
                            value = "${uiState.activeCalories} kcal"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
