package com.bsnutrition.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val title: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME(
        title = "Hoy",
        icon = Icons.Filled.Today,
        contentDescription = "Pantalla resumen de hoy"
    ),
    DIARY(
        title = "Diario",
        icon = Icons.Filled.Book,
        contentDescription = "Diario nutricional de comidas"
    ),
    ADD(
        title = "Registrar",
        icon = Icons.Filled.AddCircle,
        contentDescription = "Registro rápido de alimentos"
    ),
    PROGRESS(
        title = "Progreso",
        icon = Icons.Filled.TrendingUp,
        contentDescription = "Progreso y métricas de peso"
    ),
    MORE(
        title = "Más",
        icon = Icons.Filled.MoreHoriz,
        contentDescription = "Ajustes y perfil de usuario"
    )
}
