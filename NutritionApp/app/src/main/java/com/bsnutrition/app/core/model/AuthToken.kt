package com.bsnutrition.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthToken(
    val token: String,
    @SerialName("token_type") val tokenType: String = "Bearer"
)

@Serializable
data class AuthResponse(
    val user: User,
    val token: String,
    @SerialName("token_type") val tokenType: String = "Bearer"
)
