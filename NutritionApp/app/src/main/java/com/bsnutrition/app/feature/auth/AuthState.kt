package com.bsnutrition.app.feature.auth

import com.bsnutrition.app.core.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSuccess: Boolean = false
)
