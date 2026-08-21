package com.bsnutrition.app.core.network.model

import com.bsnutrition.app.core.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
    @SerialName("device_name") val deviceName: String? = null
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
    @SerialName("device_name") val deviceName: String? = null
)

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    @SerialName("email_verified_at") val emailVerifiedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        emailVerifiedAt = emailVerifiedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class UserContainerDto(
    val user: UserDto
)

@Serializable
data class AuthResponseDto(
    val user: UserDto,
    val token: String,
    @SerialName("token_type") val tokenType: String = "Bearer"
)

@Serializable
data class MessageResponseDto(
    val message: String
)
