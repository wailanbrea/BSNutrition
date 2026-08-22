package com.bsnutrition.app.feature.add

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bsnutrition.app.core.designsystem.component.BsnCard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import com.bsnutrition.app.feature.photo.AiFoodPhotoScreen
import com.bsnutrition.app.feature.scanner.BarcodeScannerScreen
import com.bsnutrition.app.feature.search.SearchScreen

@Composable
fun AddScreen(
    modifier: Modifier = Modifier,
    onNavigateToSearch: (() -> Unit)? = null
) {
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var isScanning by rememberSaveable { mutableStateOf(false) }
    var isTakingPhoto by rememberSaveable { mutableStateOf(false) }

    if (isTakingPhoto) {
        AiFoodPhotoScreen(
            onNavigateBack = { isTakingPhoto = false },
            onFoodLogged = { isTakingPhoto = false }
        )
        return
    }

    if (isScanning) {
        BarcodeScannerScreen(
            onNavigateBack = { isScanning = false },
            onFoodLogged = { isScanning = false }
        )
        return
    }

    if (isSearching) {
        SearchScreen(
            modifier = modifier,
            onScanBarcodeClick = { isScanning = true }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Registrar Alimento",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Selecciona una modalidad para agregar alimentos a tu diario",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val addMethods = listOf(
            Triple("Búsqueda en catálogo", "Busca entre más de 1M de alimentos verificados", Icons.Filled.Search),
            Triple("Escáner de código de barras", "Escanea productos envasados instantáneamente", Icons.Filled.QrCodeScanner),
            Triple("Foto con IA", "Reconocimiento visual de platillos con Gemini Vision", Icons.Filled.CameraAlt),
            Triple("Dictado por voz", "Describe lo que comiste en lenguaje natural", Icons.Filled.Mic),
            Triple("Entrada manual", "Registra calorías y macronutrientes manualmente", Icons.Filled.Edit)
        )

        addMethods.forEach { (title, subtitle, icon) ->
            AddMethodCard(
                title = title,
                subtitle = subtitle,
                icon = icon,
                onClick = {
                    when (title) {
                        "Búsqueda en catálogo" -> {
                            if (onNavigateToSearch != null) {
                                onNavigateToSearch()
                            } else {
                                isSearching = true
                            }
                        }
                        "Escáner de código de barras" -> {
                            isScanning = true
                        }
                        "Foto con IA" -> {
                            isTakingPhoto = true
                        }
                    }
                }
            )
        }
    }
}



@Composable
private fun AddMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    BsnCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
