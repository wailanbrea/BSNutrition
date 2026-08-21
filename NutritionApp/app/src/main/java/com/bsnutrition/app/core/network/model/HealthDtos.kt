package com.bsnutrition.app.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponseDto(
    val status: String,
    val version: String,
    val environment: String,
    val timestamp: String,
    val services: Map<String, String> = emptyMap()
)
