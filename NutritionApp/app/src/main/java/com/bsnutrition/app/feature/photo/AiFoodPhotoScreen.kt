package com.bsnutrition.app.feature.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsnutrition.app.core.designsystem.component.BsnButton
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.model.FoodCandidate
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFoodPhotoScreen(
    modifier: Modifier = Modifier,
    viewModel: AiFoodPhotoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onFoodLogged: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.ContextPlatformContext

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = processAndCompressImageUri(context, it)
            if (file != null) {
                viewModel.analyzePhoto(file)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reconocimiento IA", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AiFoodPhotoUiState.Capture -> {
                    CaptureView(
                        onSelectGallery = { galleryLauncher.launch("image/*") },
                        onTakePhoto = {
                            // Demo trigger / simulated photo
                            val sampleFile = createDummyFoodPhoto(context)
                            viewModel.analyzePhoto(sampleFile)
                        }
                    )
                }
                is AiFoodPhotoUiState.Analyzing -> {
                    AnalyzingView(message = state.message)
                }
                is AiFoodPhotoUiState.Review -> {
                    ReviewView(
                        state = state,
                        onWeightChange = { index, weight -> viewModel.updateItemWeight(index, weight) },
                        onSelectCandidate = { index, candidate -> viewModel.selectCandidate(index, candidate) },
                        onRemoveItem = { index -> viewModel.removeItem(index) },
                        onConfirm = { viewModel.confirmAndLog() },
                        onRetake = { viewModel.resetToCapture() }
                    )
                }
                is AiFoodPhotoUiState.LoggedSuccess -> {
                    SuccessView(
                        count = state.count,
                        onDone = {
                            viewModel.resetToCapture()
                            onFoodLogged()
                        }
                    )
                }
                is AiFoodPhotoUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.resetToCapture() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureView(
    onSelectGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Foto Nutricional con IA",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Toma una foto de tu comida o selecciona una imagen de tu galería. Nuestra IA reconocerá los alimentos, pesos y macronutrientes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        BsnButton(
            text = "Tomar Foto",
            onClick = onTakePhoto,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSelectGallery,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar de Galería")
        }
    }
}

@Composable
private fun AnalyzingView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Analizando con IA",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReviewView(
    state: AiFoodPhotoUiState.Review,
    onWeightChange: (Int, Double) -> Unit,
    onSelectCandidate: (Int, FoodCandidate) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Dish Title Card
                BsnCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.dishName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${(state.confidenceScore * 100).toInt()}% Confianza",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (state.summary.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                // Totals Macro Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MacroStat(label = "Calorías", value = "${state.totalCalories} kcal", color = MaterialTheme.colorScheme.primary)
                        MacroStat(label = "Proteína", value = "${state.totalProteinG}g", color = Color(0xFF4CAF50))
                        MacroStat(label = "Carbos", value = "${state.totalCarbsG}g", color = Color(0xFF2196F3))
                        MacroStat(label = "Grasas", value = "${state.totalFatG}g", color = Color(0xFFFF9800))
                    }
                }
            }

            item {
                Text(
                    text = "Ingredientes Identificados (${state.items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(state.items) { index, item ->
                FoodItemReviewCard(
                    item = item,
                    onWeightChange = { newWeight -> onWeightChange(index, newWeight) },
                    onSelectCandidate = { candidate -> onSelectCandidate(index, candidate) },
                    onRemove = { onRemoveItem(index) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Bottom CTA Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reintentar")
                }

                BsnButton(
                    text = "Registrar (${state.totalCalories} kcal)",
                    onClick = onConfirm,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
private fun FoodItemReviewCard(
    item: EditableFoodItem,
    onWeightChange: (Double) -> Unit,
    onSelectCandidate: (FoodCandidate) -> Unit,
    onRemove: () -> Unit
) {
    BsnCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${item.calories} kcal • P: ${item.proteinG}g • C: ${item.carbsG}g • G: ${item.fatG}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grams Adjuster Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Peso: ${item.weightGrams.toInt()} g",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onWeightChange(maxOf(10.0, item.weightGrams - 10.0)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-10g")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onWeightChange(item.weightGrams + 10.0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+10g")
                    }
                }
            }

            Slider(
                value = item.weightGrams.toFloat(),
                onValueChange = { onWeightChange(it.toDouble().roundToInt().toDouble()) },
                valueRange = 10f..500f,
                steps = 48,
                modifier = Modifier.fillMaxWidth()
            )

            // Candidates chips if available
            if (item.candidates.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Otras opciones detectadas:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.candidates.take(2).forEach { candidate ->
                        FilterChip(
                            selected = item.selectedFoodId == candidate.foodId,
                            onClick = { onSelectCandidate(candidate) },
                            label = { Text(candidate.canonicalName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SuccessView(count: Int, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Alimentos Registrados!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Se agregaron $count alimentos directamente a tu diario con cálculo nutricional exacto.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        BsnButton(
            text = "Ver Diario",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Error de Análisis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        BsnButton(
            text = "Reintentar",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun processAndCompressImageUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val scaled = scaleDownBitmap(bitmap, 1600)
        val tempFile = File.createTempFile("food_photo_", ".jpg", context.cacheDir)
        val out = FileOutputStream(tempFile)
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
        tempFile
    } catch (e: Exception) {
        null
    }
}

private fun scaleDownBitmap(realImage: Bitmap, maxImageSize: Float): Bitmap {
    val ratio = minOf(
        maxImageSize / realImage.width,
        maxImageSize / realImage.height
    )
    if (ratio >= 1.0) return realImage

    val width = (ratio * realImage.width).roundToInt()
    val height = (ratio * realImage.height).roundToInt()

    return Bitmap.createScaledBitmap(realImage, width, height, true)
}

private fun createDummyFoodPhoto(context: Context): File {
    val tempFile = File.createTempFile("demo_plate_", ".jpg", context.cacheDir)
    val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    val out = FileOutputStream(tempFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
    out.flush()
    out.close()
    return tempFile
}

private val Context.ContextPlatformContext: Context get() = this
